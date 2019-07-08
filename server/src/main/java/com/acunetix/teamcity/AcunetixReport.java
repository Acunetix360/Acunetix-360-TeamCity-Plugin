package com.acunetix.teamcity;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class AcunetixReport extends LogTabBase{
	
	private PluginSettingsManager pluginSettingsManager;
	
	public AcunetixReport(@NotNull PagePlaces pagePlaces, @NotNull SBuildServer server, @NotNull PluginDescriptor descriptor, @NotNull PluginSettingsManager pluginSettingsManager) {
		super("Acunetix 360 Report", "AcunetixReportTab", pagePlaces, server);
		setTabTitle(getTitle());
		setPluginName(getClass().getSimpleName());
		setIncludeUrl(descriptor.getPluginResourcesPath("acunetixReport.jsp"));
		this.pluginSettingsManager = pluginSettingsManager;
		//addCssFile(descriptor.getPluginResourcesPath("css/style.css"));
		//addJsFile(descriptor.getPluginResourcesPath("js/script.js"))
	}
	
	@Override
	protected void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request, @NotNull SBuild build) {
		try {
			Map<String, String> runnerProperties = getProperties(build);

			DataStorage dataStorage = new DataStorage(server);
			ScanRequestResult scanRequestResult = dataStorage.GetScanRequestResult(build.getBuildId(), runnerProperties);
			
			ServerLogger.logInfo("AcunetixReport", "Requesting the report...");

			String apiToken = pluginSettingsManager.getPluginSettings().getApiToken();

			ScanReport report = scanRequestResult.getReport(apiToken);
			
			ServerLogger.logInfo("AcunetixReport", "Parsing the report...");
			model.put("content", report.getContent());
			model.put("isReportGenerated", String.valueOf(report.isReportGenerated()));
			model.put("hasError", "false");
			model.put("errorMessage", "''");
			
			ServerLogger.logInfo("AcunetixReport", "Getting the report info succeeded.");
		} catch (Exception exception) {
			ServerLogger.logError("AcunetixReport", exception);
			
			model.put("isReportGenerated", "false");
			model.put("content", "''");
			model.put("hasError", "true");
			model.put("errorMessage", "An error occurred during the requesting scan report.");
		}
	}
	
	protected String getTitle() {
		return "Acunetix 360 Report";
	}
	
	protected String getJspName() {
		return "acunetixReport.jsp";
	}
}
