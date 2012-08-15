package com.javacook.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * An implementation of a multi hash map. In contrast to the standard HashMap the value is not overwritten
 * but added to the <i>set</i> of the values for the key <code>key</code>.
 * @author Joerg Vollmer
 *
 * @param <K> key class
 * @param <V> value class
 */
public class MultiHashMap<K, V> {

	private Map<K, Collection<V> > map = new LinkedHashMap<K, Collection<V> >();

	/**
	 * Puts (adds) the value <code>value</code> for the key <code>key</code>.
	 * @param key
	 * @param value
	 */
    public Collection<V> put(K key, V value) {

    	Collection<V> values = map.get(key);

        if (values == null) {
        	values = newCollection();
        	values.add(value);
            map.put(key, values);
        }
        else {
        	values.add(value);
        }

        return values;
    }// put



    public void putAll(MultiHashMap<K, V> map) {
    	if (map == null) {
    		return;
    	}
    	for (K key : map.keySet()) {
    		Collection<V> values = map.get(key);
    		if (values != null) {
    			for (V value : values) {
					this.put(key, value);
				}
    		}
		}
    }


    /**
     * Creates the collection which is uses to link it to the value. By default an ArrayList is
     * created. This method can be overwritten if e.g. an unordered Collection is desired.
     * @return a new collection object
     */
    protected Collection<V> newCollection() {
    	return new ArrayList<V>();
    }


    /**
     * Returns the values for key <code>key</code>.
     * @param key
     */
    public Collection<V> get(K key) {
    	return map.get(key);
    }

    /**
     * Returns the key set.
     */
    public Set<K> keySet() {
    	return map.keySet();
    }


    /**
     * Returns true iff <code>value</code> belongs to the key <code>key</code>, i.e. is contained in
     * the set/list of values linked to <code>key</code>.
     * @param key
     * @param value
     */
    public boolean contains(K key, V value) {
    	Collection<V> values = get(key);
    	if (values == null) { return false; }
    	return values.contains(value);
    }


}// MultiHashMap
