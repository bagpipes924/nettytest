package com.emqx.nettytest;

import java.io.FileReader;
import java.net.URL;
import java.security.Provider;
import java.security.Security;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emqx.model.Config;
import com.emqx.model.Proxy;
import com.emqx.server.NettyServer;
import com.emqx.server.TcpProxyServer;
import com.esotericsoftware.yamlbeans.YamlReader;



public class App {
	private final static Logger logger = LoggerFactory.getLogger(App.class);
	private static Config config=null;
	public static void main( String[] args ){

    	try {
    		Security.insertProviderAt((Provider)Class.forName("cn.gmssl.jce.provider.GMJCE").newInstance(), 1);
			Security.insertProviderAt((Provider)Class.forName("cn.gmssl.jsse.provider.GMJSSE").newInstance(), 2);
    		URL url=App.class.getClassLoader().getResource("");
    		//String path= System.getProperty("user.dir")+"/config/config.yaml";
    		String path=url.getPath()+"/config/config.yaml";
    		YamlReader reader=new YamlReader(new FileReader(path));
    		config=reader.read(Config.class);
    		
    		for(Proxy proxy:config.getProxys()) {
    			Thread proxyThread=new Thread(new NettyServer(proxy));
    			proxyThread.start();
    			
    		}
    		
    		//TcpProxyServer server=new TcpProxyServer(config.getProxys().get(1));
			//server.start();
    	} catch (Exception e) {
			// TODO: handle exception
    		e.printStackTrace();
		}
    	/*
        int port=8080;
       // NettyServer server=new NettyServer(port);
        //HttpNettyServer server=new HttpNettyServer(port);
        Proxy config=new Proxy();
        config.setServerIp("192.168.148.27");
        config.setServerPort(1883);
        config.setProxyPort(port);
        //HttpProxyServer server=new HttpProxyServer(config);
        TcpProxyServer server=new TcpProxyServer(config);
        try {
			server.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
    }
}
