package com.acunetix.teamcity;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.CameFromSupport;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class PluginSettingsTab extends SimpleCustomTab {

	private PluginSettingsManager pluginSettingsManager;

	public PluginSettingsTab(@NotNull final SBuildServer server,
			@NotNull final WebControllerManager webControllerManager,
			@NotNull final PluginSettingsManager pluginSettingsManager,
			@NotNull final ScanRunType scanRunType) {
		super(webControllerManager, PlaceId.ADMIN_SERVER_CONFIGURATION_TAB,
				"acunetixPluginSettings", scanRunType.getPluginSettingsJspFilePath(),
				"Acunetix 360");

		this.pluginSettingsManager = pluginSettingsManager;

		setPosition(PositionConstraint.after("serverConfigGeneral"));
		register();

		webControllerManager.registerController(ScanConstants.PLUGIN_SETTINGS_RELATIVE_URL,
				new PluginSettingsController(server, pluginSettingsManager));
	}


	@Override
	public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
		super.fillModel(model, request);
		ServerLogger.logInfo("PluginSettingsTab", "Loading plugin settings...");

		model.put("pluginSettingsManager", pluginSettingsManager);

		CameFromSupport cameFromSupport = new CameFromSupport();
		cameFromSupport.setTitleFromRequest(request, "Cancel");
		cameFromSupport.setUrlFromRequest(request, "/admin/admin.html?item=projects");
		pluginSettingsManager.setCameFromSupport(cameFromSupport);
		ServerLogger.logInfo("PluginSettingsTab", "Plugin settings loaded successfully.");
	}
}
