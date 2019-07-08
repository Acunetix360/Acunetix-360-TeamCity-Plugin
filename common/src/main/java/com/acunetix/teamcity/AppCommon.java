package com.acunetix.teamcity;

import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.HttpResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class AppCommon{
	
	public void SetResult(Path reportingRoot, Object results) throws IOException {
		Path reportPath = Paths.get(reportingRoot.toString(), ScanConstants.SCAN_REPORTING_FILENAME);
		FileOutputStream fileOut = new FileOutputStream(reportPath.toString());
		ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
		objectOut.writeObject(results);
		objectOut.close();
		
		return;
	}
	
	//Input stream can be get by getInputStream method of BuildArtifact such as:
	//InputStream inputStream = artifact.getInputStream(); artifact's type is BuildArtifact
	public Object GetResult(InputStream inputStream) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		Object result = objectInputStream.readObject();
		objectInputStream.close();
		
		return result;
	}
	
	public static boolean IsUrlValid(String url) {
		String[] schemes = {"http", "https"}; // DEFAULT schemes = "http", "https", "ftp"
		UrlValidator urlValidator = new UrlValidator(schemes, UrlValidator.ALLOW_LOCAL_URLS);
		
		if (urlValidator.isValid(url)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean IsGUIDValid(String guid) {
		try {
			if (guid == null) {
				return false;
			}
			UUID uuid = UUID.fromString(
					//fixes the guid if it doesn't contain hypens
					guid.replaceFirst(
							"(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
			return true;
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}
	
	public static URL GetBaseURL(String url) throws MalformedURLException {
		return new URL(new URL(url), "/");
	}
	
	public static String MapToQueryString(Map<String, String> map) {
		StringBuilder stringBuilder = new StringBuilder();
		
		for (String key : map.keySet()) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append("&");
			}
			String value = map.get(key);
			try {
				stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
				stringBuilder.append("=");
				stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("This method requires UTF-8 encoding support", e);
			}
		}
		
		return stringBuilder.toString();
	}
	
	public static Map<String, String> QueryStringToMap(String input) {
		Map<String, String> map = new HashMap<String, String>();
		
		String[] nameValuePairs = input.split("&");
		for (String nameValuePair : nameValuePairs) {
			String[] nameValue = nameValuePair.split("=");
			try {
				map.put(URLDecoder.decode(nameValue[0], "UTF-8"), nameValue.length > 1 ? URLDecoder.decode(
						nameValue[1], "UTF-8") : "");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("This method requires UTF-8 encoding support", e);
			}
		}
		
		return map;
	}
	
	public static Object ParseJsonValue(String Data, String key) throws ParseException {
		JSONParser parser = new JSONParser();
		Object parsedData = parser.parse(Data);
		Object value;
		if (parsedData instanceof JSONArray) {
			JSONArray array = (JSONArray) parsedData;
			JSONObject object = (JSONObject) array.get(0);
			value =  object.get(key);
		} else {
			JSONObject obj = (JSONObject) parsedData;
			value =  obj.get(key);
		}
		return value;
	}

	public static String ParseResponseToString(HttpResponse response) throws IOException {
		return IOUtils.toString(response.getEntity().getContent());
	}
}
