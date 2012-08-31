package com.javacook.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * Diese Klasse stellt Routinen zum (zeilenweisen) Lesen und Schreiben von Dateien zur Verfuegung.
 * Es ist sogar moeglich, den Java-Wert <code>null</code> abzuspeichern (siehe Konstruktoren).
 * Default-Verhalten: <code>null</code> wird nicht abgespeichert.
 * @author vollmer
 */
public class FileUtils
{
	private final static String CRLF 				= System.getProperty("line.separator");
	private final static String FILE_SEPARATOR 	= System.getProperty("file.separator"); // Bei Windows Backslash, ansonsten Slash:
	protected String nullCoding;

	/**
	 * Standard-Konstruktor
	 */
	public FileUtils()
	{
		nullCoding = null;
	}


	/**
	 * Konstruktor, der definiert, welcher String als Kodierung von <code>null</code> verwendet werden soll.
	 * @param nullCoding
	 */
	public FileUtils(String nullCoding)
	{
		this.nullCoding = nullCoding;
	}


	/**
	 * Eigene Exception-Klasse, die allerdings von <code>RuntimeException</code> abgeleitet ist und nicht
	 * von <code>Excpetion</code>. Semantisch hat sie die gleiche Bedeutung wie die
	 * <code>java.io.IOException</code>.
	 */
	public static class IOException extends RuntimeException
	{
		private static final long serialVersionUID = 4787999770693259907L;

		public IOException(java.io.IOException e) {
			super(e);
		}

		@Override
		public String getMessage() {
			return getCause().getMessage();
		}
	};


	// ---------- r e a d -----------------------------------------------------

	/**
	 * Private innnere Klasse, die dazu dient, eine Datei zeilenweise durchzuiterieren.
	 * Der Vorteil ist, dass die IO-technischen Belange in der Klasse <code>FileUtils</code>
	 * gekapselt bleiben. Der Iterator arbeitet so, dass erst beim Aufruf von <code>next</code>
	 * eine neue Zeile aus der Datei gelesen wird. Die Datei wird automtisch geschlossen,
	 * wenn der letzte Eintrag gelesen worden ist.
	 */
	private class TextFileIterator implements Iterator<String> {

		private BufferedReader reader;
		private String line;

		/**
		 * Konstruktor, der eine Iterator zur Datei <code>file</code> erstellt. Innerhalb des
		 * Konstruktors wird die Datei bereits geoeffnet, und es wird versucht, die erste Zeile
		 * zu lesen.
		 * @param file Datei, fuer den der Iterator erstellt wird
		 * @throws IOException, falls beim Oeffnen der Datei oder beim Lesen der ersten Zeile
		 * ein IOException auftritt.
		 */
		public TextFileIterator(File file) throws IOException
		{
			if (file == null) { throw new IllegalArgumentException("Argument 'file' is null."); }
			try
			{
	         	reader = new BufferedReader(new FileReader(file));
	         	// Sicherheitscheck:
	         	if (reader == null) { throw new IllegalStateException("Value of 'reader' is null."); }
	         	line = reader.readLine();
	         	if (line == null) {	reader.close();	}
			}
			catch (java.io.IOException e)
			{
				close(reader);
				throw new IOException(e);
			}
		}// Constructor


		/**
		 * Wie <code>TextFileIterator(File)</code>, nur dass der Datei-Pfad die Datei festlegt
		 * @param filePath
		 * @see #TextFileIterator(File)
		 */
		public TextFileIterator(String filePath)
		{
			this(new File(filePath));
		}


		/**
		 * Gibt aus, ob noch eine weitere Zeile zum Lesen bereit steht.
		 * @Override
		 */
		public boolean hasNext()
		{
			return line != null;
		}


