package com.acunetix.teamcity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.util.StringUtil;

@XStreamAlias("PluginSettings")
public class PluginSettings{
	
	private String serverURL;
	private String apiToken;

	private String proxyHost;
	private String proxyPort;
	private String proxyUsername;
	private String proxyPassword;
	private Boolean proxyUsed;
	
	public String getServerURL() {
		return serverURL;
	}
	
	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}
	
	public String getApiToken() {
		return apiToken;
	}
	
	public String getEncryptedApiToken() {
		return RSACipher.encryptDataForWeb(apiToken);
	}
	
	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}

	public Boolean getProxyUsed() { return proxyUsed; }

	public String getProxyHost() { return proxyHost; }

	public String getProxyPort() { return proxyPort; }

	public String getProxyUsername() { return StringUtil.isEmptyOrSpaces(proxyUsername)  || proxyUsername == null ? "" : proxyUsername; }

	public String getProxyPassword() { return StringUtil.isEmptyOrSpaces(proxyPassword)  || proxyPassword == null ? "" : proxyPassword; }

	public String getEncryptedProxyPassword() { return StringUtil.isEmptyOrSpaces(proxyPassword) || proxyPassword == null ? "" : RSACipher.encryptDataForWeb(proxyPassword); }

	public void setProxyUsed(Boolean proxyUsed){ this.proxyUsed = proxyUsed; }

	public void setProxyHost(String proxyHost){ this.proxyHost = proxyHost; }

	public void setProxyPort(String proxyPort) { this.proxyPort = proxyPort; }

	public void setProxyUsername(String proxyUsername) { this.proxyUsername = proxyUsername;}

	public void setProxyPassword(String proxyPassword) { this.proxyPassword = proxyPassword; }
}
