package com.acunetix.teamcity;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.ViewLogTab;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public abstract class LogTabBase extends ViewLogTab{
	
	public final SBuildServer server;
	
	public LogTabBase(
			@NotNull final String tabTitle,
			@NotNull final String tabCode,
			@NotNull final PagePlaces pagePlaces,
			@NotNull final SBuildServer server) {
		super("", "", pagePlaces, server);
		this.server = server;
	}
	
	@Override
	public boolean isAvailable(@NotNull HttpServletRequest request, @NotNull SBuild build) {
		SBuild sbuild = this.getBuild(request);
		if (sbuild != null) {
			boolean hasStep = hasStep(sbuild);
			return hasStep;
		}
		
		return false;
	}
	
	protected boolean hasStep(@NotNull SBuild sbuild) {
		if (getBuildRunner(sbuild) != null) {
			return true;
		}
		
		return false;
	}
	
	protected Map<String, String> getProperties(@NotNull SBuild sbuild) {
		ServerLogger.logInfo("AcunetixReport", "Getting runner properties...");
		SBuildRunnerDescriptor runner = getBuildRunner(sbuild);
		Map<String, String> properties = runner.getParameters();

		return properties;
	}
	
	protected SBuildRunnerDescriptor getBuildRunner(@NotNull SBuild sbuild) {
		List<SBuildRunnerDescriptor> buildRunners = sbuild.getBuildType().getBuildRunners();
		
		for (SBuildRunnerDescriptor runner : buildRunners) {
			if (runner.getType().equalsIgnoreCase(ScanConstants.SCAN_RUN_TYPE)) {
				return runner;
			}
		}
		
		return null;
	}
}
