package framework_2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The top-level class of a framework for
 * logic-based games with AI.
 * <p>
 * The generic parameter represents the board, which
 * could be anything from a 2-dimensional array in a
 * board game to a string in a word game.
 * 
 * @author Brian McCutchon
 */
public abstract class Game<B> {
	
	/**
	 * The current game board.
	 * Its type is determined by subclasses.
	 */
	protected B board;
	
	/**
	 * The final state of the game.
	 * After a game, this will be one of:
	 * 0 for a draw,
	 * 1 for player 1 winning,
	 * 2 for player 2 winning, etc.
	 */
	protected int endstate;
	
	/**
	 * The maximum depth of the game tree.
	 * {@code -1} indicates that the game
	 * tree has no depth limit.
	 * <p>
	 * This value does not always strictly limit the
	 * depth of the game tree. Due to memoization,
	 * the tree may go deeper. Also, a branch may
	 * not reach this depth if the end of the game
	 * is within view.
	 */
	private final int maxTreeDepth;
	
	/**
	 * The total number of (human or computer) players.
	 */
	protected final int numPlayers;
	
	/**
	 * An array of booleaans, where index 1 tells whether
	 * player 1 is human, index 1 tells whether player 2
	 * is human, and so on. Its length is equal to
	 * {@code numPlayers + 1}.
	 */
	protected final boolean[] isHuman;
	
	/**
	 * The root of the current game tree.
	 */
	private GameTreeNode<B> root;
	
	/**
	 * The leaves of the current game tree. This will be a
	 * weak hashset, created using the method mentioned here:
	 * <a href="http://stackoverflow.com/a/4062950/2093695">
	 * http://stackoverflow.com/a/4062950/2093695</a>.
	 */
	private Set<GameTreeNode<B>> gameTreeLeaves;
	
	/**
	 * Used to remember the descendants of a node.
	 * Maps hash strings to game tree nodes.
	 */
	private HashMap<String, GameTreeNode<B>> boardNodes =
			new HashMap<String, GameTreeNode<B>>();
	
	/**
	 * Constructor for a game without a tree depth limit.
	 * 
	 * @param numPlayers
	 *   The total number of (human or computer) players.
	 * 
	 * @see #Game(int)
	 * @see #numPlayers
	 */
	protected Game(int numPlayers) {
		this(numPlayers, -1);
	}
	
	/**
	 * Constructor for a game with a tree depth limit.
	 * 
	 * @param numPlayers
	 *   The total number of (human or computer) players.
	 * @param maxTreeDepth
	 *   The tree depth limit.
	 * 
	 * @see #numPlayers
	 */
	protected Game(int numPlayers, int maxTreeDepth) {
		this.numPlayers = numPlayers;
		this.maxTreeDepth = maxTreeDepth;
		isHuman = new boolean[numPlayers + 1];
	}
	
	/**
	 * Starts the game. Blocks until the game is completed.
	 * <p>
	 * This method can be overriden by subclasses for the
	 * purpose of resetting fields between games so that
	 * this method can be called more than once per instance.
	 * However, implementors should usually call this method
	 * via <code>super.play();</code>.
	 */
	protected void play() {
		if (atLeastOnePlayerIsAnAI()) {
			root = buildTree(board);
		} else {
			root = null;
		}
		
		getHumanOrComputer();
		
		
		
		{ // TODO Finish writing play()
			if (root != null) {
				buildNextTreeTier();
			}
			
		}
	}
	
	protected abstract void getHumanOrComputer();

	/**
	 * @return <code>true</code> if at least one player
	 * is an AI.
	 */
	private boolean atLeastOnePlayerIsAnAI() {
		for (int i=1; i<isHuman.length; i++) {
			if (!isHuman[i]) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Builds a game tree given a board.
	 * @param b The game board.
	 * @return The game tree.
	 */
	protected GameTreeNode<B> buildTree(B b) {
		GameTreeNode<B> root = new GameTreeNode<B>(b);
		
		gameTreeLeaves = Collections.newSetFromMap(
				new WeakHashMap<GameTreeNode<B>, Boolean>());
		
		gameTreeLeaves.add(root);
		
		for (int i = 0; i < maxTreeDepth; i++) {
			buildNextTreeTier();
		}
		
		return root;
	}
	
	/**
	 * Builds the next "level" of the tree.
	 * <p>
	 * Essentially, this method looks at all of the nodes in
	 * {@link #gameTreeLeaves}, computing their children and
	 * adding said children to the tree. It also updates and
	 * replaces gameTreeLeaves, creating a new {@link Set}.
	 */
	private void buildNextTreeTier() {
		Set<GameTreeNode<B>> oldLeaves = gameTreeLeaves;
		
		gameTreeLeaves = Collections.newSetFromMap(
				new WeakHashMap<GameTreeNode<B>, Boolean>());

		for (GameTreeNode<B> node : oldLeaves) {
			for (B child : getChildren(node.board)) {
				GameTreeNode<B> newNode;
				String bh = boardHash(child);
				
				if (boardNodes.containsKey(bh)) {
					newNode = boardNodes.get(bh);
				} else {
					newNode = new GameTreeNode<B>(child);
					gameTreeLeaves.add(newNode);
					boardNodes.put(bh, newNode);
				}
				
				node.children.add(newNode);
			}
		}
	}
	
	/**
	 * Creates a String that represents the board
	 * (for use in a HashMap), such that, if a and b
	 * are boards, and {@code a.equals(b)}, then 
	 * {@code boardHash(a) == boardHash(b)}.
	 * @param b The board.
	 * @return A String representing the board.
	 */
	protected abstract String boardHash(B b);
	
	/**
	 * Computes an {@link ArrayList} of all of the possible
	 * boards after the next move made on this board. 
	 * @param b A game board.
	 * @return A DSArrayList of boards.
	 */
	protected abstract ArrayList<B> getChildren(B b);
	
	/**
	 * Executes the turn of a player.
	 * @param turn The number of the player whose turn it is.
	 */
	private void move(int turn) {
		if (isHuman[turn]) {
			humanMove(turn);
		} else {
			computerMove(turn);
		}
	}
	
	protected abstract void humanMove(int turn);
	
	/**
	 * Attempts to select "the best" move and make it,
	 * replacing the {@link #board} field with a clone.
	 * @param turn The number of the computer player whose turn it is.
	 */
	protected void computerMove(int turn) {
		// TODO Write method computerMove()
	}
	
	/**
	 * Creates a new "deeply equal" board. The clone
	 * should share no references with the original board,
	 * but {@link #boardHash(B)} should return equal
	 * Strings for both the old board and its clone.
	 * @param b The board to clone.
	 * @return The cloned board.
	 */
	protected abstract B cloneBoard(B b);
	
	/**
	 * Determines whether a board will result
	 * in a win, lose, or draw, depending on 
	 * @param board A game board.
	 * @return An int representing the result of the board,
	 * as determined by subclasses.
	 */
	protected abstract int evaluateBoard(B board);
	
}
