package com.brianmccutchon.checkers.model;

import java.awt.Point;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	 * Regex for parsing user input. Matches at least
	 * four capturing groups, representing a player's move.
	 */
	private static final Pattern inputPattern = Pattern.compile(
			"^\\s*([A-Z]+)\\s*(\\d+)(?:\\s*([A-Z]+)\\s*(\\d+))+\\s*$",
			Pattern.CASE_INSENSITIVE);
	
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
	
	/**
	 * Constructs a Move by parsing a String of human input.
	 * Input is in the form <code>"" + oldFile + oldRank + " " +
	 * newFile1 + newRank1 + " " + newFile2 + newRank2 ...</code>,
	 * as described by {@link Move#inputPattern inputPattern},
	 * where the files are letters, the ranks are one-based
	 * numbers, and every file/rank pair after the second pair
	 * (newFile1 + newRank1) represents a double jump (triple,
	 * etc.).
	 * @param humanMove the String to parse.
	 * @throws IllegalArgumentException If input is invalid.
	 */
	public Move(String humanMove) throws IllegalArgumentException {
		newSquares = new DSArrayList<Point>();
		
		Matcher match = inputPattern.matcher(humanMove);
		if (match.find()) { // String consists of valid input
			oldSquare = new Point(parseCol(match.group(1)),
					parseRow(match.group(2)));
			
			// FIXME  Doesn't parse double-jumps correctly
			for (int i = 3; i <= match.groupCount(); i += 2) {
				newSquares.add(new Point(parseCol(match.group(i)),
						parseRow(match.group(i+1))));
			}
		} else {
			throw new IllegalArgumentException(
				"Invalid input. Input must be in the following form:" +
				"\n<startRow><startCol> <nextRow><nextCol> " +
				"[<nextRow><nextCol> ...]");
		}
	}
	
	/**
	 * Converts the given string into a number 
	 * representing a rank on the board.
	 * @param boardRank The string to parse.
	 * @return The int in question.
	 */
	private int parseRow(String boardRank) {
		return Checkers.HEIGHT - Integer.valueOf(boardRank);
	}
	
	/**
	 * Turns a String of letters into a number, such that
	 * <code>"A" => 0, "B" => 1, "C" => 2, ... "Z" => 25,
	 * "AA" => 26, "AB" => 27 ...</code> Case-insensitive.
	 * @param boardFile The string to parse.
	 * @return The int in question.
	 */
	int parseCol(String boardFile) {
		boardFile = boardFile.toUpperCase(java.util.Locale.ENGLISH);
		String base26 = "";
		
		// Convert to base-26 String
		for (int i = 0; i < boardFile.length(); i++) {
			int intVal = (int) boardFile.charAt(i);
			
			if (intVal < 10 + Checkers.ASCII_A)
				base26 += intVal - Checkers.ASCII_A;
			else
				base26 += intVal - 10;
		}
		
		// Parse the base-26 String
		return Integer.valueOf(base26, 26);
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
