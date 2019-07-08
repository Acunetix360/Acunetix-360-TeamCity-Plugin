package com.acunetix.teamcity;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.util.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PluginSettingsController extends AjaxControllerBase{
	
	private PluginSettingsManager pluginSettingsManager;
	
	public PluginSettingsController(@NotNull final SBuildServer server,
	                                final PluginSettingsManager pluginSettingsManager) {
		super(server);
		this.pluginSettingsManager = pluginSettingsManager;
	}
	
	@Override
	protected ModelAndView doGet(HttpServletRequest request, HttpServletResponse response) {
		return null;
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response, Element xmlResponse) {
		
		ActionErrors errors = validate(request);
		if (errors.hasErrors()) {
			ServerLogger.logWarn("PluginSettingsController", errors.getErrors().size() + " Errors:");
			for (ActionErrors.Error error : errors.getErrors()) {
				ServerLogger.logWarn("PluginSettingsController", error.getMessage());
			}
			errors.serialize(xmlResponse);
			ServerLogger.logWarn("PluginSettingsController", "Parameters are not valid.");
			return;
		}
		
		setPluginSettings(request);
		
		getOrCreateMessages(request).addMessage("settingsSaved", "Settings saved successfully.");
	}
	
	private ActionErrors validate(final HttpServletRequest request) {
		ServerLogger.logInfo("PluginSettingsController", "Validating parameters...");
		ActionErrors errors = new ActionErrors();
		
		final String serverURL = request.getParameter("acunetixServerURL");
		if (StringUtil.isEmptyOrSpaces(serverURL)) {
			errors.addError("acunetixApiURL", "The parameter 'Acunetix 360 Server URL' must be specified.");
			ServerLogger.logWarn("PluginSettingsController", "Server URL is Empty.");
		}
		
		if (!StringUtil.isEmptyOrSpaces(serverURL) && !AppCommon.IsUrlValid(serverURL)) {
			errors.addError("acunetixApiURL", "The parameter 'Acunetix 360 Server URL' is invalid.");
			ServerLogger.logWarn("PluginSettingsController", "Server URL is invalid.");
		}
		
		final String apiToken = RSACipher.decryptWebRequestData(request.getParameter("encryptedAcunetixApiToken"));
		if (StringUtil.isEmptyOrSpaces(apiToken)) {
			errors.addError("acunetixApiToken", "The parameter 'API Token' must be specified.");
			ServerLogger.logWarn("PluginSettingsController", "API token is empty.");
		}
		
		return errors;
	}
	
	private void setPluginSettings(HttpServletRequest request) {
		final PluginSettings settings = new PluginSettings();
		ServerLogger.logInfo("PluginSettingsController", "Saving parameters...");
		
		String serverURL = request.getParameter("acunetixServerURL");
		String apiToken = RSACipher.decryptWebRequestData(request.getParameter("encryptedAcunetixApiToken"));
		String apiTokenInitial = request.getParameter("acunetixApiTokenInitialValue");
		
		settings.setServerURL(serverURL);
		if (!apiToken.equals(apiTokenInitial)) {
			settings.setApiToken(apiToken);
		} else {
			settings.setApiToken(pluginSettingsManager.getPluginSettings().getApiToken());
		}
		
		pluginSettingsManager.setPluginSettings(settings);
		pluginSettingsManager.save();
		ServerLogger.logInfo("PluginSettingsController", "Plugin parameters saved successfully.");
	}
}
