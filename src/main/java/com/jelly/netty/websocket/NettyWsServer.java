package com.jelly.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class NettyWsServer {
    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>(){
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast("http-codec", new HttpServerCodec());
                    socketChannel.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
                    socketChannel.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                    socketChannel.pipeline().addLast("handler", new NettyWsHandler());
                }
            });
            System.out.println("服务端开启等待客户端连接....");
            Channel ch = serverBootstrap.bind(8080).sync().channel();
            ch.closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //优雅的退出程序
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
