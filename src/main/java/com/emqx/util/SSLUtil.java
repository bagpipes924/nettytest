package com.emqx.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;

import com.emqx.model.Proxy;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

public class SSLUtil {
	public static SslContext getSslContext(String keyCertChainFile, String keyFile,String caFile) throws SSLException {
		SslContext sslCtx=null;
		List<String> ciphers=Arrays.asList("","");
		sslCtx = SslContextBuilder.forServer(new File(keyCertChainFile), new File(keyCertChainFile))
				//.protocols("TLSv1.3","TLSv1.2")
				.sslProvider(SslProvider.JDK)
				//.clientAuth(ClientAuth.REQUIRE)
				.trustManager(caFile==null?null:new File(caFile))
				//.ciphers(ciphers)
				.build();
		return sslCtx;
		
	}
	
	
	@SuppressWarnings("unused")
	public static SslContext getSslContext(File keyCertChainFile, File keyFile,File caFile) throws SSLException {
		SslContext sslCtx=null;
		List<String> ciphers=Arrays.asList("","");
		sslCtx = SslContextBuilder.forServer(keyCertChainFile, keyCertChainFile)
				.protocols("TLSv1.3","TLSv1.2")
				.sslProvider(SslProvider.OPENSSL)
				.clientAuth(ClientAuth.NONE)
				.trustManager(caFile==null?null:caFile)
				//.ciphers(ciphers)
				.build();;
		return sslCtx;
		
	}
	public static SslContext getSslContext(String keyCertChainFile, String password) throws Exception {
		KeyManagerFactory keyManagerFactory = null;
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(keyCertChainFile), password.toCharArray());
        keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore,password.toCharArray());
        SslContext sslContext = SslContextBuilder.forServer(keyManagerFactory).build();
        return sslContext;
	}

}
