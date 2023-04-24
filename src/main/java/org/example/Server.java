package org.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;


public class Server {
    public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static void main(String[] args) {
        // like nio, this only responsible for client connect
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // after client connect, handle the future io
        EventLoopGroup workerGroup = new NioEventLoopGroup(10);
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            ChannelFuture future = bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // handler will infect all the server channel, child handler will only affect the client channel
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ServerChildHandler());
                        }
                    }).bind(8888)
                    // to sync bind
                    .sync();
            System.out.println("server started");
            future.channel().closeFuture().sync(); // closeFuture: close() -> ChannelFuture
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

        System.out.println("a client connect!");
    }
}

class ServerChildHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Server.clients.add(ctx.channel());
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

//            ctx.writeAndFlush(msg);
            Server.clients.writeAndFlush(msg);
        } finally {
            if (buf != null) {
                System.out.println(buf.refCnt());
//                ReferenceCountUtil.release(buf);
                System.out.println(buf.refCnt());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}
