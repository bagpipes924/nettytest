package com.emqx.handler;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.emqx.coder.DecodeProxy;
import com.emqx.util.ChannelUtil;
import com.sun.swing.internal.plaf.metal.resources.metal;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;

public class TcpProxyHandler extends ChannelInboundHandlerAdapter{
	private final String host;
	private final int port;
	
	private Channel outboundChannel;
	public TcpProxyHandler(String host, int port) {
		this.host = host;
		this.port = port;
	}
	private boolean flag=false;
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

		final Channel proxyChannel = ctx.channel();
		//System.out.println(getRemoteAddress(ctx));
		Bootstrap b = new Bootstrap();
        b.group(proxyChannel.eventLoop())
         .channel(ctx.channel().getClass())
         //.handler(new LoggingHandler(LogLevel.INFO))
         .option(ChannelOption.AUTO_READ, false)
         //.handler(new TcpServerHandler(proxyChannel));
         
         .handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new TcpServerHandler(proxyChannel));
			}
        	 
         });
        ChannelFuture f = b.connect(host, port);
        outboundChannel = f.channel();
        /*
        String proxy="PORXY TCP4 "+getIP(proxyChannel)+" "+getIP(outboundChannel)+" "+getPort(proxyChannel)+" "+getPort(outboundChannel)+"\\r\\n";
        System.out.println(proxy);
        outboundChannel.writeAndFlush(proxy);
        */
        f.addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
                    // connection complete start to read first data
			        
                    proxyChannel.read();

                } else {
                    // Close the connection if the connection attempt has failed.
                    proxyChannel.close();
                }
			}
		});
		
	}
	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
		
		if (outboundChannel.isActive()) {
			//addressInfoMap.put(channelId, proxy);
            //System.out.println(channelId+": "+proxy+": "+msg.toString());
            outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        // was able to flush out data, start to read the next chunk
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
		System.out.println("客户端断开连接："+ctx.channel().id().asLongText());
		DecodeProxy.addressInfoMap.remove(ctx.channel().id().asLongText());
		ChannelUtil.closeOnFlush(outboundChannel);
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		System.out.println("异常断开连接："+ctx.channel().id().asLongText());
		DecodeProxy.addressInfoMap.remove(ctx.channel().id().asLongText());
		ChannelUtil.closeOnFlush(ctx.channel());
	}
	
}
