package com.acunetix.teamcity;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.StringUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class ScanPropertiesProcessor implements PropertiesProcessor{
	
	@Override
	public Collection<InvalidProperty> process(Map<String, String> properties) {
		
		Collection<InvalidProperty> result = new HashSet<>();
		ScanType scanType = null;
		
		//Scan Type
		try {
			scanType = ScanType.valueOf(properties.get(ScanRequest.SCAN_TYPE_Literal));
		} catch (Exception ex) {
			result.add(new InvalidProperty(
					ScanRequest.SCAN_TYPE_Literal,
					"The parameter 'Scan Type' must be specified."));
		}
		
		//WebsiteId
		boolean isWebsiteIdEmpty = StringUtil.isEmptyOrSpaces(properties.get(ScanRequest.WEBSITE_ID_Literal));
		if (isWebsiteIdEmpty) {
			result.add(new InvalidProperty(
					ScanRequest.WEBSITE_ID_Literal,
					"The parameter 'Website URL' must be specified."));
		}
		boolean isWebsiteIdValid = AppCommon.IsGUIDValid(properties.get(ScanRequest.WEBSITE_ID_Literal));
		if (!isWebsiteIdEmpty && !isWebsiteIdValid) {
			result.add(new InvalidProperty(
					ScanRequest.WEBSITE_ID_Literal,
					"The parameter 'Website URL' is invalid"));
		}
		
		//ProfileId
		boolean isProfileIdEmpty = StringUtil.isEmptyOrSpaces(properties.get(ScanRequest.PROFILE_ID_Literal));
		boolean isProfileIdRequired = scanType != ScanType.FullWithPrimaryProfile;
		if (isProfileIdEmpty && isProfileIdRequired) {
			result.add(new InvalidProperty(
					ScanRequest.PROFILE_ID_Literal,
					"The parameter 'Profile Name' must be specified."));
		}
		
		boolean isProfileIdValid = AppCommon.IsGUIDValid(properties.get(ScanRequest.PROFILE_ID_Literal));
		if (!isProfileIdEmpty && isProfileIdRequired && !isProfileIdValid) {
			result.add(new InvalidProperty(
					ScanRequest.PROFILE_ID_Literal,
					"The parameter 'Profile Name' is invalid"));
		}
		
		return result;
	}
}