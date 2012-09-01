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


	/**
	 * Liefert true, falls <code>path</code> mit aufgenommen werden soll. Das ist
	 * stets der Fall, falls er in <code>includeArtifacts</code> enthalten ist;
	 * ansonsten, falls er nicht eine Verlängerung eines Pfads aus
	 * <code>excludeArtifacts</code> ist.
	 */
	public boolean isValid(String path) {
		path = FileUtils.normalizePath(path);

		if (includeArtifacts != null && includeArtifacts.contains(path)) {
			return true;
		}
		if (excludeArtifacts != null) {
			for (String artifact : excludeArtifacts) {
				if (path.startsWith(artifact)) return false;
			}
		}
		return true;
	}

}