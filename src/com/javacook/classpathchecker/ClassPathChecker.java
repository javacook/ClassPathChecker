package com.javacook.classpathchecker;

import static com.javacook.util.StringUtils.truncPrefix;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.javacook.util.FileUtils;
import com.javacook.util.JavaCookLogger;
import com.javacook.util.MultiHashMap;
import com.javacook.util.PathSet;
import com.javacook.util.StringUtils;

public class ClassPathChecker {

	private final static String FILE_SEPARATOR = System.getProperty("file.separator"); // Bei Windows Backslash, ansonsten Slash:

	private final static String CRLF = System.getProperty("line.separator");

	/**
	 * Zum Loggen von Ausgaben
	 */
	private JavaCookLogger logger;


	/**
	 * Menge aller Datei-Pfade zu Jars oder Verzeichnissen, die untersucht werden. Diese
	 * werden als Key gespeicht. Der Value enthaelt die Information, ob ein Zugriff moeglich ist.
	 */
	private Map<String, Boolean> artifactPaths = new LinkedHashMap<String, Boolean>();


	/**
	 * Menge aller Archive (jar, zip), die untersucht werden. Dies sind nur diejenige, die
	 * auch existieren bzw. auf die ein Zugriff moeglich ist.
	 */
	private List<String> archives = new ArrayList<String>();


	/**
	 * Hier werden alle Resourcen mit ihrem Vorkommen gesammelt
	 */
	private final MultiHashMap<String, String> resourceToOccurence = new MultiHashMap<String, String>();


	/**
	 * Der in den System-Properties vorhandene ClassPath ist je nach BS mit unterschiedlichen
	 * Trennsymvbolen zerlegt (Bei Unix Doppelpunkt, bei Windows Semikolon)
	 */
	private final static String PATH_SEPARATOR = System.getProperty("path.separator");


	/**
	 * Enthaelt die geladenen Properties, bzw. die Defaultwerte, falls keine zur Verfuegung.
	 */
	private PropertyHolderInterface propertyHolder;

	private FileUtils fileUtils = new FileUtils();


	/*-----------------------------------------------------------------------*\
	 * constructors                                                          *
	\*-----------------------------------------------------------------------*/

	public ClassPathChecker() throws Exception {
		this(PropertyHolder.getInstance());
	}


	public ClassPathChecker(PropertyHolderInterface propertyHolder) throws Exception {
		try {
			this.propertyHolder = propertyHolder;
			if (this.propertyHolder == null) {
				logger = new JavaCookLogger();
				throw new IllegalArgumentException("Argument 'propertyHolder' is null.");
			}
			else {
				logger = new JavaCookLogger(propertyHolder.logToConsole(), propertyHolder.getLogFileName());
				if (propertyHolder.usingDefaults()) {
					logger.log("No property file found at '"  + propertyHolder.getPropFileName() + "' => using defaults.");
				}
				else {
					logger.log("Properties loaded successfully from file '"  + propertyHolder.getPropFileName() + "'");
				}
			}
		} catch (Exception e) {
			if (logger != null) {
				logger.log("Exception occured while initializing ClassPathChecker:");
				logger.log(e.toString());
			}
			else {
				e.printStackTrace();
				throw e;
			}
		}
	}


	/*-----------------------------------------------------------------------*\
	 * public methods                                                        *
	\*-----------------------------------------------------------------------*/

	/**
	 * Hier geht los...
	 * @throws ZipException
	 * @throws IOException
	 */
	public ClassPathChecker run() {
		try {
			logger.log("Start searching for duplicate class path entries...");
			handleClassPaths();
			logger.log("...Success.");
		} catch (Exception e) {
			logger.log("...Failed!");
			logger.log(e.toString());
		}
		return this;
	}

