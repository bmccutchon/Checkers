package framework;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The top-level class of a framework for
 * logic-based games with AI.
 * <p>
 * The generic parameter represents the board, which
 * could be anything from a 2-dimensional array in a
 * board game to a string in a word game.
 * 
 * @author CSUDallas
 */
public abstract class Game<B> {
	
	/**
	 * The number of levels deep to spawn threads.
	 */
	protected static final int THREAD_DEPTH = 3;

	/**
	 * The maximum depth of the game tree.
	 */
	private final int maxTreeDepth;

	/**
	 * Constructor for a game without a tree depth limit.
	 */
	protected Game() {
		this.maxTreeDepth = -1;
	}
	
	/**
	 * Constructor for a game with a tree depth limit.
	 * @param maxTreeDepth The tree depth limit.
	 */
	protected Game(int maxTreeDepth) {
		this.maxTreeDepth = maxTreeDepth;
	}
	
	/**
	 * The final state of the game.
	 * Its value is determined by subclasses.
	 */
	protected int endstate;
	
	/**
	 * The current game board.
	 */
	protected B board;
	
	/**
	 * Executes the turn of a player.
	 * @param turn The number of the player whose turn it is
	 */
	protected final void move(int turn) {
		if (isHuman[turn]) {
			humanMove(turn);
		} else {
			computerMove(turn);
		}
	}
	
	/** Array of booleans: true = human, false = computer */
	protected boolean[] isHuman = new boolean[3];
	
	int numNodes;
	
	/**
	 * Gets input from the player as to his/her move,
	 * updates the board to reflect this input,
	 * and increments numMoves.
	 * @param turn The number of the player whose turn it is.
	 */
	protected abstract void humanMove(int turn);
	
	/**
	 * Selects a legal move and makes it.
	 * @param turn The number of the computer player whose turn it is.
	 */
	protected void computerMove(int turn) {
		//DSArrayList<DSGameNode<B>> candidates =
		//		new DSArrayList<DSGameNode<B>>();
		
		//int topValue = Integer.MIN_VALUE;
		
		DSArrayList<B> children = getChildren(board);
		
		// If there's only one move, take it.
		if (children.size() == 1) {
			board = children.get(0);
			return;
		}
		
		children.shuffle();

		board = cloneBoard(children.stream().parallel()
			.map(b -> buildTree(b, maxTreeDepth))
			.max((a, b) -> Integer.compare(evaluateNode(a), evaluateNode(b)))
			.get()
			.returnThing());

//			DSGameNode<B> node = buildTree(b, maxTreeDepth);
//			int val = evaluateNode(node);
//			if (val > topValue) {
//				topValue = val;
//				candidates = new DSArrayList<DSGameNode<B>>();
//				candidates.add(node);
//			} else if (val == topValue)
//				candidates.add(node);
		
		//board = cloneBoard(candidates.get(
		//		(int)(Math.random() * candidates.size())).returnThing());
	}

	// We will override this so that
	// each game can draw itself
	protected abstract void drawBoard();

	/**
	 * Checks to see if the game is over.
	 * Called after every move.
	 * @return An int indicating the endstate.
	 * Its meaning is determined by subclasses.
	 */
	protected abstract int endCheck();

	/** Call this method to start a game. */
	protected abstract void play();
	
