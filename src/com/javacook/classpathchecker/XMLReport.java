package com.javacook.classpathchecker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class XMLReport {

	public final String DEFAULT_REPORT_NAME = "cpc_report.xml";

	private final static String FILE_SEPARATOR = System.getProperty("file.separator"); // Bei Windows Backslash, ansonsten Slash:
	private String content;

	public XMLReport(String content) {
		this.content = content;
	}


	public XMLReport save(String fileName, boolean override) throws IOException {
		if (fileName == null) throw new IllegalArgumentException("Argument 'fileName' ist null,");

		if (new File(fileName).isDirectory()) {
			fileName += FILE_SEPARATOR + DEFAULT_REPORT_NAME;
		}
		File file = new File(fileName);
		if (file.exists() && !override) {
			throw new IOException("The file '" + file + "' already exists.");
		}

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);
			fileWriter.write(content);
		} catch (IOException e) {
			throw e;
		} finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
		return this;
	}


	public XMLReport save(String fileName) throws IOException {
		return save(fileName, true);
	}


	@Override
	public String toString() {
		return (content == null)? "null" : content;
	}

}
