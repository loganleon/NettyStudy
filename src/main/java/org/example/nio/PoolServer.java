package org.example.nio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.spec.ECField;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PoolServer {
    // instead of use one thread, using one thread to watch the serverSocketChannel,
    // then use other threads to handle the channel between the client
    ExecutorService pool = Executors.newFixedThreadPool(50);

    private Selector selector;

    public static void main(String[] args) {
        PoolServer server = new PoolServer();
        server.initServer(8888);
        server.listen();
    }

    public void initServer(int port) {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            this.selector = Selector.open();
            serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
            System.out.println("open successfully");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void listen() {
        while (true) {
            try {
                this.selector.select();
                Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel channel = serverSocketChannel.accept();
                        channel.configureBlocking(false);
                        channel.register(this.selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                        pool.execute(new ThreadHandlerChannel(key));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
class ThreadHandlerChannel extends Thread {
    private SelectionKey key;
    ThreadHandlerChannel(SelectionKey key) {this.key = key;}

    @Override
    public void run() {
        SocketChannel channel = (SocketChannel) key.channel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            int size = 0;
            while ((size = channel.read(buffer)) > 0) {
                buffer.flip();
                byteArrayOutputStream.write(buffer.array(), 0, size);
                buffer.clear();
            }
            byteArrayOutputStream.close();

            byte[] content = byteArrayOutputStream.toByteArray();
            ByteBuffer writeBuf = ByteBuffer.allocate(content.length);
            writeBuf.put(content);
            writeBuf.flip();

            channel.write(writeBuf);

            if (size == -1) {
                channel.close();;
            } else {
                this.key.interestOps(this.key.interestOps() | SelectionKey.OP_READ);
                this.key.selector().wakeup();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
