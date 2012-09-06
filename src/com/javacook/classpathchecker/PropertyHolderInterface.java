package com.javacook.classpathchecker;

import java.util.List;

public interface PropertyHolderInterface {

	List<String> getClassPathKeys();

	List<String> getArchiveExtensions();

	List<String> getOutputExtensions();

	List<String> getAdditionalArtifacts();

	List<String> getExcludeArtifacts();

	boolean logToConsole();

	void setLogToConsole(boolean logToConsole);

	String getLogFileName();

	void setLogFileName(String logFileName);

	boolean usingDefaults();

	void loadProperties(String propFileName) throws Exception;

	String getPropFileName();

}
