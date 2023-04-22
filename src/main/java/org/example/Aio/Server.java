package org.example.Aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class Server {

    public static void main(String[] args) {
        try {
            final AsynchronousServerSocketChannel ssc = AsynchronousServerSocketChannel.open()
                    .bind(new InetSocketAddress(8888));
            // this method is not blocking
            ssc.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                @Override
                public void completed(AsynchronousSocketChannel client, Object attachment) {
                    ssc.accept(null, this);
                    try {
                        System.out.println(client.getRemoteAddress());
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        // read is also async
                        client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                            @Override
                            public void completed(Integer result, ByteBuffer attachment) {
                                attachment.flip();
                                System.out.println(new String(attachment.array(), 0, result));
                                client.write(ByteBuffer.wrap("HelloClient".getBytes()));
                            }

                            @Override
                            public void failed(Throwable exc, ByteBuffer attachment) {
                                exc.printStackTrace();
                            }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void failed(Throwable exc, Object attachment) {
                    exc.printStackTrace();
                }
            });

            // accept is async, so in case it ends
            while (true) {
                Thread.sleep(1000);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
