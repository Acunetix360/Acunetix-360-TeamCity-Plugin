package com.acunetix.teamcity;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class ScanTestController extends AjaxControllerBase{
	private final WebControllerManager webControllerManager;
	
	public ScanTestController(@NotNull final SBuildServer server,
	                          @NotNull final WebControllerManager webControllerManager) {
		super(server);
		this.webControllerManager = webControllerManager;
		webControllerManager.registerController(ScanConstants.TEST_CONTROLLER_RELATIVE_URL, this);
	}
	
	@Override
	@Nullable
	protected ModelAndView doGet(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response) {
		return null;
	}
	
	@Override
	protected void doPost(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull Element element) {
		Map<String, String> parameters = getParameters(httpServletRequest);
		String decryptedToken = RSACipher.decryptWebRequestData(httpServletRequest.getParameter(ApiRequestBase.API_TOKEN_Literal));
		parameters.put(ApiRequestBase.API_TOKEN_Literal, decryptedToken);

		try {
			ServerLogger.logInfo("ScanTestController", "Testing the Acunetix 360 connection.");
			WebsiteModelRequest websiteModelRequest = new WebsiteModelRequest(parameters);
			websiteModelRequest.requestPluginWebSiteModels();
			int httpStatusCode=websiteModelRequest.getResponseStatusCode();
			element.addContent(new Element("httpStatusCode").setText(String.valueOf(httpStatusCode)));
			
			if (httpStatusCode == 200) {
				ServerLogger.logInfo("ScanTestController", "Acunetix 360 test connection succeeded.");
			} else {
				ServerLogger.logError("ScanTestController", "Acunetix 360 rejected the request. HTTP status code: " + String.valueOf(httpStatusCode));
			}
			
		} catch (Exception e) {
			ServerLogger.logError("WebsiteModelController", e);
			element.removeContent();
			element.addContent(new Element("httpStatusCode").setText("0"));
		}
	}
}
