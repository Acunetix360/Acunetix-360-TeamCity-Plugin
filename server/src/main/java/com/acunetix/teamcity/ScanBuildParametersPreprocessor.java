package com.acunetix.teamcity;

import jetbrains.buildServer.serverSide.ParametersPreprocessor;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.WebLinks;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.vcs.*;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;

public class ScanBuildParametersPreprocessor implements ParametersPreprocessor{
	private final Map<String, String> parameters = new HashMap<>();
	private final WebLinks webLinks;
	private final PluginSettingsManager pluginSettingsManager;
	
	public ScanBuildParametersPreprocessor(@NotNull final WebLinks webLinks,
	                                       @NotNull final PluginSettingsManager pluginSettingsManager) {
		this.webLinks = webLinks;
		this.pluginSettingsManager = pluginSettingsManager;
	}
	
	@Override
	public void fixRunBuildParameters(@NotNull SRunningBuild build,
	                                  @NotNull Map<String, String> runParameters,
	                                  @NotNull Map<String, String> buildParams) {
		ServerLogger.logInfo("ScanBuildParametersPreprocessor", "Adding build parameters...");
		addParameters(build);
		runParameters.putAll(parameters);
		ServerLogger.logInfo("ScanBuildParametersPreprocessor", "Build parameters added successfully.");
	}
	
	private void addParameters(SBuild build) {
		parameters.clear();
		
		boolean buildHasChange = addBuildParameters(build);
		final List<SVcsModification> containingChanges = build.getContainingChanges();
		if (buildHasChange) {
			addVCSParameters(containingChanges);
		}
	}
	
	private boolean addBuildParameters(SBuild build) {
		ServerLogger.logInfo("ScanBuildParametersPreprocessor", "Getting plugin settings...");
		final PluginSettings pluginSettings = pluginSettingsManager.getPluginSettings();
		
		ServerLogger.logInfo("ScanBuildParametersPreprocessor", "Adding API settings...");
		parameters.put(ApiRequestBase.API_URL_Literal, pluginSettings.getServerURL());
		parameters.put(ApiRequestBase.API_TOKEN_Literal, pluginSettings.getApiToken());

		if(pluginSettings.getProxyUsed()){
			parameters.put(ApiRequestBase.PROXY_Used,String.valueOf(pluginSettings.getProxyUsed()));
			parameters.put(ApiRequestBase.PROXY_Host,pluginSettings.getProxyHost());
			parameters.put(ApiRequestBase.PROXY_Port,String.valueOf(pluginSettings.getProxyPort()));
			parameters.put(ApiRequestBase.PROXY_Username,pluginSettings.getProxyUsername());
			parameters.put(ApiRequestBase.PROXY_Password_ENCRYPTED,pluginSettings.getEncryptedProxyPassword());
			parameters.put(ApiRequestBase.PROXY_Password,pluginSettings.getProxyPassword());
		}
		
		ServerLogger.logInfo("ScanBuildParametersPreprocessor", "Adding CI parameters...");
		final long buildId = build.getBuildId();
		parameters.put(VCSCommit.BUILD_ID_LITERAL, String.valueOf(buildId));
		
		final String buildConfigurationName = build.getBuildTypeName();
		parameters.put(VCSCommit.BUILD_CONFIGURATION_NAME_LITERAL, buildConfigurationName);
		
		final String buildURL = webLinks.getViewLogUrl(build);
		parameters.put(VCSCommit.BUILD_URL_LITERAL, buildURL);
		
		final boolean buildHasChange = !build.getContainingChanges().isEmpty();
		parameters.put(VCSCommit.BUILD_HAS_CHANGE, String.valueOf(buildHasChange));
		
		//this date will be overwritten if we obtain date from vcs in addVCSParameters method.
		final Date ciDate = new Date();
		SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		String dateString = originalFormat.format(ciDate);
		parameters.put(VCSCommit.VCS_Timestamp, dateString);
		
		return buildHasChange;
	}
	
	
	private void addVCSParameters(List<SVcsModification> containingChanges) {
		ServerLogger.logInfo("ScanBuildParametersPreprocessor", "Adding VCS parameters...");
		SVcsModification containingChange = (SVcsModification) Collections.max(containingChanges, new SVcsModificationComparator());
		
		final String versionControlName = containingChange.getVersionControlName();
		if (!StringUtil.isEmptyOrSpaces(versionControlName)) {
			parameters.put(VCSCommit.VCS_NAME_LITERAL, versionControlName);
		}
		final String vcsBranchVersion = containingChange.getVersion();
		if (!StringUtil.isEmptyOrSpaces(vcsBranchVersion)) {
			parameters.put(VCSCommit.VCS_VERSION_LITERAL, vcsBranchVersion);
		}
		
		final String committerUsername = containingChange.getUserName();
		if (!StringUtil.isEmptyOrSpaces(committerUsername)) {
			parameters.put(VCSCommit.COMMITTER_USERNAME_LITERAL, committerUsername);
		}
		
		final Date vcsDate = containingChange.getVcsDate();
		SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		String dateString = originalFormat.format(vcsDate);
		parameters.put(VCSCommit.VCS_Timestamp, dateString);
	}
	
	private class SVcsModificationComparator implements Comparator{
		public int compare(Object o1, Object o2) {
			SVcsModification m1 = (SVcsModification) o1;
			SVcsModification m2 = (SVcsModification) o2;
			
			Date m1Date = m1.getVcsDate();
			Date m2Date = m2.getVcsDate();
			if (m1Date.compareTo(m2Date) == 0)
				return 0;
			else if (m1Date.compareTo(m2Date) > 0)
				return 1;
			else
				return -1;
		}
	}
}




