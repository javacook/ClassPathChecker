package com.javacook.classpathchecker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class PropertyHolder implements PropertyHolderInterface {

	public final String DEFAULT_PROP_FILE_NAME = "cpc.properties";

	public final static String PROP_KEY_CLASS_PATH_KEYS 		= "	";
	public final static String PROP_KEY_ARCHIVE_EXTENSIONS 	= "archiveExtensions";
	public final static String PROP_KEY_OUTPUT_EXTENSIONS 	= "outputExtensions";
	public final static String PROP_KEY_ADDITIONAL_ARTIFACTS 	= "additionalArtifacts";
	public final static String PROP_KEY_EXCLUDE_ARTIFACTS 	= "excludeArtifacts";
	public final static String PROP_KEY_LOG_TO_CONSOLE 		= "logToConsole";
	public final static String PROP_KEY_LOG_FILE_NAME 		= "logFileName";

	private List<String> 	classPathKeys 			= new ArrayList<String>();
	private List<String> 	archiveExtensions 		= new ArrayList<String>();
	private List<String> 	outputExtensions 		= new ArrayList<String>();
	private List<String>	additionalArtifacts		= new ArrayList<String>();
	private List<String>	excludeArtifacts		= new ArrayList<String>();
	private boolean			logToConsole			= true;
	private String			logFileName;
	private boolean			usingDefaults			= true;
	private String			propFileName;

	private Properties properties = new Properties();

	/*-----------------------------------------------------------------------*\
	 * constructors                                                          *
	\*-----------------------------------------------------------------------*/

	private PropertyHolder(String propFileName) throws Exception {
		this.propFileName = (propFileName == null)? DEFAULT_PROP_FILE_NAME : propFileName;
		// In der folgenden Zeile muss es der Slash sein, nicht der FILE_SEPARATOR
		if (!this.propFileName.startsWith("/")) {
			this.propFileName = "/" + this.propFileName;
		}
		try {
			loadProperties(this.propFileName);
		} catch (IOException e) {
			throw e;
		} finally {
			analyseProperties();
		}
	}


	private PropertyHolder() throws Exception {
		this(null);
	}

	/*-----------------------------------------------------------------------*\
	 * singleton pattern                                                     *
	\*-----------------------------------------------------------------------*/

	private static PropertyHolderInterface instance;

	public static synchronized PropertyHolderInterface getInstance() throws Exception {
		if (instance == null) {
			instance = new PropertyHolder();
		}
		return instance;
	}


	/*-----------------------------------------------------------------------*\
	 * public methods                                                        *
	\*-----------------------------------------------------------------------*/

	public List<String> getClassPathKeys() {
		return classPathKeys;
	}

	public List<String> getArchiveExtensions() {
		return archiveExtensions;
	}

	public List<String> getAdditionalArtifacts() {
		return additionalArtifacts;
	}

	public List<String> getExcludeArtifacts() {
		return excludeArtifacts;
	}

	public List<String> getOutputExtensions() {
		return outputExtensions;
	}

	public boolean logToConsole() {
		return logToConsole;
	}

	public void setLogToConsole(boolean logToConsole) {
		this.logToConsole = logToConsole;
	}

	public String getLogFileName() {
		return logFileName;
	}

	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}

	public boolean usingDefaults() {
		return usingDefaults;
	}

	public String getPropFileName() {
		return propFileName;
	}


	public void loadProperties(String propFileName) throws IOException {
		if (propFileName == null) throw new IllegalArgumentException("Argument 'propFileName' is null.");
		this.propFileName = propFileName;
		InputStream propFileStream = ClassPathChecker.class.getResourceAsStream(propFileName);
		if (propFileStream != null) {
			properties.load(propFileStream);
			usingDefaults = false;
		}
	}

	/*-----------------------------------------------------------------------*\
	 * internal methods                                                      *
	\*-----------------------------------------------------------------------*/

	private void analyseProperties() {
		classPathKeys = getProperties(PROP_KEY_CLASS_PATH_KEYS);
		if (classPathKeys.size() == 0) {
			classPathKeys.add("java.class.path");
			classPathKeys.add("sun.boot.class.path");
			classPathKeys.add("java.ext.dirs");
			classPathKeys.add("java.endorsed.dirs");
		}

		archiveExtensions= getProperties(PROP_KEY_ARCHIVE_EXTENSIONS);
		if (archiveExtensions.size() == 0) {
			archiveExtensions.add("jar");
			archiveExtensions.add("zip");
		}

		outputExtensions = getProperties(PROP_KEY_OUTPUT_EXTENSIONS);
		if (outputExtensions.size() == 0) {
			outputExtensions.add("class");
			outputExtensions.add("properties");
			outputExtensions.add("xml");
		}

		excludeArtifacts = getProperties(PROP_KEY_EXCLUDE_ARTIFACTS);

		additionalArtifacts = getProperties(PROP_KEY_ADDITIONAL_ARTIFACTS);

		String logToConsoleStr = properties.getProperty(PROP_KEY_LOG_TO_CONSOLE);
		if (logToConsoleStr != null) logToConsoleStr = logToConsoleStr.trim();
		logToConsole = Boolean.parseBoolean(logToConsoleStr);

		logFileName = properties.getProperty(PROP_KEY_LOG_FILE_NAME);
		if (logFileName != null) logFileName = logFileName.trim();

	}// analyseProperties



	/**
	 * Liefert zu <code>keyPrefix</code> die Menge aller Properties, deren Key mit
	 * <code>keyPrefix</code> startet. So liefert bei
	 * <pre>
	 * outputExtensions[0] = class
	 * outputExtensions[1] = properties
	 * outputExtensions[2] = xml
	 * </pre>
	 * die Methode <code>getProperties("outputExtension")</code> die Liste
	 * <code>["class", "properties", "xml"]</code>.
	 * @param keyPrefix
	 */
	public List<String> getProperties(String keyPrefix) {
		if (properties == null) {
			throw new IllegalArgumentException("The value of 'properties' is null.");
		}

		List<String> result = new ArrayList<String>();

		for (Object key : properties.keySet()) {
			String keyStr = (String)key;
			if (keyStr.startsWith(keyPrefix)) {
				result.add(properties.getProperty(keyStr).trim());
			}
		}
		return result;
	}


	/*-----------------------------------------------------------------------*\
	 * main                                                                  *
	\*-----------------------------------------------------------------------*/

	public static void main(String[] args) throws Exception {
		PropertyHolder propertyHolder = new PropertyHolder();
		System.out.println(propertyHolder.getClassPathKeys());
		System.out.println(propertyHolder.getArchiveExtensions());
		System.out.println(propertyHolder.getOutputExtensions());
	}

}