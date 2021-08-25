package com.emqx.model;

public class Proxy {
	private String serverIp;
	private int serverPort;
	private int proxyPort;
	private boolean ssl;
	private String mode="tcp";
	private boolean verifyClient;
	private String certFile;
	private String keyFile;
	private String keyPassword;
	

	public String getServerIp() {
		return serverIp;
	}
	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}
	public int getServerPort() {
		return serverPort;
	}
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	public int getProxyPort() {
		return proxyPort;
	}
	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}
	public boolean isSsl() {
		return ssl;
	}
	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}
	public String getCertFile() {
		return certFile;
	}
	public void setCertFile(String certFile) {
		this.certFile = certFile;
	}
	public String getKeyFile() {
		return keyFile;
	}
	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}
	public String getKeyPassword() {
		return keyPassword;
	}
	public void setKeyPassword(String keyPassword) {
		this.keyPassword = keyPassword;
	}

	public boolean isVerifyClient() {
		return verifyClient;
	}
	public void setVerifyClient(boolean verifyClient) {
		this.verifyClient = verifyClient;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}

	

}
