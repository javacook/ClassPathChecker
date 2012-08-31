package com.javacook.classpathchecker;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.javacook.util.FileUtils;

public class PathFilter implements PathFilterInterface {

	private Collection<String> excludeArtifacts;
	private Collection<String> includeArtifacts;


	public PathFilter(PropertyHolderInterface propertyHolder) {

		List<String> origExcludeArtifacts = propertyHolder.getExcludeArtifacts();
		excludeArtifacts = new HashSet<String>();

		if (origExcludeArtifacts != null) {
			for (String path : origExcludeArtifacts) {
				path = FileUtils.normalizePath(path);
				excludeArtifacts.add(path);
			}
		}

		List<String> origIncludeArtifacts = propertyHolder.getAdditionalArtifacts();
		includeArtifacts = new HashSet<String>();

		if (origIncludeArtifacts != null) {
			for (String path : origIncludeArtifacts) {
				path = FileUtils.normalizePath(path);
				includeArtifacts.add(path);
			}
		}
	}


	public boolean isValid(String path) {
		path = FileUtils.normalizePath(path);

		if (includeArtifacts != null && includeArtifacts.contains(path)) {
			return true;
		}
		// FIXME 28.08.2012 jvollmer: hier auf Prefixeigenschaft testen: 
		return excludeArtifacts == null || !excludeArtifacts.contains(path);
	}

}
