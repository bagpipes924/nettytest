package com.emqx.nettytest;

import java.security.Provider;
import java.security.Security;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.emqx.util.SslContextFactory;

public class TestMan {

	/*
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	*/
	      
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String certFile="/Users/bagpipes/Downloads/cert/gmzs2/server.crt";
		String keyFile="/Users/bagpipes/Downloads/cert/gmzs2/pkcs8.pem";
		String caFile="/Users/bagpipes/Downloads/cert/gmzs2/root.crt";
		//SSLContext sslCtx=SslContextFactory.test(certFile, keyFile, caFile);
		//SSLEngine engine=sslCtx.createSSLEngine();
		String pfxFile="/Users/bagpipes/Downloads/GM.Example/keystore/sm2.server1.both.pfx";
		String jksFile="/Users/bagpipes/Downloads/GM.Example/keystore/sm2.server1.jks";
		//ConventPFXToJKS.coverTokeyStore(pfxFile, "12345678", jksFile, "12345678");
		/*
		Security.addProvider(new BouncyCastleProvider());
        BouncyCastleProvider bc = new BouncyCastleProvider();
        Set<Provider.Service> services = bc.getServices();
        for (Provider.Service s:services){
            if (s.toString().toUpperCase().contains("CIPHER")) System.out.println(s.toString());
        }
		*/
	}

}
