package com.emqx.coder;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.emqx.constant.Constant;
import com.emqx.util.ChannelUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

public class DecodeProxy extends ByteToMessageDecoder{
	public static AttributeKey<String> key = AttributeKey.valueOf("IP");
	public static Map<String, String> addressInfoMap = new ConcurrentHashMap<>();
	private String mode;
	
	
	public DecodeProxy(String mode) {
		this.mode = mode;
	}
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		
		byte[] bytes = printSz(in);
        String message = new String(bytes, Charset.forName("UTF-8"));
        System.out.println("消息转换："+message);
        if(bytes.length>0) {
        	String channelId = ctx.channel().id().asLongText();
        	if(mode.equals(Constant.HTTP)) {
        		out.add(in.readBytes(in.readableBytes()));
        	}else {
            	if(!addressInfoMap.containsKey(channelId)) {
            		String proxy=null;
            		if(message.indexOf("PROXY") != -1){
                		//tcp代理连接过来
            			if(message.indexOf("\n") != -1){
            				proxy=message.split("\n")[0];
            				out.add(in.readBytes(in.readableBytes()));
            			}
                	}else if (message.indexOf("X-Forwarded-For") != -1) {
        				//外部websocket 代理连接过来
                		
        			}else {
        				//客户端直连java代理
        				proxy="PROXY TCP4 "+ChannelUtil.getIP(ctx.channel(),false)+" "+ChannelUtil.getIP(ctx.channel(),true)+" "+ChannelUtil.getPort(ctx.channel(),false)+" "+ChannelUtil.getPort(ctx.channel(),true)+"\r\n";
        				ByteBuf proxyBuf=Unpooled.copiedBuffer(proxy, CharsetUtil.UTF_8);
        				proxyBuf.writeBytes(in);
        				byte[] bytes2 = printSz(proxyBuf);
        		        String message2 = new String(bytes, Charset.forName("UTF-8"));
        		        System.out.println("新增消息转换："+message2);
        				out.add(proxyBuf.readBytes(proxyBuf.readableBytes()));
        			}
            		addressInfoMap.put(channelId, proxy);
            		System.out.println("当前连接总量："+addressInfoMap.size());
            	}else {
            		if(in.readableBytes() > 0){
    	        		out.add(in.readBytes(in.readableBytes()));
    	        	}
    			}
			}

        	
        }
		
		/*
		String channelId = ctx.channel().id().asLongText();
		if(!addressInfoMap.containsKey(channelId)) {
			String proxy="PROXY TCP4 "+ChannelUtil.getIP(ctx.channel(),false)+" "+ChannelUtil.getIP(ctx.channel(),true)+" "+ChannelUtil.getPort(ctx.channel(),false)+" "+ChannelUtil.getPort(ctx.channel(),true)+"\r\n";
			addressInfoMap.put(channelId, proxy);
			ByteBuf proxyBuf=Unpooled.copiedBuffer(proxy, CharsetUtil.UTF_8);
			proxyBuf.writeBytes(in);
			byte[] bytes = printSz(proxyBuf);
	        String message = new String(bytes, Charset.forName("UTF-8"));
	        System.out.println("新增消息转换："+message);
			if(proxyBuf.readableBytes() > 0){
        		out.add(proxyBuf.readBytes(proxyBuf.readableBytes()));
        		//in.skipBytes(in.readableBytes());
        	}
		}else {
			byte[] bytes = printSz(in);
	        String message = new String(bytes, Charset.forName("UTF-8"));
	        System.out.println("消息转换："+message);
	        if(bytes.length > 0){
	        	if(message.indexOf("PROXY") != -1){
	        		System.out.println("PROXY MSG: " + message.substring(0,message.length()-2));
	        		if(message.indexOf("\n") != -1){
	        			String[] str =  message.split("\n")[0].split(" ");
	        			System.out.println("Real Client IP: " + str[2]);
	        			Attribute<String> channelAttr = ctx.channel().attr(key);
	        			if(null == channelAttr.get()){
	        				channelAttr.set(str[2]);
	        			}
	        		}
	        		in.clear();
	        	}else if(message.indexOf("X-Forwarded-For") != -1) {
	        		String[] str =  message.substring(message.indexOf("X-Forwarded-For")).split("\n")[0].split(" ");
					System.out.println("Real Client IP: " + str[1]);
				}
	        	if(in.readableBytes() > 0){
	        		out.add(in.readBytes(in.readableBytes()));
	        	}
	        }
		}
		 */
		
	}
	public byte[] printSz(ByteBuf newBuf){
        ByteBuf copy = newBuf.copy();
        byte[] bytes = new byte[copy.readableBytes()];
        copy.readBytes(bytes);
        System.out.println("字节数组打印:" + Arrays.toString(bytes));
        //logger.info("字节数组打印:" + Arrays.toString(bytes));
        return bytes;
    }


}
