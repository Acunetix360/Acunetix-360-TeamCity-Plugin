package com.acunetix.teamcity;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.security.ForbiddenClassException;

import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.web.util.CameFromSupport;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class PluginSettingsManager {
	private File configFile;
	private XStream xStream;
	private PluginSettings pluginSettings;
	private CameFromSupport cameFromSupport;

	public PluginSettingsManager(@NotNull ServerPaths serverPaths) {
		xStream = new XStream(new DomDriver());
		XStream.setupDefaultSecurity(xStream);
		xStream.autodetectAnnotations(true);
		xStream.processAnnotations(PluginSettings.class);
		xStream.setClassLoader(PluginSettings.class.getClassLoader());
		xStream.addPermission(AnyTypePermission.ANY);

		configFile = new File(serverPaths.getConfigDir(), "acunetix-config.xml");
		loadConfig();
	}

	public PluginSettings getPluginSettings() {
		synchronized (this) {
			loadConfig();
		}

		return pluginSettings;
	}

	public void setPluginSettings(PluginSettings pluginSettings) {
		this.pluginSettings = pluginSettings;
	}

	public String getHexEncodedPublicKey() {
		return RSACipher.getHexEncodedPublicKey();
	}

	public String getRandom() {
		return String.valueOf(Math.random());
	}

	public CameFromSupport getCameFromSupport() {
		return cameFromSupport;
	}

	public void setCameFromSupport(CameFromSupport cameFromSupport) {
		this.cameFromSupport = cameFromSupport;
	}

	public void save() {
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(configFile);
			xStream.toXML(pluginSettings, outputStream);
		} catch (FileNotFoundException e) {
			ServerLogger.logError("PluginSettings", e);
		} finally {
			FileUtil.close(outputStream);
		}
	}

	private void loadConfig() {
		if (configFile.exists()) {
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(configFile);
				Object config = this.xStream.fromXML(inputStream);
				if (config instanceof PluginSettings) {
					pluginSettings = (PluginSettings) config;
				} else {
					throw new RuntimeException("Invalid config file");
				}
			} catch (IOException e) {
				ServerLogger.logError("PluginSettings", e);
				OverwriteConfigSettings();
			} catch (ForbiddenClassException e) { // for xstream package error seperation
				ServerLogger.logError("PluginSettings", e);
			} catch (RuntimeException e) {
				ServerLogger.logError("PluginSettings", e);
			} finally {
				FileUtil.close(inputStream);
			}
		}
	}
	
	private void OverwriteConfigSettings(){
		try{
			if(pluginSettings.getApiToken() != ""){
				pluginSettings.setApiToken(pluginSettings.getApiToken());
			}else{
				pluginSettings.setApiToken("");
			}
	
			if(pluginSettings.getServerURL() != ""){
				pluginSettings.setServerURL(pluginSettings.getServerURL());
			}else{
				pluginSettings.setServerURL("");
			}
	
			pluginSettings.setProxyUsed(false);
			pluginSettings.setProxyHost("");
			pluginSettings.setProxyPassword("");
			pluginSettings.setProxyPort("");
			pluginSettings.setProxyUsername("");
			save();
		}catch (Exception e){}
	}
}
