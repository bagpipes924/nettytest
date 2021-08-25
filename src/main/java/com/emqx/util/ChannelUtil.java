package com.emqx.util;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

public class ChannelUtil {
	
	public static void closeOnFlush(Channel ch) {
		if (ch.isActive()) {
			ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	public static String getNettyRequestIp(ChannelHandlerContext ctx) {
		try {
			return ctx.channel().remoteAddress().toString().replace("/", "");
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("getNettyRequestIp:"+e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
    public static String getRemoteAddress(ChannelHandlerContext channelHandlerContext){
        String socketString = "";
        socketString = channelHandlerContext.channel().remoteAddress().toString();
        return socketString;
    }
    public static String getIPString(ChannelHandlerContext channelHandlerContext){
        String ipString = "";
        String socketString = channelHandlerContext.channel().remoteAddress().toString();
        int colonAt = socketString.indexOf(":");
        ipString = socketString.substring(1,colonAt);
        return ipString;
    }
    
    public static String getIP(Channel channel,boolean local){
    	
        String ip= "";
        String socketString=null;
        if(local) {
        	socketString= channel.localAddress().toString();
        }else {
        	socketString= channel.remoteAddress().toString();
		}
        int colonAt = socketString.indexOf(":");
        ip = socketString.substring(1,colonAt);
        return ip;
    }
    public static String getPort(Channel channel,boolean local){
        String port = "";
        String socketString=null;
        if(local) {
        	socketString= channel.localAddress().toString();
        }else {
        	socketString= channel.remoteAddress().toString();
		}
        int colonAt = socketString.indexOf(":");
        port = socketString.substring(colonAt+1);
        return port;
    }


}
