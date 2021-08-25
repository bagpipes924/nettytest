package com.emqx.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emqx.coder.DecodeProxy;
import com.emqx.util.ChannelUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;

public class NettyProxyHandler extends ChannelInboundHandlerAdapter {

	private final static Logger logger = LoggerFactory.getLogger(NettyProxyHandler.class);
	private final String host;
	private final int port;
	
	private Channel outboundChannel;
	
	public NettyProxyHandler(String host, int port) {
		this.host = host;
		this.port = port;
	}
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		final Channel proxyChannel = ctx.channel();
		Bootstrap b = new Bootstrap();
        b.group(proxyChannel.eventLoop())
         .channel(ctx.channel().getClass())
         .option(ChannelOption.AUTO_READ, false)
         .handler(new ChannelInitializer<SocketChannel>() {

 			@Override
 			protected void initChannel(SocketChannel ch) throws Exception {
 				ch.pipeline().addLast(new NettyClientHandler(proxyChannel));
 			}
         	 
          });
         ChannelFuture f = b.connect(host, port);
         outboundChannel = f.channel();
         f.addListener(new ChannelFutureListener() {
 			
 			@Override
 			public void operationComplete(ChannelFuture future) throws Exception {
 				if (future.isSuccess()) {
                     proxyChannel.read();

                 } else {
                     proxyChannel.close();
                 }
 			}
 		});
	}
	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		if (outboundChannel.isActive()) {
			outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
		}
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		DecodeProxy.addressInfoMap.remove(ctx.channel().id().asLongText());
		logger.info("客户端通道断开连接："+ctx.channel().id().asLongText());
		ChannelUtil.closeOnFlush(outboundChannel);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		DecodeProxy.addressInfoMap.remove(ctx.channel().id().asLongText());
		logger.error("服务端通道异常断开连接："+ctx.channel().id().asLongText());
		ChannelUtil.closeOnFlush(ctx.channel());
	}
}
