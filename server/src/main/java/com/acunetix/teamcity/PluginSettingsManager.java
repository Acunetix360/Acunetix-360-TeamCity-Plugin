package com.acunetix.teamcity;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.web.util.CameFromSupport;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class PluginSettingsManager{
	private File configFile;
	private XStream xStream;
	private PluginSettings pluginSettings;
	private CameFromSupport cameFromSupport;
	
	public PluginSettingsManager(@NotNull ServerPaths serverPaths) {
		xStream = new XStream(new DomDriver());
		xStream.processAnnotations(PluginSettings.class);
		xStream.setClassLoader(PluginSettings.class.getClassLoader());
		
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
				pluginSettings = (PluginSettings) xStream.fromXML(inputStream);
			} catch (IOException e) {
				ServerLogger.logError("PluginSettings", e);
			} finally {
				FileUtil.close(inputStream);
			}
		}
	}
}
