/*
 * This class will use generics (<...>)
 * to hold Objects of any type.
 * 
 * The variable E will be used throughout the class
 * description to stand for the type of object that
 * a particular instance will hold.
 * 
 * We will use an array to store the references
 * to our objects.
 */

package framework;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

public class DSArrayList<E> implements Iterable<E> {
	
	/**
	 * Holds the items in the DSArrayList.
	 */
	private E[] array;
	
	/** 
	 * Set within the constructor.
	 * Keeps track of the size of the DSArrayList
	 * each time an element is added.
	 */
	private int numItems;
	
	/**
	 * Creates a DSArrayList with room for 10 items.
	 */
	@SuppressWarnings("unchecked")
	public DSArrayList() {
		array = (E[])(new Object[10]);
		numItems = 0;
	}
	
	/**
	 * Creates a DSArrayList.
	 * @param capacity
	 *   The initial capacity of the DSArrayList.
	 */
	@SuppressWarnings("unchecked")
	public DSArrayList(int capacity) {
		array = (E[])(new Object[capacity]);
		numItems = 0;
	}
	
	/**
	 * Creates a new DSArrayList from the
	 * arguments provided (or an array).
	 * @param things The things to
	 * put in the DSArrayList.
	 */
	public DSArrayList(E... things) {
		this.array = things;
		numItems = things.length;
	}
	
	/**
	 * Adds an item to the DSArrayList.
	 * Enlarges the array as neccessary.
	 * @param item The thing to add.
	 */
	@SuppressWarnings("unchecked")
	public void add(E item) {
		if (array.length == 0) {
			// Use an array of size 10
			array = (E[]) new Object[10];
		} else if (array.length == numItems) {
			// Double array size
			E[] tmpArray = (E[]) new Object[numItems * 2];
			for (int i=0; i<numItems; i++) {
				tmpArray[i] = array[i];
			}
			// replaces old array with new, bigger array
			array = tmpArray;
		}

		array[numItems] = item;
		numItems++;
	}

	public E get(int index) {
		return array[index];
	}

	/**
	 * @return The size of the DSArrayList.
	 */
	public int size() {
		return numItems;
	}

	/*
	 * Places something in an existing and occupied slot
	 * in the DSArrayList, replacing the old item.
	 * @param index The place to put the item.
	 * @param item The thing to place in the DSArrayList.
	 * @throws IndexOutOfBoundsException
	 * If the index provided is not in [0, size()).
	 * @see java.util.ArrayList#set(int, Object)
	 */
	/*public void set(int index, E item) throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub

	}*/

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < numItems;
			}

			@Override
			public E next() {
				return get(i++);
			}

			/**
			 * This method is not supported.
			 */
			@Override
			public void remove() {
				throw new UnsupportedOperationException(
						"The DSArrayList iterator does not" + 
						"support the remove operation.");
			}

		};
	}
	
	public void append(DSArrayList<E> toAppend) {
		for (E item : toAppend)
			add(item);
	}
	
	/**
	 * Determines whether the DSArrayList contains
	 * a specified element. Returns true if
	 * any element is "loosely equal" to the
	 * specified element as determined by
	 * {@link Object#equals(Object)}.
	 * @param item The thing for which to search.
	 * @return
	 *   Whether the item is contained in the DSArrayList.
	 */
	public boolean contains(E item) {
		for (E otherItem : this)
			if ((item == null && otherItem == null) ||
					item.equals(otherItem))
				return true;
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DSArrayList))
			return false;
		
		DSArrayList<E> arrList;
		
		try {
			arrList = (DSArrayList<E>) obj;
		} catch (ClassCastException err) {
			return false;
		}
		
		if (!(arrList.size() == numItems))
			return false;
		
		for (int i = 0; i < numItems; i++)
			if (!((array[i] == null && arrList.get(i) == null) ||
					array[i].equals(arrList.get(i))))
				return false;
		
		return true;
	}
	
	/**
	 * Removes and returns the last item in the DSArrayList.
	 * @return The last item in the DSArrayList.
	 */
	public E pop() {
		E rv = array[numItems-1];
		array[--numItems] = null;
		return rv;
	}

	public Stream<E> stream() {
		return Arrays.stream(array).limit(numItems);
	}

	public void shuffle() {
		// TODO Actually implement shuffling!
		array = Arrays.copyOfRange(array, 0, numItems);
		Collections.shuffle(Arrays.asList(array));
	}
	
}
