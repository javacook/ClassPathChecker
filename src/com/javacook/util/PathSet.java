package com.javacook.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;

public class PathSet extends TreeSet<String> {
	private static final long serialVersionUID = -8757505877799051559L;

	@Override
	public boolean add(String path) {
		String normalizedPath = normalizePath(path);
		return (normalizedPath != null)? super.add(normalizedPath) : false;
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
		if (path.length() == 0) return null;
		try {
			return new File(path).getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
