package com.javacook.classpathchecker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import com.javacook.util.FileUtils;

public class PathSet extends TreeSet<String> {
	private static final long serialVersionUID = -8757505877799051559L;

	private final PathFilterInterface pathFilter;

	public PathSet(PathFilterInterface pathFilter) {
		this.pathFilter = pathFilter;
	}


	@Override
	public boolean add(String path) {
		String normalizedPath = FileUtils.normalizePath(path);

		if (pathFilter.isValid(normalizedPath)) {
			return super.add(normalizedPath);
		}
		else return false;
	}


	public boolean addAll(Collection<? extends String> coll) {
		boolean ok = true;
		for (String path : coll) {
			ok &= add(path);
		}
		return ok;
	}


	/**
	 * Es kann sein, dass in den Pfaden ein Pfad ein Teilpfad von einem anderen ist,
	 * z.B. <tt>WEB-INF/lib</tt> und <tt>WEB-INF/lib/xercesImpl.jar</tt>, sodass die
	 * Klassen von <tt>xercesImpl.jar</tt> doppelt hinzugefuegt wuerden. Daher
	 * entfernt diese Methode alle Pfade, fuer die es bereits ein Prefix gibt.
	 */
	public List<String> adjustedList() {
		List<String> result = new ArrayList<String>();
		String prefixPath = null;
		// Dies funktioniert nur, weil die Liste der Pfade alphabetisch sortiert ist.
		for (String path : this) {
			if (prefixPath == null) {
				prefixPath = path;
				result.add(path);
			}
			else if (!path.startsWith(prefixPath)) {
				result.add(path);
				prefixPath = path;
			}
		}
		return result;
	}


	/*----------------------------------------------------------------------------*\
	 * main                                                                       *
	\*----------------------------------------------------------------------------*/

	public static void main(String[] args) {
		PathSet pathSet = new PathSet(new PathFilterInterface() {

			public boolean isValid(String path) {
				return true;
			}
		});
		pathSet.add("null.jar");
		pathSet.add("WEB-INF/lib/eins.jar");
		pathSet.add("zehn.jar");
		pathSet.add("WEB-INF/lib");
		pathSet.add("WEB-INF/lib/zwei.jar");
		System.out.println(pathSet.adjustedList());
	}
}
