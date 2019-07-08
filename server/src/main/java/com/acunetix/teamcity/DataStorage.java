package com.acunetix.teamcity;

import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;

import java.net.MalformedURLException;
import java.util.Map;

public class DataStorage{
	private SBuildServer server;
	private final String ScanRequestStoragePrefix = "NCScanRequestStorageId:";
	
	public DataStorage(SBuildServer server) {
		this.server = server;
	}
	
	public ScanRequestResult StoreScanRequestResult(Map<String, String> runnerParams) throws MalformedURLException {
		ServerLogger.logInfo("ScanDataController", "Storing the build data...");
		ScanRequestResult scanRequestResult = new ScanRequestResult(runnerParams);
		final SBuild build = server.findBuildInstanceById(scanRequestResult.getBuildID());
		SBuildType buildType = build.getBuildType();
		
		CustomDataStorage scanRequestStorage = buildType.getCustomDataStorage(
				ScanRequestStoragePrefix + scanRequestResult.getBuildID());
		scanRequestStorage.putValues(scanRequestResult.Parameters);
		scanRequestStorage.flush();
		ServerLogger.logInfo("ScanDataController", "Storing the build data succeeded.");
		
		return scanRequestResult;
	}
	
	public ScanRequestResult GetScanRequestResult(long buildID, Map<String, String> runnerParams) throws MalformedURLException {
		ServerLogger.logInfo("AcunetixReport", "Getting the scan request result...");
		
		final SBuild build = server.findBuildInstanceById(buildID);
		SBuildType buildType = build.getBuildType();
		CustomDataStorage scanRequestStorage = buildType.getCustomDataStorage(ScanRequestStoragePrefix + buildID);
		
		Map<String, String> scanRequestResultParameters = scanRequestStorage.getValues();
		scanRequestResultParameters.putAll(runnerParams);
		ScanRequestResult scanRequestResult = new ScanRequestResult(scanRequestResultParameters);
		
		ServerLogger.logInfo("AcunetixReport", "Scan request result retrieved successfully.");
		return scanRequestResult;
	}
}