	/**
	 * Erzeugt eine Ausgabe in Form einer XML-Datei
	 * @return
	 */
	public XMLReport xmlReport() {
		StringBuilder output = new StringBuilder();
		output.append("<report>").append(CRLF);

		output.append("\t<artifacts>").append(CRLF);
		for (String path : artifactPaths.keySet()) {
			output.append("\t\t<path accessable=\"" + artifactPaths.get(path) + "\">").append(path).append("</path>").append(CRLF);
		}
		output.append("\t</artifacts>").append(CRLF);

		Collections.sort(archives);
		output.append("\t<archives>").append(CRLF);
		for (String path : archives) {
			output.append("\t\t<path>").append(path).append("</path>").append(CRLF);
		}
		output.append("\t</archives>").append(CRLF);

		output.append("\t<problems>").append(CRLF);
		for (String qualifiedClassName : resourceToOccurence.keySet()) {
			// Filtern:
			if (!hasOutputExtension(qualifiedClassName)) continue;

			Collection<String> occurences = resourceToOccurence.get(qualifiedClassName);
			if (occurences.size() > 1) {
				output.append("\t\t<resource path=\""+qualifiedClassName+"\">").append(CRLF);

				for (String occurence : occurences) {
					output.append("\t\t\t<occurence>"+occurence+"</occurence>").append(CRLF);
				}
				output.append("\t\t</resource>").append(CRLF);
			}
		}
		output.append("\t</problems>").append(CRLF);
		output.append("</report>").append(CRLF).append(CRLF);

		output.append("<logs>").append(CRLF);
		for (String logEntry : logger.getLogEntries()) {
			output.append("\t<entry>").append(logEntry).append("</entry>").append(CRLF);
		}
		output.append("</logs>");
		return new XMLReport(output.toString());
	}


	/*-----------------------------------------------------------------------*\
	 * internal methods                                                      *
	\*-----------------------------------------------------------------------*/

	protected void handleClassPaths() throws Exception {
		if (propertyHolder == null) {
			throw new IllegalStateException("Cannot process without properties.");
		}

		PathSet artifactPathSet = new PathSet();
		List<String> classPathKeys = propertyHolder.getClassPathKeys();

		if (classPathKeys != null) {
			for (String classPathKey: classPathKeys) {
				String classPathChain = System.getProperty(classPathKey);
				if (classPathChain == null) {
					logger.log("WARNING: There is no System property available for key '" + classPathKey + "'");
				}
				else {
					artifactPathSet.addAll(splitPathChain(classPathChain));
				}
			}
		}

		List<String> additionalArtifacts = propertyHolder.getAdditionalArtifacts();

		if (additionalArtifacts != null) {
			artifactPathSet.addAll(additionalArtifacts);
		}

		// Speziellen Pfad hinzufuegen (so bekommt man das Lib im WebSphere heraus)
		CodeSource codeSource = ClassPathChecker.class.getProtectionDomain().getCodeSource();
		if (codeSource != null) {
			URL location = codeSource.getLocation();
			if (location != null) {
				artifactPathSet.add(location.getFile());
			}
		}

		// Alle Pfad nach Zugriff untersuchen und dann durchstoebern...
		for (String path : artifactPathSet) {
			if (new File(path).exists()) {
				artifactPaths.put(path, true);
				collect(path);
			}
			else {
				artifactPaths.put(path, false);
				logger.log("WARNING: Class path entry '" + path + "' is not availiable.");
			}
		}
	}


	/**
	 * Wandert rekursiv das Verzeichnis <code>basePath</code> durch und sucht dort nach Klassen (Endung .class)
	 * und nach Archiven (Endung .jar oder .zip) und fuegt die Klassen-Funde sukzessive der Map
	 * <code>classToOccurence</code> hinzu.
	 * @param basePath kann ein Verzeichnis sein aber auch eine einzelne Datei sein (.jar, .class)
	 */
	protected void collect(final String basePath) throws Exception {

		fileUtils.browseDirTree(basePath, new FileUtils.CallBack() {
			public void action(String filePath) throws Exception {
				// z.B. actualPath = "/Volumes/Braeburn/Entwicklung/Software/Sonstiges/ClassPathChecker/bin/com/javacook/classpathchecker/ClassPathChecker.class"
				if (hasArchiveExtension(filePath)) { // z.B. ".jar"
					collectArchive(filePath);
				} else {
					// Abschneiden des basePath ("/Volumes/Braeburn/Entwicklung/Software/Sonstiges/ClassPathChecker/bin") vorne:
					String temp = truncPrefix(filePath, basePath, true);
					temp = truncPrefix(temp, FILE_SEPARATOR);
					resourceToOccurence.put(temp, basePath);
				}
			}
		});
	}// collect


