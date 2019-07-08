package com.acunetix.teamcity;

import jetbrains.buildServer.log.Loggers;

public class ServerLogger{
	public static void logInfo(String message) {
		Loggers.SERVER.info(logMessage(message));
	}
	
	public static void logInfo(String source, String message) {
		Loggers.SERVER.info(logMessage(source, message));
	}
	
	public static void logWarn(String message) {
		Loggers.SERVER.warn(logMessage(message));
	}
	
	public static void logWarn(String source, String message) {
		Loggers.SERVER.warn(logMessage(source, message));
	}
	
	public static void logError(Exception ex) {
		if (ex != null) {
			logError(ex.toString());
		}
	}
	
	public static void logError(String source, Exception ex) {
		if (ex != null) {
			logError(source, ex.toString());
		}
	}
	
	public static void logError(String message) {
		Loggers.SERVER.error(logMessage(message));
	}
	
	public static void logError(String source, String message) {
		Loggers.SERVER.error(logMessage(source, message));
	}
	
	public static String logMessage(String source, String msg) {
		return "[acunetix]::" + source + ": " + msg;
	}
	
	public static String logMessage(String msg) {
		return "[acunetix]:: " + msg;
	}
}
