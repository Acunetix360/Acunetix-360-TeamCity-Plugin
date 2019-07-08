package com.acunetix.teamcity;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import org.jetbrains.annotations.NotNull;


public class ScanBuildRunner extends AbstractBuildRunner{
	public ScanBuildRunner(@NotNull ArtifactsWatcher artifactsWatcher) {
		super(artifactsWatcher);
	}
	
	@NotNull
	public BuildProcess createBuildProcess(@NotNull final AgentRunningBuild build,
	                                       @NotNull final BuildRunnerContext context) throws RunBuildException {
		BuildProgressLogger progresslogger = build.getBuildLogger();
		progresslogger.message("Creating build process.");
		BuildProcess process;
		try {
			process = new ScanBuildProcessAdapter(artifactsWatcher, build, context, progresslogger);
		} catch (RunBuildException ex) {
			progresslogger.buildFailureDescription("Failed to create build process.");
			throw ex;
		}
		
		return process;
	}
	
	@NotNull
	public AgentBuildRunnerInfo getRunnerInfo() {
		return new ScanBuildRunnerInfo();
	}
}
