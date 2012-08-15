package com.javacook.util;

import java.util.Collection;
import java.util.TreeSet;

public class PathSet extends TreeSet<String> {
	private static final long serialVersionUID = -8757505877799051559L;
	private final static String FILE_SEPARATOR 	= System.getProperty("file.separator"); // Bei Windows Backslash, ansonsten Slash:


	@Override
	public boolean add(String path) {
		return super.add(normalizePath(path));
	}

	@Override
	public boolean addAll(Collection<? extends String> coll) {
		boolean ok = true;
		for (String path : coll) {
			ok &= add(path);
		}
		return ok;
	}

	private String normalizePath(String path) {
		if (path == null) return null;
		path = path.trim();
		while (path.endsWith(FILE_SEPARATOR)) {
			path = StringUtils.truncSuffix(path, FILE_SEPARATOR);
		}
		return path;
	}

}
