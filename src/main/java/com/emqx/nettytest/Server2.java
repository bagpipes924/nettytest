package com.emqx.nettytest;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import javax.net.ssl.*;

import cn.gmssl.jce.provider.GMJCE;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class Server2
{
	public Server2()
	{
	}

	public static void main(String[] args) throws Exception
	{
		Security.insertProviderAt((Provider)Class.forName("cn.gmssl.jce.provider.GMJCE").newInstance(), 1);
		Security.insertProviderAt((Provider)Class.forName("cn.gmssl.jsse.provider.GMJSSE").newInstance(), 2);
		
		int port = 8899;
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup wokerGroup = new NioEventLoopGroup();
		try
		{
			ServerBootstrap serverBootstrap = new ServerBootstrap();
			serverBootstrap.group(bossGroup, wokerGroup).channel(NioServerSocketChannel.class).childHandler(new MyServerInitializer2());
			ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
			System.out.println("服务已开启");
			channelFuture.channel().closeFuture().sync();
		}
		finally
		{
			bossGroup.shutdownGracefully();
			wokerGroup.shutdownGracefully();
		}
	}
}

class TrustAllManager2 implements X509TrustManager
{
	private X509Certificate[] issuers;

	public TrustAllManager2()
	{
		this.issuers = new X509Certificate[0];
	}

	public X509Certificate[] getAcceptedIssuers()
	{
		return issuers;
	}

	public void checkClientTrusted(X509Certificate[] chain, String authType)
	{
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType)
	{
	}
}

class MyServerInitializer2 extends ChannelInitializer<SocketChannel>
{
	@Override
	protected void initChannel(SocketChannel ch) throws Exception
	{
		ChannelPipeline pipeline = ch.pipeline();
		String pfxfile = "/Volumes/Keyaas/Projects/GM.Example/keystore/sm2.server1.both.pfx";
		pfxfile = "/Users/bagpipes/Downloads/GM.Example/keystore/sm2.server1.both.pfx";
		String pwdpwd = "12345678";
		KeyStore pfx = KeyStore.getInstance("PKCS12", GMJCE.NAME);
		pfx.load(new FileInputStream(pfxfile), pwdpwd.toCharArray());
		SSLContext ctx = createServerSocketFactory(pfx, pwdpwd.toCharArray());
		SSLEngine sslEngine = ctx.createSSLEngine();
		sslEngine.setUseClientMode(false);
		sslEngine.setNeedClientAuth(true);
	
		sslEngine.setEnabledProtocols("GMSSLv1.1".split(","));
		
		pipeline.addFirst("ssl", new SslHandler(sslEngine));
		
		pipeline.addLast(new HttpResponseEncoder());
		pipeline.addLast(new HttpRequestDecoder());
		pipeline.addLast(new HttpServerInboundHandler2());
	}

	public static SSLContext createServerSocketFactory(KeyStore kepair, char[] pwd) throws Exception
	{
		{
			TrustManager[] trust =
				{ new TrustAllManager2() };
		}
		KeyManager[] kms = null;
		if (kepair != null)
		{
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(kepair, pwd);
			kms = kmf.getKeyManagers();
		}
		
    	CertificateFactory cf = CertificateFactory.getInstance("X.509");
    	KeyStore trust = KeyStore.getInstance("PKCS12","GMJCE");
    	trust.load(null);
        FileInputStream fin = new FileInputStream("/Users/bagpipes/Downloads/caifen/server1.oca.pem");//"/Users/qi/Downloads/sm2.t1_server/sm2.oca.pem");
        X509Certificate oca = (X509Certificate)cf.generateCertificate(fin);
    	trust.setCertificateEntry("oca", oca);
        fin = new FileInputStream("/Users/bagpipes/Downloads/caifen/server1.rca.pem");//"/Users/qi/Downloads/sm2.t1_server/sm2.rca.pem");
        X509Certificate rca = (X509Certificate)cf.generateCertificate(fin);
    	trust.setCertificateEntry("rca", rca);

		TrustManager[] tms = null;
		if (kepair != null)
		{
			// 指定指定的证书验证
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(trust);
			tms = tmf.getTrustManagers();
		}
		else
		{
			// 不验证(信任全部)
			tms = new TrustManager[1];
			tms[0] = new TrustAllManager2();
		}

		SSLContext ctx = SSLContext.getInstance(cn.gmssl.jsse.provider.GMJSSE.GMSSLv11, cn.gmssl.jsse.provider.GMJSSE.NAME);
		java.security.SecureRandom secureRandom = new java.security.SecureRandom();
		ctx.init(kms, tms, secureRandom);
		ctx.getServerSessionContext().setSessionCacheSize(8192);
		ctx.getServerSessionContext().setSessionTimeout(3600);
		return ctx;
	}
}


class HttpServerInboundHandler2 extends ChannelInboundHandlerAdapter {

    private HttpRequest request;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;

            String uri = request.getUri();
            System.out.println("Uri:" + uri);
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            ByteBuf buf = content.content();
            System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));
            buf.release();
            String res = "I am OK";
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                    OK, Unpooled.wrappedBuffer(res.getBytes("UTF-8")));
            response.headers().set(CONTENT_TYPE, "text/plain");
            response.headers().set(CONTENT_LENGTH,
                    response.content().readableBytes());
            if (HttpHeaders.isKeepAlive(request)) {
                response.headers().set(CONNECTION, Values.KEEP_ALIVE);
            }
            ctx.write(response);
            ctx.flush();
            ctx.close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) 
    {
    	cause.printStackTrace();
        ctx.close();
    }

}