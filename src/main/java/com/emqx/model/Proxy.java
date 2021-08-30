package com.emqx.model;

public class Proxy {
	private String serverIp;
	private int serverPort;
	private int proxyPort;
	private boolean ssl;
	private String mode="tcp";
	private boolean verifyClient;
	private String pfxFile;
	private String rootCAFile;
	private String middleCAFile;
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
	public String getPfxFile() {
		return pfxFile;
	}
	public void setPfxFile(String pfxFile) {
		this.pfxFile = pfxFile;
	}
	public String getRootCAFile() {
		return rootCAFile;
	}
	public void setRootCAFile(String rootCAFile) {
		this.rootCAFile = rootCAFile;
	}
	public String getMiddleCAFile() {
		return middleCAFile;
	}
	public void setMiddleCAFile(String middleCAFile) {
		this.middleCAFile = middleCAFile;
	}

	

}
