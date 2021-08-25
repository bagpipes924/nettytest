package com.emqx.model;

import java.util.List;

public class Config {
	private int port;
	private List<Proxy> proxys;
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public List<Proxy> getProxys() {
		return proxys;
	}
	public void setProxys(List<Proxy> proxys) {
		this.proxys = proxys;
	}

}
