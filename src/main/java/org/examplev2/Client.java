package org.examplev2;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

public class Client {
    private Channel channel;
    public static void main(String[] args) {
        Client c = new Client();
        c.connect();
    }

    public void connect() {
        EventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();

        try {
            ChannelFuture future = bootstrap.group(group).channel(NioSocketChannel.class)
                    .handler(new ClientChannelInitializer()).connect("localhost", 8888);

            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        System.out.println("not connected!");
                    } else {
                        System.out.println("connected!");
                        // initialize channel
                        channel = future.channel();
                    }
                }
            });
            future.sync();
            System.out.println("...");
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
    public void send(String msg) {
        ByteBuf buf = Unpooled.copiedBuffer(msg.getBytes());
        channel.writeAndFlush(buf);
    }
    public void closeConnect() {
        this.send("__bye__");
    }
    static class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new ClientHandler());
        }
    }


    static class ClientHandler extends ChannelInboundHandlerAdapter {
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
                String msgAccepted = new String(bytes);
                ClientFrame.INSTANCE.updateText(msgAccepted);

            } finally {
                if (buf != null) {
                    ReferenceCountUtil.release(buf);
                    System.out.println(buf.refCnt());
                }
            }

        }
    }

}
