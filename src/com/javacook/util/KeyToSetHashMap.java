package com.javacook.util;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Es kann sein, dass in den Pfaden ein Pfad ein Teil-Pfad von einem anderen ist, 
 * z.B. <tt>WEB-INF/lib</tt> und <tt>WEB-INF/lib/xercesImpl.jar</tt>, sodass die
 * Klassen von <tt>xercesImpl.jar</tt> doppelt hinzugefuegt wurden. Das wird zwar
 * neuerdings durch adjustedList von PathSet erledigt, aber zur Sicherheit bleibt
 * das hier auch noch drin.
 */
public class KeyToSetHashMap<K, V> extends MultiHashMap<K, V> {
	
	@Override
    protected Collection<V> newCollection() {
    	return new LinkedHashSet<V>();
    }

}// MultiHashMap
