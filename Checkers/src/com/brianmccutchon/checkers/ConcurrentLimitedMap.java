package com.brianmccutchon.checkers;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A {@link ConcurrentHashMap} with a fixed size.
 * This size is both the initial and maximum capacity.
 * Old elements are not guaranteed to be removed in the precise order
 * in which they were added. Elements added with a method other than put()
 * may be overlooked.
 * </p><p>
 * The idea comes from here:
 * <a href="http://stackoverflow.com/a/5601377/2093695">http://stackoverflow.com/a/5601377/2093695</a>
 * </p>
 * @author Brian McCutchon
 */
public class ConcurrentLimitedMap<K, V> extends ConcurrentHashMap<K, V> {

	private ConcurrentLinkedQueue<K> keys;
	private static final long serialVersionUID = -6599382167190522623L;

	/**
	 * The maximum size of this HashMap.
	 */
	private int sizeLimit;

	/**
	 * @param size The size of this HashMap.
	 * It is both the initial and maximum capacity.
	 */
	public ConcurrentLimitedMap(int size) {
		super(size);
		this.sizeLimit = size;
		keys = new ConcurrentLinkedQueue<K>();
	}

	@Override
	public V put(K key, V value) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);
		if (size() > sizeLimit) {
			this.remove(keys.remove());
		}

		// Precise order isn't important here
		keys.add(key);
		return super.put(key, value);
	}

}