	/**
	 * Zerlegen eines Archiv-Files (Jar) und einsammeln der dort enthaltenen Resourcen.
	 * Sie werden sukzessive in der Map <code>resourceToOccurence</code> abgelegt.
	 * @param archivePath abs. Pfad eines jar-Files
	 */
	protected void collectArchive(String archivePath) throws ZipException, IOException {
		archives.add(archivePath);
		ZipFile zipFile = new ZipFile(new File(archivePath));
		Enumeration<? extends ZipEntry> resourceEntries = zipFile.entries();

		while (resourceEntries.hasMoreElements()) {
			ZipEntry resourceEntry = (ZipEntry) resourceEntries.nextElement();
			String resourceName = resourceEntry.getName();
			// Directories sollen nicht gesammelt werden - erkennbar am Suffix '/'.
			// TODO: Ist das in Windows der Backslash?
			if (!resourceName.endsWith("/")) {
				if (isResourceAccessable(resourceName)) {
					resourceToOccurence.put(resourceName, archivePath);
				}
				else {
					logger.log("WARNING: Resource '" + resourceName + "' contained in '" + archivePath + "' is not accessable.");
				}
			}
		}// while

	}// collect


	/**
	 * Checkt, ob die Resource auch wiklich verfuegbar ist aus Sicht des System-Class-Loaders
	 * TODO: ist das ueberhaupt sinnvoll in Anbetracht verschiedener Class-Loader? Ich glaube nicht.
	 * Auf einem Windowssystem funktionierte das z.B. nicht.
	 */
	protected boolean isResourceAccessable(String resource) {
		return true;
//		return System.class.getResource(FILE_SEPARATOR + resource) != null;
	}


	/**
	 * Bestimmt, welche Dateiendungen eine Datei als Archiv klassifizieren. Default sind "jar" und "zip".
	 */
	protected boolean hasArchiveExtension(String fileName) {
		String extension = StringUtils.suffixOf(fileName, '.');
		return (extension == null)? false : propertyHolder.getArchiveExtensions().contains(extension);
	}


	/**
	 * Bestimmt, welche Dateiendungen bei der Report-Ausgabe beruecksichtigt werden. Default sind
	 * "class", "properties" und "xml".
	 */
	protected boolean hasOutputExtension(String fileName) {
		String extension = StringUtils.suffixOf(fileName, '.');
		return (extension == null)? false : propertyHolder.getOutputExtensions().contains(extension);
	}


	/**
	 * Zerteilt einen Klassenpfad durch Doppelpunkt bzw. Semikolon getrennt in seine Bestandteile.
	 * @param pathChain Beispiel: "/Library/Java/Extensions:/System/Library/Java/Contents/Classes/jsse.jar"
	 * @return
	 */
	protected List<String> splitPathChain(String pathChain) {
		List<String> result = new ArrayList<String>();
		String[] paths = pathChain.split(PATH_SEPARATOR);
		for (String path : paths) {
			result.add(path);
		}
		return result;
	}



	/*-----------------------------------------------------------------------*\
	 * main                                                                  *
	\*-----------------------------------------------------------------------*/

	public static void main(String[] args) throws Exception {

//		System.out.println(ClassPathChecker.class.getProtectionDomain().getCodeSource());
//		System.out.println(System.getProperty("java.home"));
//		System.out.println(System.class.getResource("/java/lang/System.class"));
//		if (true) return;
//        Class[] loadedClasses = SimpleJavaAgent.getInstrumentation().getAllLoadedClasses();
//        for (Class clazz : loadedClasses) {
//            System.out.println(clazz);
//        }

		System.out.println(new ClassPathChecker().run().xmlReport());
	}// main

}
