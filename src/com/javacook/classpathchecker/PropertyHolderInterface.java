package com.javacook.classpathchecker;

import java.util.List;

public interface PropertyHolderInterface {

	List<String> getClassPathKeys();

	List<String> getArchiveExtensions();

	List<String> getOutputExtensions();

	List<String> getAdditionalArtifacts();

	boolean logToConsole();

	String getLogFileName();

	boolean usingDefaults();

	String getPropFileName();

}