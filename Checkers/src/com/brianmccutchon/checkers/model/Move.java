package com.brianmccutchon.checkers.model;

import java.awt.Point;

import framework.DSArrayList;

/**
 * Simple structure to hold information about a move,
 * including a move with multiple jumps.
 * @author Brian McCutchon
 * @version 0.1.0
 */
public class Move {
	
	/**
	 * The coordinates of the square from which we are moving.
	 */
	public Point oldSquare;
	
	/**
	 * Holds the coordinates of the squares to which the piece
	 * will move. If this DSArrayList has move than one item,
	 * the move involves multiple jumps.
	 */
	public DSArrayList<Point> newSquares;
	
	/**
	 * 
	 * @param x The column from which the piece is moving.
	 * @param y The row from which the piece is moving.
	 * @param newSquares The square(s) to which the piece is moving.
	 */
	public Move(int x, int y, Point... newSquares) {
		oldSquare = new Point(x, y);
		this.newSquares = new DSArrayList<Point>(newSquares);
	}
	
	void toArrayIndices() {
		oldSquare.x = oldSquare.x / 2;
		oldSquare.y++;
		for (Point p : newSquares) {
			p.x = p.x / 2;
			p.y++;
		}
	}
	
	/**
	 * Changes this object from the actual array
	 * indices to the easy-to-use values used by
	 * certain high-level methods.
	 */
	void toBoardValues() {
		// add one for even rows with 1 - oldSquare.y%2 
		oldSquare.x = oldSquare.y%2 + oldSquare.x*2;
		oldSquare.y--;
		for (Point p : newSquares) {
			p.x = p.y%2 + p.x*2;
			p.y--;
		}
	}
	
	public DSArrayList<Point> getJumpedSquares() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String toString() {
		String rv = "(" + oldSquare.x + "," + oldSquare.y + ")";
		for (Point pt : newSquares)
			rv += "(" + pt.x + "," + pt.y + ")";
		return rv;
	}
	
	/**
	 * Prepends this move to every element
	 * in a DSArrayList of moves.
	 * @param moves The moves to which to prepend this move.
	 */
	public void prependToAll(DSArrayList<Move> moves) {
		for (Move m : moves)
			m.prepend(this);
	}
	
	/**
	 * Attaches a specified move to the beginning of
	 * this move, shifting over all necessary squares.
	 * @param move The move to prepend.
	 * @throws IllegalArgumentException
	 * If the moves do not match (i.e. if the last
	 * square of the argument does not loosely equal
	 * the first square in <code>this</code> move.)
	 * So,
	 * <pre>oldSquare.equals(move.newSquares.get(
	 *        move.newSquares.size() - 1));</pre>
	 * should return <code>true</code>.
	 */
	public void prepend(Move move) throws IllegalArgumentException {
		if (!oldSquare.equals(move.newSquares.get(move.newSquares.size() - 1)))
			throw new IllegalArgumentException("The moves do not match.");
		
		DSArrayList<Point> newNewSquares = new DSArrayList<Point>();
		newNewSquares.append(move.newSquares);
		newNewSquares.append(this.newSquares);
		this.newSquares = newNewSquares;
		this.oldSquare = move.oldSquare;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Move &&
				oldSquare.equals(((Move) obj).oldSquare) &&
				newSquares.equals(((Move)obj).newSquares));
	}
	
}
