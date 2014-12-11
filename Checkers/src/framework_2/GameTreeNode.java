package framework_2;

import java.util.ArrayList;
import java.util.WeakHashMap;

/**
 * A GameTreeNode is a tree-like structure where anything goes.
 * <p>
 * It can have cycles or nodes with multiple parents. The only
 * real rule is that it cannot have more than one root, but this
 * is not enforced. Nodes do not keep track of their parents.
 * @author Brian McCutchon
 */
class GameTreeNode<E> {
	
	E board;
	int boardValue;
	boolean isVisited = false;
	ArrayList<GameTreeNode<E>> children;
	
	/**
	 * Creates a new GameTreeNode to hold the board provided.
	 * @param board
	 */
	GameTreeNode(E board) {
		this.board = board;
	}
	
	/**
	 * Adds a new child of this node containing the specified board.
	 * @param board The board to add.
	 */
	void addChild(E board) {
		children.add(new GameTreeNode<E>(board));
	}
	
	/**
	 * A 2-D {@link WeakHashMap}. Memoizes the result of
	 * {@link #hasDescendant(GameTreeNode)}.
	 * The keys to the first HashMap are the ancestors,
	 * the second keys are the possible descendants.
	 * The idea for a 2-D HashMap comes from
	 * <a href="http://stackoverflow.com/a/14678042/2093695">
	 * http://stackoverflow.com/a/14678042/2093695</a>.
	 */
	static WeakHashMap<GameTreeNode<Object>,
			WeakHashMap<GameTreeNode<Object>, Boolean>>
		hasDescendantNodes = new WeakHashMap<GameTreeNode<Object>,
			WeakHashMap<GameTreeNode<Object>, Boolean>>();
	
	/**
	 * Determines whether this node has
	 * the given node as a descendant.
	 * @param node The node which may be a descendant.
	 * @return Whether or not the given node is a
	 * descendant of this node.
	 */
	boolean hasDescendant(GameTreeNode<E> node) {
		// TODO Implement memoization.
		
		boolean rv;
		
		this.isVisited = true;
		
		main: {
			for (GameTreeNode<E> child : children) {
				if (child.isVisited) {
					// We've seen this place before! We're going in circles!
					rv = false;
					break main;
				} else if (child == node || child.hasDescendant(node)) {
					rv = true;
					break main;
				}
			}

			rv = false;
		}
		
		this.isVisited = false;
		
		return rv;
	}
}
