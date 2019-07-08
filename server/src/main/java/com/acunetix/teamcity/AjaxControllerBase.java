package com.acunetix.teamcity;

import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public abstract class AjaxControllerBase extends BaseFormXmlController{
	public AjaxControllerBase(@NotNull SBuildServer server) {
		super(server);
	}
	
	protected Map<String, String> getParameters(HttpServletRequest request) {
		Map<String, String> parameters = new HashMap<>();
		Enumeration<String> parameterNames = request.getParameterNames();
		
		while (parameterNames.hasMoreElements()) {
			String key = parameterNames.nextElement();
			String value = request.getParameter(key);
			parameters.put(key, value);
		}
		
		return parameters;
	}
}
