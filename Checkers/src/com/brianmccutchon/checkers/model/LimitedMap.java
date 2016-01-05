package com.brianmccutchon.checkers.model;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * A {@link LinkedHashMap} with a fixed size.
 * This size is both the initial and maximum capacity.
 * </p><p>
 * The idea comes from here:
 * <a href="http://stackoverflow.com/a/5601377/2093695">http://stackoverflow.com/a/5601377/2093695</a>
 * </p>
 * @author Brian McCutchon
 */
public class LimitedMap<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = -6599382167190522623L;
	
	/**
	 * The maximum size of this HashMap.
	 */
	private int sizeLimit;
	
	/**
	 * 
	 * @param size The size of this HashMap.
	 * It is both the initial and maximum capacity.
	 * @see LinkedHashMap#LinkedHashMap(int)
	 */
	public LimitedMap(int size) {
		super(size);
		this.sizeLimit = size;
	}
	
	/**
	 * Removes the eldest entry if the size
	 * is greater than {@link #sizeLimit}.
	 */
	@Override
	protected boolean removeEldestEntry(Entry<K, V> eldest) {
		return (size() > sizeLimit);
	}

}
