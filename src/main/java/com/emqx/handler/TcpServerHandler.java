package com.emqx.handler;

import com.emqx.util.ChannelUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TcpServerHandler extends ChannelInboundHandlerAdapter {
	private final Channel proxyChannel;

	public TcpServerHandler(Channel proxyChannel) {
		this.proxyChannel = proxyChannel;
	}
	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		proxyChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				// TODO Auto-generated method stub
				if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
			}
		});
	}
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.read();
	}
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ChannelUtil.closeOnFlush(proxyChannel);
		
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ChannelUtil.closeOnFlush(ctx.channel());
	}

}
