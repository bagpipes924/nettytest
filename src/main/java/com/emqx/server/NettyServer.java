package com.emqx.server;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emqx.coder.DecodeProxy;
import com.emqx.handler.NettyProxyHandler;
import com.emqx.model.Proxy;
import com.emqx.util.SSLUtil;
import com.emqx.util.SslContextFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

public class NettyServer implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(NettyServer.class);
	private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	private final EventLoopGroup group = new NioEventLoopGroup();
	private Proxy config;
	private SSLContext sslContext=null;

	public NettyServer(Proxy config) {
		this.config = config;
	}


	@Override
	public void run() {

		try {
			if(config.isSsl()) {
				
				if(config.isVerifyClient()) {
					//sslContext=SSLUtil.getSslContext(config.getCertFile(),config.getKeyPassword());
					sslContext=SslContextFactory.getGMContext2(config.getKeyFile(),config.getCertFile(), config.getKeyPassword());
					logger.info("监听端口:"+config.getProxyPort()+"采用 ssh双向认证");
				}else {
					//sslContext=SSLUtil.getSslContext(config.getCertFile(),config.getKeyPassword());
					sslContext=SslContextFactory.getGMContext2(config.getKeyFile(),null, config.getKeyPassword());
					logger.info("监听端口:"+config.getProxyPort()+"采用 ssh单向认证");
				}
			}
			ServerBootstrap sb = new ServerBootstrap();
			sb.group(bossGroup, group)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(LogLevel.INFO))
			.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					if(sslContext!=null) {
						//SSLEngine engine=sslContext.newEngine(ch.alloc());
						SSLEngine engine=sslContext.createSSLEngine();
						// 设置服务端模式
						engine.setUseClientMode(false);
						// 需要验证客户端身份,如果是双向验证，需要设为true
						if(config.isVerifyClient()) {
							engine.setNeedClientAuth(true);
						}else {
							engine.setNeedClientAuth(false);
						}
						ch.pipeline().addFirst("ssl", new SslHandler(engine));
					}
					ch.pipeline().addLast(new LoggingHandler(LogLevel.ERROR));
					ch.pipeline().addLast("decoder",new DecodeProxy(config.getMode()));
					// 添加自定义业务处理器
					ch.pipeline().addLast(new NettyProxyHandler(config.getServerIp(), config.getServerPort()));
				}
				
			})
			.childOption(ChannelOption.AUTO_READ, false);
			ChannelFuture future=sb.bind(config.getProxyPort()).sync();
			future.channel().closeFuture().sync();
			logger.info("开始监听端口 "+config.getProxyPort()+" 转发到-> "+config.getServerIp()+":"+config.getServerPort());
			System.out.println("开始监听端口 "+config.getProxyPort()+" 转发到-> "+config.getServerIp()+":"+config.getServerPort());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("启动监听端口 "+config.getProxyPort()+" 异常："+e.getMessage());
		}
	}
	
}
