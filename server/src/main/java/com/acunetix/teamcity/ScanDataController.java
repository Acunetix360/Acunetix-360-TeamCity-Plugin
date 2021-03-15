package com.acunetix.teamcity;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.util.Map;

public class ScanDataController extends AjaxControllerBase{
	private DataStorage storage;
	private final PluginSettingsManager pluginSettingsManager;
	
	public ScanDataController(@NotNull final SBuildServer server,
	                          @NotNull final WebControllerManager webControllerManager,
							  @NotNull final PluginSettingsManager pluginSettingsManager) {
		super(server);
		storage = new DataStorage(server);
		this.pluginSettingsManager = pluginSettingsManager;
		webControllerManager.registerController(ScanConstants.SCAN_DATA_CONTROLLER_RELATIVE_URL, this);
	}
	
	@Override
	@Nullable
	protected ModelAndView doGet(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response) {
		return null;
	}
	
	@Override
	protected void doPost(@NotNull HttpServletRequest httpServletRequest,
	                      @NotNull HttpServletResponse httpServletResponse,
	                      @NotNull Element element) {
		try {
			
			Map<String, String> parameters = getParameters(httpServletRequest);
			final PluginSettings pluginSettings = pluginSettingsManager.getPluginSettings();

			if(pluginSettings.getProxyUsed()){
				parameters.put(ApiRequestBase.PROXY_Used,String.valueOf(pluginSettings.getProxyUsed()));
				parameters.put(ApiRequestBase.PROXY_Host,pluginSettings.getProxyHost());
				parameters.put(ApiRequestBase.PROXY_Port,String.valueOf(pluginSettings.getProxyPort()));
				parameters.put(ApiRequestBase.PROXY_Username,pluginSettings.getProxyUsername());
				parameters.put(ApiRequestBase.PROXY_Password_ENCRYPTED,pluginSettings.getEncryptedProxyPassword());
			}

			//Stores scanRequestResult for AcunetixReport. Now scan log tab can access scan report from NC
			ScanRequestResult scanRequestResult = storage.StoreScanRequestResult(parameters);
			
			element.addContent(new Element("ScanTaskID").setText(scanRequestResult.getScanTaskID()));
		} catch (MalformedURLException e) {
			ServerLogger.logError("ScanDataController", "Failed to Store the build data.");
			httpServletResponse.setStatus(400);
		}
	}
}