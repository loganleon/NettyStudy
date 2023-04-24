package org.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

public class Client {
    public static void main(String[] args) {
        // Thread pool
        EventLoopGroup group = new NioEventLoopGroup(10);
        // class for initial
        Bootstrap bootstrap = new Bootstrap();
        try {
//            bootstrap.group(group)
//                    .channel(NioSocketChannel.class)
//                    .handler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        protected void initChannel(SocketChannel ch) throws Exception {
//                            System.out.println(ch);
//                        }
//                    })
//                    .connect("localhost", 8888)
//                    // connect is an async method,  sync is to wait for the end
//                    .sync();

            ChannelFuture future = bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            System.out.println(ch);
                            ch.pipeline().addLast(new ClientHandler());
                        }
                    })
                    .connect("localhost", 8888);
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        System.out.println("not connected");
                    } else {
                        System.out.println("connected");
                    }
                }
            });
            future.sync();
            System.out.println("finished");
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }

    }
}

class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // channel first available. direct access the memory of the operating system, instead of copy to JVM
        ByteBuf byteBuf = Unpooled.copiedBuffer("hello".getBytes());
        // Flush automatically release the memory
        ctx.writeAndFlush(byteBuf);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // msg store the data
        ByteBuf buf = null;
        try {
            buf = (ByteBuf) msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            System.out.println(new String(bytes));

        } finally {
            if (buf != null) {
                ReferenceCountUtil.release(buf);
                System.out.println(buf.refCnt());
            }
        }

    }
}