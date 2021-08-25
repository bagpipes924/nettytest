package com.emqx.server;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emqx.coder.DecodeProxy;
import com.emqx.handler.TcpProxyHandler;
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
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;

public class TcpProxyServer {
	private final static Logger logger = LoggerFactory.getLogger(TcpProxyServer.class);
	private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	private final EventLoopGroup group = new NioEventLoopGroup();
	private Proxy config;
	SslContext sslContext;
	
	public TcpProxyServer(Proxy config) {
		this.config = config;
	}
	public void start() throws Exception {
		final SSLContext sslCtx;
		try {
			if(config.isSsl()) {
				//加密解密算法  https://github.com/xjfuuu/SM2_SM3_SM4Encrypt
				
				//https://blog.csdn.net/weixin_37893887/article/details/87281323
				//https://github.com/devsunny/netty-ssl-example
				//https://help.aliyun.com/document_detail/42214.html?spm=a2c6h.12873639.0.0.215d5289tAOyK0#concept-a4g-mbv-ydb
				//sslCtx=SslContextFactory.getContext(config.getCertFile(), config.getKeyFile(), null);
				//sslCtx=SSLUtil.getSslContext(config.getKeyFile(), config.getCertFile(),config.getCaFile());
				
				if (config.isVerifyClient()) {
					//sslCtx = SslContextFactory.getServerContext(config.getCertFile(),config.getCertFile(),config.getKeyPassword());
					sslCtx=SslContextFactory.getContext(config.getKeyFile(),config.getCertFile(), config.getKeyPassword());
				}else {
					//sslCtx = SslContextFactory.getServerContext(config.getCertFile(),null,config.getKeyPassword());
					sslCtx=SslContextFactory.getContext(config.getKeyFile(),null, config.getKeyPassword());
				}
				
				/*
				if(config.isVerifyClient()) {
					sslContext=SSLUtil.getSslContext(config.getCertFile(),config.getKeyPassword());
				}else {
					sslContext=SSLUtil.getSslContext(config.getCertFile(),config.getKeyPassword());
				}
				*/
				//sslContext = SSLUtil.getSslContext(config.getCertFile(), config.getKeyFile(), null);
				
			}else {
				sslCtx=null;
			}
			ServerBootstrap sb = new ServerBootstrap();
			sb.group(bossGroup, group)
				.channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						// TODO Auto-generated method stub
						
						if(sslCtx!=null) {
							
							SSLEngine engine=sslCtx.createSSLEngine();
							//SSLEngine engine=sslContext.newEngine(ch.alloc());
							// 设置服务端模式
							engine.setUseClientMode(false);
							// 需要验证客户端身份,如果是双向验证，需要设为true
							if(config.isVerifyClient()) {
								engine.setNeedClientAuth(true);
							}else {
								engine.setNeedClientAuth(false);
							}
							ch.pipeline().addFirst("ssl", new SslHandler(engine));
							//ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()));
							 
						}
						// 添加编解码,tcp 不支持
						//ch.pipeline().addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
						//ch.pipeline().addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
						
						ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
						ch.pipeline().addLast("decoder",new DecodeProxy(config.getMode()));
						// 添加业务处理器
						ch.pipeline().addLast(new TcpProxyHandler(config.getServerIp(), config.getServerPort()));
					}
					
				})
				.childOption(ChannelOption.AUTO_READ, false);
			ChannelFuture future=sb.bind(config.getProxyPort()).sync();
			logger.info("开始监听端口 "+config.getProxyPort()+" 转发到-> "+config.getServerIp()+":"+config.getServerPort());
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("启动监听端口 "+config.getProxyPort()+" 异常："+e.getMessage());
		}
		
	}

}