		/**
		 * Dient dazu, die Zeilen der Datei iterativ auszulesen. Jeder Aufruf liefert
		 * jeweils die naechste Zeile.</br>
		 * Hinweis: Falls die Datei den Wert <code>null</code> zureuckgibt, bedeutet das
		 * nicht, dass das Ende der Datei erreicht ist.
		 * @NoSuchElementException falls versucht wird eine Zeile auszulesen, obwohl die
		 * Methode <code>hasNext</code> den Wert <code>false</code> ausgibt.
		 * @Override
		 */
		public String next() throws IOException
		{
			if (line == null) { throw new  NoSuchElementException(); }
			String temp = line;
			try
			{
				if (reader == null) { throw new IllegalStateException("Value of 'reader' is null."); }
				line = reader.readLine();
				// Ist man am Ende angelangt, wird die Datei geschlossen:
				if (line == null) {	reader.close();	}
				if (nullCoding != null && nullCoding.equals(temp)) { temp = null; }
				return temp;
			}
			catch (java.io.IOException e)
			{
				close(reader);
				throw new IOException(e);
			}
		}// next


		/**
		 * Wurde noch nicht implementiert.
		 * @throws IllegalStateException, immer
		 * @Override
		 */
		public void remove()
		{
			throw new IllegalStateException("remove is not implemented here.");
		}

	}// TextFileIterator



	/**
	 *
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public Iterable<String> read(final String filePath) throws IOException
	{
		if (filePath == null) { throw new IllegalArgumentException("Argument 'filePath' is null."); }
		return new Iterable<String>()
		{
			public Iterator<String> iterator()
			{
				return new TextFileIterator(filePath);
			}
		};
	}


	// ---------- w r i t e -----------------------------------------------------

	/**
	 * Schreibt eine Iteration von String-Werte zeilenweise in eine Datei mit dem Namen
	 * <code>filePath</code>. Die Werte der Iteration darf durchaus auch den Wert <code>null</code>
	 * annehmen. Wurde der Kunstruktor <code>FileUtils(String nullCoding)</code> verwendet,
	 * wird fuer <code>null</code> der Wert <code>nullCoding</code> verwendet (als eigene Zeile).
	 * Ansonsten wird der Wert <code>null</code> einfach uebergangen.
	 * @throws IOException falls bei der Operaton ein <code>java.io.IOException</code> aufgetreten
	 * ist.
	 */
	public void write(Iterator<?> iter, String filePath) throws IOException
	{
		if (filePath == null) { throw new IllegalArgumentException("Argument 'filePath' is null."); }
		BufferedWriter wr = null;
		try
		{
			File file = new File(filePath);
			wr = new BufferedWriter(new FileWriter(file));

			while (iter.hasNext())
			{
				Object temp = iter.next();
				if (temp == null)
				{
					if (nullCoding != null) { wr.write(nullCoding); }
				}
				else { wr.write(temp.toString()); }
				if (iter.hasNext()) { wr.write(CRLF); }
			}
			if (wr != null) { wr.close(); }
		}
		catch (java.io.IOException e)
		{
			close(wr);
			throw new IOException(e);
		}
	}// write


	/**
	 * Schliesst <code>cl</code>.
	 * @param cl Zu schliessende Datei.
	 * @throws IOException, falls beim Schliessen eine <code>java.io.IOException</code> aufgetreten ist.
	 */
	protected void close(Closeable cl) throws IOException
	{
		try
		{
			if (cl != null) { cl.close(); }
		}
		catch (java.io.IOException e) { throw new IOException(e); }
	}// close


	/**
	 * Moegliche Rueckgabe-Werte der Methode <code>rename</code>
	 */
	public enum RenameErrorCode {OK, SOURCE_FILE_DOES_NOT_EXIST, DEST_FILE_ALREADY_EXISTS, OTHER_REASON};

