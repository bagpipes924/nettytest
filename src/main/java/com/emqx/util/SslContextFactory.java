package com.emqx.util;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.bind.DatatypeConverter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;



public class SslContextFactory {
	
	
	private static final String PROTOCOL = "TLS";

    private static SSLContext SERVER_CONTEXT;//服务器安全套接字协议

    private static SSLContext CLIENT_CONTEXT;//客户端安全套接字协议
    
    public static KeyStore createKeyStore(String keyStorePath, String password) throws Exception {

        KeyStore keyStore = KeyStore.getInstance("PKCS12", "GMJSSE");
        keyStore.load(new FileInputStream(keyStorePath), password.toCharArray());

        return keyStore;
    }
    public static SSLContext getGMContext(String keyfile ,String rootCA,String middleCA,String passwd){
    	SSLContext context=null;
    	KeyStore keyStore=null;
    	try {
    		KeyManager[] kms = null;
    		if(keyfile!=null) {
    			keyStore=createKeyStore(keyfile, passwd);
    			KeyManagerFactory kmf=KeyManagerFactory.getInstance("SunX509");
    			kmf.init(keyStore, passwd.toCharArray());
    			kms = kmf.getKeyManagers();
    		}
    		
    		CertificateFactory cf = CertificateFactory.getInstance("X.509");
    		KeyStore trustStore = KeyStore.getInstance("PKCS12","GMJCE");
    		trustStore.load(null);
    		if(rootCA!=null) {
    			X509Certificate rca = (X509Certificate)cf.generateCertificate(new FileInputStream(rootCA));
    			trustStore.setCertificateEntry("rca", rca);
    		}
    		if(middleCA!=null) {
    			X509Certificate oca = (X509Certificate)cf.generateCertificate(new FileInputStream(middleCA));
    			trustStore.setCertificateEntry("oca", oca);
    		}
    		if(rootCA==null&& middleCA==null) {
    			trustStore=null;
    		}
    		TrustManager[] tms = { new TrustAllManager() };
    		if(trustStore!=null) {
    			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    			tmf.init(trustStore);
    			tms = tmf.getTrustManagers();
    		}
    		context = SSLContext.getInstance("GMSSLv1.1", "GMJSSE");
    		java.security.SecureRandom secureRandom = new java.security.SecureRandom();
    		context.init(kms, tms, secureRandom);
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return context;
    }

    public static SSLContext getGMContext2(String keyfile ,String certFile,String passwd){
    	TrustManager[] trust = { new TrustAllManager() };
    	SSLContext context=null;
    	try {
    		KeyStore ks = KeyStore.getInstance("PKCS12", "GMJSSE");
    		
    		KeyManagerFactory kmf=KeyManagerFactory.getInstance("SunX509");
    		if(passwd!=null) {
				ks.load(new FileInputStream(keyfile), passwd.toCharArray());
            	kmf.init(ks, passwd.toCharArray());
            }else {
            	ks.load(new FileInputStream(keyfile),null);
            	kmf.init(ks,null);
			}
    		context = SSLContext.getInstance("GMSSLv1.1", "GMJSSE");
			java.security.SecureRandom secureRandom = new java.security.SecureRandom();
			context.init(kmf.getKeyManagers(), trust, secureRandom);
    		
		} catch (Exception e) {
			e.printStackTrace();
			context=null;
		}
    	return context;
    }

    public static SSLContext getGMContext(String keyfile ,String certFile,String passwd){
    	SSLContext context=null;
    	InputStream keyStream=null;
    	InputStream certsStream =null;
    	try {
    		
    		////密钥管理器
    		KeyManagerFactory kmf = null;
    		if (keyfile != null){
    			keyStream=new FileInputStream(keyfile);
    			KeyStore ks = KeyStore.getInstance("PKCS12", "GMJSSE");
        		
    			kmf = KeyManagerFactory.getInstance("SunX509");
    			if(passwd!=null) {
    				ks.load(keyStream, passwd.toCharArray());
                	kmf.init(ks, passwd.toCharArray());
                }else {
                	ks.load(keyStream,null);
                	kmf.init(ks,null);
				}
    		}
    		//信任库
    		TrustManagerFactory tf = null;
    		if(certFile!=null) {
    			certsStream=new FileInputStream(certFile);
    			KeyStore ks = KeyStore.getInstance("PKCS12", "GMJSSE");
    			if(passwd!=null) {
                	ks.load(certsStream, passwd.toCharArray());
                }else {
                	ks.load(certsStream,null);
				}
    			tf = TrustManagerFactory.getInstance("SunX509");
    			tf.init(ks);
    		}
			context = SSLContext.getInstance("GMSSLv1.1", "GMJSSE");
			java.security.SecureRandom secureRandom = new java.security.SecureRandom();
			if(kmf!=null&& tf!=null) {
				context.init(kmf.getKeyManagers(), tf.getTrustManagers(), secureRandom);
			}else if (kmf==null&& tf!=null) {
				context.init(null, tf.getTrustManagers(), secureRandom);
			}else if(kmf!=null&& tf==null){
				context.init(kmf.getKeyManagers(), null, secureRandom);
			}else {
				return null;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			context=null;
		}
		return context;
    	
    }
    


    public static SSLContext getServerContext(String pkPath,String caPath, String passwd){
        if(SERVER_CONTEXT!=null) return SERVER_CONTEXT;
        InputStream in =null;
        InputStream tIN = null;

        try{
            //密钥管理器
            KeyManagerFactory kmf = null;
            if(pkPath!=null){
                KeyStore ks = KeyStore.getInstance("JKS");
                in = new FileInputStream(pkPath);
                ks.load(in, passwd.toCharArray());

                kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, passwd.toCharArray());
            }
            //信任库
            TrustManagerFactory tf = null;
            if (caPath != null) {
                KeyStore tks = KeyStore.getInstance("JKS");
                tIN = new FileInputStream(caPath);
                tks.load(tIN, passwd.toCharArray());
                tf = TrustManagerFactory.getInstance("SunX509");
                tf.init(tks);
            }

            SERVER_CONTEXT= SSLContext.getInstance(PROTOCOL);

            //初始化此上下文
            //参数一：认证的密钥      参数二：对等信任认证  参数三：伪随机数生成器 。 由于单向认证，服务端不用验证客户端，所以第二个参数为null
            //单向认证？无需验证客户端证书
            if(tf == null){
                SERVER_CONTEXT.init(kmf.getKeyManagers(),null, null);
            }
            //双向认证，需要验证客户端证书
            else{
                SERVER_CONTEXT.init(kmf.getKeyManagers(),tf.getTrustManagers(), null);
            }


        }catch(Exception e){
            throw new Error("Failed to initialize the server-side SSLContext", e);
        }finally{
            if(in !=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                in = null;
            }

            if (tIN != null){
                try {
                    tIN.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tIN = null;
            }
        }

        return SERVER_CONTEXT;
    }


    public static SSLContext getClientContext(String pkPath,String caPath, String passwd){
        if(CLIENT_CONTEXT!=null) return CLIENT_CONTEXT;

        InputStream in = null;
        InputStream tIN = null;
        try{
            KeyManagerFactory kmf = null;
            if (pkPath != null) {
                KeyStore ks = KeyStore.getInstance("JKS");
                in = new FileInputStream(pkPath);
                ks.load(in, passwd.toCharArray());
                kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, passwd.toCharArray());
            }

            TrustManagerFactory tf = null;
            if (caPath != null) {
                KeyStore tks = KeyStore.getInstance("JKS");
                tIN = new FileInputStream(caPath);
                tks.load(tIN, passwd.toCharArray());
                tf = TrustManagerFactory.getInstance("SunX509");
                tf.init(tks);
            }

            CLIENT_CONTEXT = SSLContext.getInstance(PROTOCOL);

            //初始化此上下文
            //参数一：认证的密钥      参数二：对等信任认证  参数三：伪随机数生成器 。 由于单向认证，服务端不用验证客户端，所以第二个参数为null
            //单向认证？无需验证服务端证书
            if(tf == null){
                //设置信任证书
                CLIENT_CONTEXT.init(null,tf == null ? null : tf.getTrustManagers(), null);
            }
            //双向认证，需要验证客户端证书
            else{
                CLIENT_CONTEXT.init(kmf.getKeyManagers(),tf.getTrustManagers(), null);
            }
        }catch(Exception e){
            throw new Error("Failed to initialize the client-side SSLContext");
        }finally{
            if(in !=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                in = null;
            }

            if (tIN != null){
                try {
                    tIN.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tIN = null;
            }
        }

        return CLIENT_CONTEXT;
    }

	
	public static SSLContext createSSLContext(String type , String path , String password) throws Exception {
        KeyStore ks = KeyStore.getInstance(type); /// "JKS"
        InputStream ksInputStream = new FileInputStream(path); /// 证书存放地址
        ks.load(ksInputStream, password.toCharArray());
        //KeyManagerFactory充当基于密钥内容源的密钥管理器的工厂。
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());//getDefaultAlgorithm:获取默认的 KeyManagerFactory 算法名称。
        kmf.init(ks, password.toCharArray());
        //SSLContext的实例表示安全套接字协议的实现，它充当用于安全套接字工厂或 SSLEngine 的工厂。
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }
	public static SSLContext getContext(String pkPath,String caPath,String keyPassword){
		SSLContext serverSslContext=null;
		InputStream in =null;
        InputStream tIn = null;
        try {
        	serverSslContext=SSLContext.getInstance(PROTOCOL);
        	//密钥管理器
        	KeyManagerFactory kmf = null;
        	if(pkPath!=null){
        		KeyStore ks = KeyStore.getInstance("JKS");
                in = new FileInputStream(pkPath);
                

                kmf = KeyManagerFactory.getInstance("SunX509");
                if(keyPassword!=null) {
                	ks.load(in, keyPassword.toCharArray());
                	kmf.init(ks, keyPassword.toCharArray());
                }else {
                	ks.load(in,null);
                	kmf.init(ks,null);
				}
                
        	}
        	//信任库
            TrustManagerFactory tf = null;
            if (caPath != null) {
                KeyStore tks = KeyStore.getInstance("JKS");
                tIn = new FileInputStream(caPath);
                if(keyPassword!=null) {
                	tks.load(tIn, keyPassword.toCharArray());
                }else {
                	tks.load(tIn,null);
				}
                tf = TrustManagerFactory.getInstance("SunX509");
                tf.init(tks);
                serverSslContext.init(kmf.getKeyManagers(),tf.getTrustManagers(), null);
            }else {
            	 serverSslContext.init(kmf.getKeyManagers(),null, null);
			}
            
		} catch (Exception e) {
			// TODO: handle exception
			throw new Error("Failed to initialize SSLContext", e);
		}finally {
			if(in !=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                in = null;
            }

            if (tIn != null){
                try {
                    tIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tIn = null;
            }
		}
        return serverSslContext;
	}


    public static byte[] loadPrivateKeyByFile(String path) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String readLine = null;
            StringBuilder sb = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) == '-') {
                    continue;
                } else {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }
            br.close();
            return DatatypeConverter.parseBase64Binary(sb.toString());
        } catch (IOException e) {
            throw new Exception("私钥数据读取错误");
        } catch (NullPointerException e) {
            throw new Exception("私钥输入流为空");
        }
    }
    

}
