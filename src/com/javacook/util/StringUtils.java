package com.javacook.util;

public class StringUtils {

	public static String truncPrefix(String str, String prefix, boolean throwException) {
		if (str == null) 	throw new IllegalArgumentException("Argument 'str' is null.");
		if (prefix == null)	throw new IllegalArgumentException("Argument 'prefix' is null.");

		if (str.startsWith(prefix)) {
			return str.substring(prefix.length());
		}
		else if (throwException) {
			throw new IllegalArgumentException("Argument str = \"" + str + "\" does not start with prefix = \"" + prefix + "\".");
		}
		else {
			return str;
		}
	}

	public static String truncPrefix(String str, String prefix) {
		return truncPrefix(str, prefix, false);
	}


	public static String truncSuffix(String str, String suffix, boolean throwException) {
		if (str == null) 	throw new IllegalArgumentException("Argument 'str' is null.");
		if (suffix == null)	throw new IllegalArgumentException("Argument 'suffix' is null.");

		if (str.endsWith(suffix)) {
			return str.substring(0, str.length() - suffix.length());
		}
		else if (throwException) {
			throw new IllegalArgumentException("Argument str = \"" + str + "\" does not end with suffix = \"" + suffix + "\".");
		}
		else {
			return str;
		}
	}


	public static String truncSuffix(String str, String prefix) {
		return truncSuffix(str, prefix, false);
	}


	public static String suffixOf(String str, char delim) {
		if (str == null) 	throw new IllegalArgumentException("Argument 'str' is null.");
		int lastIndexOfDot = str.lastIndexOf(delim);
		return (lastIndexOfDot < 0)? null : str.substring(lastIndexOfDot +1);
	}



	/*-----------------------------------------------------------------------*\
	 * main                                                                  *
	\*-----------------------------------------------------------------------*/

	public static void main(String[] args) {
		System.out.println(StringUtils.truncPrefix("Meininghaus", "Mein"));
		System.out.println(StringUtils.truncSuffix("Meininghaus", "haus"));

		for (Object key : System.getProperties().keySet()) {
			System.out.println("Key = " + key + ", Value = " + System.getProperty((String)key));
		}
	}

}