package com.javacook.util;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class JavaCookLogger {

	private final static String CRLF = System.getProperty("line.separator");
	private boolean logToConsole = true;
	private String logFileName;
	private List<String> logEntries;
	
	/*-----------------------------------------------------------------------*\
	 * constructors                                                          *
	\*-----------------------------------------------------------------------*/

	public JavaCookLogger(boolean logToConsole, String logFileName) {
		this.logToConsole = logToConsole;
		this.logFileName = logFileName;
		this.logEntries = new ArrayList<String>();
	}

	public JavaCookLogger() {
		this(true, null);
	}

	
	/*-----------------------------------------------------------------------*\
	 * public methods                                                        *
	\*-----------------------------------------------------------------------*/

	public void log(String mess) {
		if (logToConsole) {
			System.out.println(mess);
		}
		if (logFileName != null) {
			append(logFileName, mess);
		}
		logEntries.add(mess);
	}


	public List<String> getLogEntries() {
		return logEntries;
	}

	public void reset() {
		logEntries.clear();
	}



	/*-----------------------------------------------------------------------*\
	 * internal methods                                                      *
	\*-----------------------------------------------------------------------*/

	private void append(String logFileName, String mess) {
		if (logFileName == null)  throw new IllegalArgumentException("Argument 'logFileName' is null.");
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(new File(logFileName), true); // true fuer append
			fileWriter.append(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date())).append(" - ");
			fileWriter.append((mess == null)? "null" : mess).append(CRLF);
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			try {
				if (fileWriter != null) {
					fileWriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/*-----------------------------------------------------------------------*\
	 * main                                                                  *
	\*-----------------------------------------------------------------------*/

	public static void main(String[] args) {
		JavaCookLogger logger = new JavaCookLogger(true, "/Volumes/Braeburn/Entwicklung/Software/Sonstiges/ClassPathChecker/log/cpc.log");
		logger.log("Hallo Welt");
	}



}