	/**
	 * Benennt die Datei mit dem Dateinamen <code>sourceFileName</code> um in <code><destFileName/code>.
	 * Ist die Datei mit dem Namen <code>destFileName</code> bereits vorhanden, bestimmt
	 * <code>overwriteDest</code>, ob die Zieldatei ueberschreiben, d.h. zuvor geloescht wird.
	 * Ist dies nicht der Fall, bliebt alles beim Alten, und es wird der Wert
	 * <code>DEST_FILE_ALREADY_EXISTS</code> zurueckgeliefert.
	 * @param sourceFileName Dateiname der umzubenennenden Datei
	 * @param destFileName Ziel der umzubenennenden Datei
	 * @param overwriteDest bestimmt, ob eine evtl. existierende Datei ueberschrieben wird.
	 * @return <code>RenameErrorCode.OK</code>, falls die Umbennung fehlerfrei erfolgte;
	 * <code>SOURCE_FILE_DOES_NOT_EXIST</code>, falls keine Datei unter dem Dateinamen
	 * <code>sourceFileName</code> existiert; <code>DEST_FILE_ALREADY_EXISTS</code>, falls bereits
	 * eine Datei mit dem Namen <code>DEST_FILE_ALREADY_EXISTS</code> existiert und
	 * <code>overwriteDest</code> false ist;  <code>OTHER_REASON</code>, falls es andere
	 * Gruende gab, warum die Umbenennung nicht erfolgreich gewesen ist.
	 * <code></code>
	 */
	public RenameErrorCode rename(String sourceFileName, String destFileName, boolean overwriteDest) {
		File sourceFile = new File(sourceFileName);
		if (!sourceFile.exists()) {
			return RenameErrorCode.SOURCE_FILE_DOES_NOT_EXIST;
		}

		File destFile = new File(destFileName);

		if (destFile.exists()) {
			if (overwriteDest) {
				destFile.delete();
			} else {
				return RenameErrorCode.DEST_FILE_ALREADY_EXISTS;
			}
		}

		return sourceFile.renameTo(destFile)? RenameErrorCode.OK : RenameErrorCode.OTHER_REASON;
	}// rename




	public interface CallBack {
		public void action(String actualPath) throws Exception;
	}


	/**
	 * Wandert rekursiv das Verzeichnis <code>basePath</code> durch und ruft fuer jede Datei (die kein Verzeichnis ist)
	 * <code>callBack</code> auf.
	 */
	public void browseDirTree(String basePath, CallBack callBack) throws Exception {
		browseRecursively(basePath, basePath, callBack);
	}

	private void browseRecursively(String basePath, String actualPath, CallBack callBack) throws Exception {
		File file = new File(actualPath);
		if (file.isDirectory()) {
			for (String fileName : file.list()) {
				browseRecursively(basePath, file.getAbsolutePath() + FILE_SEPARATOR + fileName, callBack);
			}
		}
		else {
			callBack.action(actualPath);
		}
	}


	/**
	 * Vervollstaendigt relative Pfad und entfernt z.B. auch (Back-)Slashes
	 * am Ende.
	 * @param path z.B. "Vollmer/"
	 * @return z.B. "/Volumes/Braeburn/Entwicklung/Software/javacook/ClassPathChecker/Vollmer"
	 */
	public static String normalizePath(String path) {
		if (path == null) return null;
		path = path.trim();
		if (path.length() == 0) return null;
		try {
			return new File(path).getCanonicalPath();
		} catch (java.io.IOException e) {
			throw new RuntimeException("Fehler bei der Normierung des Pfads '" + path + "'", e);
		}
	}



	/*----------------------------------------------------------------------------*\
	 * main                                                                       *
	\*----------------------------------------------------------------------------*/

	public static void main(String[] args) throws IOException
	{

		FileUtils fileUtils = new FileUtils();
		System.out.println(fileUtils.rename("/Users/vollmer/leute.csv", "/Users/vollmer/guck.csv", true));

//		long start = System.currentTimeMillis();
//		Iterable<String> lines = fileUtils.read("G:/jiowa/trunk/msearch/src/test/resources/samples/vornamen50.txt");
//		Iterator<String> iter = lines.iterator();
//		while (iter.hasNext()) { System.out.println("Element: " + iter.next()); }
//		System.out.println("Ausfuehrungszeit: " + (System.currentTimeMillis() - start));
//		fileUtils.write(lines.iterator(), "F:/vornamen50.txt");

   }// main

}// FileUtils
