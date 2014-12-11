/*
 * A tree is a connected graph with no cycles.
 */

package framework;

import java.math.BigInteger;
import java.util.HashMap;

public class DSNode<E> {

	private DSArrayList<DSNode<E>> children;
	private DSNode<E> parent;
	private E thing;
	
	/**
	 * Constructor - creates a new node.
	 * @param thing The thing that this node holds.
	 * @param parent The parent of this node.
	 */
	public DSNode(E thing, DSNode<E> parent) {
		this.thing = thing;
		children = new DSArrayList<DSNode<E>>();
		this.parent = parent;
	}

	/**
	 * Adds a child to the tree.
	 * @param something The contents of the node.
	 * @return The node created.
	 */
	public DSNode<E> addChild(E something) {
		DSNode<E> newNode = new DSNode<E>(something, this);
		children.add(newNode);
		return newNode;
	}

	static HashMap<DSNode<Object>, BigInteger> sizeHash =
			new HashMap<DSNode<Object>, BigInteger>();
	
    /**
     * Recursively finds the size of
     * a tree of DSNodes including the
     * starting node (root) and its children.
     *
     * @see DSNode#DSNode(Object, DSNode)
     * @see DSArrayList#size()
     * @return the size of tree.
     */
	@SuppressWarnings("unchecked")
	public BigInteger sizeOfTree(){
		if (sizeHash.containsKey(this))
			return sizeHash.get(this);
		
		// return value. Starts at 1 for myself
		BigInteger retVal = BigInteger.ONE;
		
		for(int i = 0; i < children.size(); i++)
			retVal = retVal.add(children.get(i).sizeOfTree());
		
		sizeHash.put((DSNode<Object>) this, retVal);
		System.out.println("Tree of size " + retVal);
		return retVal;
	}
	
	static HashMap<DSNode<Object>, BigInteger> leafHash =
			new HashMap<DSNode<Object>, BigInteger>();

	/**
	 * Computes the number of leaves in the tree,
	 * i.e. the number of nodes which
	 * <ol>
	 * 	<li>are descendants of this node, and</li>
	 * 	<li>have no children.</li>
	 * </ol>
	 * @return The number of leaves in the tree.
	 */
	@SuppressWarnings("unchecked")
	public BigInteger countLeaves(){
		if (leafHash.containsKey(this))
			return leafHash.get(this);
		
		// Base case: this is a leaf
		if (children.size() == 0)
			return BigInteger.ONE;
		
		// Return value.
		BigInteger retVal = BigInteger.ZERO;
		
		for(int i = 0; i < children.size(); i++)
			retVal = retVal.add(children.get(i).countLeaves());
		
		leafHash.put((DSNode<Object>) this, retVal);
		
		return retVal;
	}

	/**
	 * Adds a whole tree to the node.
	 * @param newNode The child of this tree.
	 */
	public void addChild(DSNode<E> newNode) {
		newNode.parent = this;
		children.add(newNode);
	}

	/**
	 * Gets the children of this node.
	 * @return A DSArrayList containing the children. 
	 */
	public DSArrayList<DSNode<E>> returnChildren() {
		return children;
	}

	/**
	 * Gets the number of children in this node.
	 * @return The number of children.
	 */
	public int countChildren() {
		return children.size();
	}

	/**
	 * 
	 * @return The parent of this node.
	 */
	public DSNode<E> returnParent() {
		return parent;
	}

	/**
	 * 
	 * @return The thing that this node holds.
	 */
	public E returnThing() {
		return thing;
	}

	/**
	 * Sets the contents of the node.
	 * @param something The contents of the node.
	 */
	public void setThing(E something) {
		thing = something;
	}

	/**
	 * Turns the entire tree into a DSArrayList.
	 * Uses an accumulator -- see {@link #linearizeTo(DSArrayList)}.
	 * @return The DSArrayList generated.
	 */
	public DSArrayList<E> linearize() {
		return linearizeTo(new DSArrayList<E>());
	}
		
	/**
	 * Turns the entire tree into a DSArrayList.
	 * Uses an accumulator to keep track 
	 * of the items in the DSArrayList.
	 * (Auxiliary method for linearize())
	 * Algorithm:
	 *  1) Add current node to the DSArrayList
	 *  2) Recurse on children, if any
	 * 
	 * @param stack 
	 * An accumulator representing the DSArrayList generated.
	 * @return The DSArrayList generated.
	 */
	private DSArrayList<E> linearizeTo(DSArrayList<E> stack) {
		stack.add(thing);
		
		// Base case takes care of itself.
		for (int i=0; i<children.size(); i++) {
			children.get(i).linearizeTo(stack);
		}
		
		return stack;
	}
	
}

