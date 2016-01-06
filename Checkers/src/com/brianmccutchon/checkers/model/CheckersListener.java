package com.brianmccutchon.checkers.model;

/**
 * Listener for events in a checkers game.
 * @author Brian McCutchon
 */
public interface CheckersListener {

	/** Signals that the user has tried to make an invalid move. **/
	public void invalidMove();

	/**
	 * Signals that it is the human's turn and synchronously gets the human's
	 * move. If this move is invalid, {@link #invalidMove()} will be called.
	 * @return The human's move
	 */
	public Move getHumanMove();

	/**
	 * Signals that the board has changed and should be redrawn.
	 * @param board The game board
	 */
	public void boardChanged(byte[][] board);

}