	/**
	 * Builds a game tree given a board.
	 * @param b The game board.
	 * @param depth The depth to which to build the tree.
	 * {@code -1} if the tree has no depth limit.
	 * @return The game tree.
	 */
	protected DSGameNode<B> buildTree(B b, int depth) {
		String bh = boardHash(b);

		synchronized (boardNodes) {
			if (boardNodes.containsKey(bh))
				return boardNodes.get(bh);
		}
		
		DSGameNode<B> root = new DSGameNode<B>(b, null);
		numNodes++;
		
		if (depth != 0) {
			// Get the children of this board.
			// In the base case, there will be no children
			DSArrayList<B> children = getChildren(b);

			Stream<B> stream = children.stream();
			if (maxTreeDepth - depth < THREAD_DEPTH - 1) {
				stream = stream.parallel();
			}
			stream.map(c -> buildTree(c, depth - 1))
					.forEach(root::addChild);
		}

		synchronized (boardNodes) {
			boardNodes.put(bh, root);
		}

		return root;
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
	 * Computes a {@link DSArrayList} of all of the possible
	 * boards after the next move made on this board. 
	 * @param b A game board.
	 * @return A DSArrayList of boards.
	 */
	protected abstract DSArrayList<B> getChildren(B b);

	/**
	 * Used to remember the result of a board.
	 * Maps hash strings to Integers.
	 */
	protected Map<String, Integer> boardValues = new HashMap<>();
	
	/**
	 * Used to remember the descendants of a node.
	 * Maps hash strings to game nodes.
	 */
	protected Map<String, DSGameNode<B>> boardNodes = new HashMap<>();
	
	/**
	 * Creates a String that represents the
	 * board (for use in a HashMap). 
	 * @param b The board.
	 * @return A String representing the board.
	 */
	protected abstract String boardHash(B b);
	
	/**
	 * Determines who wins a particular board.
	 * Typically, this is called on the result of buildTree.
	 * XXX Currently, this is geared toward two-player games.
	 * @param node The game tree.
	 * @return An int representing the result.
	 */
	protected int evaluateNode(DSGameNode<B> node) {
		int rv = -99; // code for "uninitialized"
		
		String bh = boardHash(node.returnThing());
		
		if (boardValues.containsKey(bh))
			return boardValues.get(bh);
		
		// First, look at our own board.
		int val = evaluateBoard(node.returnThing());

		if(val != TwoPlayer.CONTINUE) {
			rv = val;
		} else {
			Set<DSNode<B>> children = node.returnChildren();

			boolean drawIsPossible = false; // flag!
			int turn = whoseTurn(node.returnThing());
	
			// TODO Bound max and make sure this works!
			//Optional<Integer> maxScore = children.stream().parallel()
			//		.map(c -> evaluateNode((DSGameNode<B>) c))
			//		.max(Integer::compare);

			for (int i=0; i<children.size(); i++) {
				DSGameNode<B> c = (DSGameNode<B>) children.get(i);
				int childVal = evaluateNode(c);
				if (childVal == TwoPlayer.PLAYER1WIN && turn == 1) {
					rv = TwoPlayer.PLAYER1WIN;
					break;
				} else if (childVal == TwoPlayer.PLAYER2WIN && turn == 2) {
					rv = TwoPlayer.PLAYER2WIN;
					break;
				} else if (childVal == TwoPlayer.DRAW) {
					drawIsPossible = true;
				}
			}

			if (rv == -99) {
			//if (maxScore.isPresent()) {
				//rv = maxScore.get();
			//} else {
				if (drawIsPossible) {
					rv = TwoPlayer.DRAW;
				} else if (turn == 1) {
					rv = TwoPlayer.PLAYER2WIN;
				} else {
					rv = TwoPlayer.PLAYER1WIN;
				}
			}
		}

		boardValues.put(bh, rv);
		
		return rv;
	}

	/**
	 * Determines whose turn it is.
	 * @param board A game board.
	 * @return An int representing the player whose turn it is.
	 */
	protected abstract int whoseTurn(B board);

	/**
	 * Determines whether a board will result
	 * in a win, lose, or draw, depending on 
	 * @param board A game board.
	 * @return An int representing the result of the board,
	 * as determined by subclasses.
	 */
	protected abstract int evaluateBoard(B board);

	/**
	 * Inner class used for building game trees.
	 * Holds win/lose/draw info on each node.
	 */
	protected class DSGameNode<E> extends DSNode<E> {
		public int winState;

		// Constructor
		public DSGameNode(E thing, DSNode<E> parent) {
			super(thing, parent);
		}
		
	}

}

