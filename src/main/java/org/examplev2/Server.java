package org.examplev2;

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

    public void serverStart() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(5);

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            ChannelFuture future = bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new TankMsgDecoder()).addLast(new ServerChildHandler());
                        }
                    }).bind(8888)
                    .sync();
            ServerFrame.INSTANCE.updateServerMsg("server started!");
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
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
//            buf = (ByteBuf) msg;
//            byte[] bytes = new byte[buf.readableBytes()];
//            buf.getBytes(buf.readerIndex(), bytes);
//            String s = new String(bytes);
//            System.out.println(s);
//            ServerFrame.INSTANCE.updateClientMsg(s);
//            if (s.equals("__bye__")) {
//                ServerFrame.INSTANCE.updateServerMsg("client ask to disconnect");
//                Server.clients.remove(ctx.channel());
//                ctx.close();
//                ReferenceCountUtil.release(buf);
//            } else {
//                Server.clients.writeAndFlush(msg);
//            }
            TankMsg tankMsg = (TankMsg) msg;
            System.out.println(tankMsg.toString());
        } finally {
//            if (buf != null) {
//                System.out.println(buf.refCnt());
////                ReferenceCountUtil.release(buf);
//                System.out.println(buf.refCnt());
//            }
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        Server.clients.remove(ctx.channel());
        ctx.close();
    }
}

