package com.acunetix.teamcity;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import jetbrains.buildServer.serverSide.crypt.RSACipher;

@XStreamAlias("PluginSettings")
public class PluginSettings{
	
	private String serverURL;
	private String apiToken;
	
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
}
