package com.brianmccutchon.checkers.model;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import com.brianmccutchon.checkers.model.Checkers;
import com.brianmccutchon.checkers.model.Preferences;

public class CheckersTest {
	
	byte o = Checkers.UNOCCUPIED_SQUARE;
	byte b = Checkers.P1_PAWN;
	byte B = Checkers.P1_KING;
	byte r = Checkers.P2_PAWN;
	byte R = Checkers.P2_KING;
	
	Checkers c = new Checkers(new Preferences(7, true, true, "Normal"));
	
	@Test
	public void testEvaluateBoard() {
		assertEquals("A board with no players should be " +
				"a loss for P1 if it is P1's turn.",
				Checkers.LOSS, c.evaluateBoard(new byte[][]{
					{b, 0, 0, 0},
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
				}));
		
		assertEquals("A board with no players should be " +
				"a loss for P2 if it is P2's turn.",
				Checkers.LOSS, c.evaluateBoard(new byte[][]{
					{r, 0, 0, 0},
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
				}));
		
		assertEquals("The initial board should have a score of 0.",
				0, c.evaluateBoard(new byte[][]{
					{b, 0, 0, 0},
					{   r , r , r , r },
					{ r , r , r , r   },
					{   r , r , r , r },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ b , b , b , b   },
					{   b , b , b , b },
					{ b , b , b , b   },
				}));
		
		assertEquals("A balanced board should have a score of 0.",
				0, c.evaluateBoard(new byte[][]{
					{b, 0, 0, 0},
					{   r , r , r , r },
					{ r , r , r , r   },
					{   o , r , r , r },
					{ r , o , o , o   },
					{   o , o , o , b },
					{ b , b , b , o   },
					{   b , b , b , b },
					{ b , b , b , b   },
				}));
		
		assertEquals("If the depth (board[0][1]) is not zero, " +
				"a loss should be assumed.",
				Checkers.LOSS, c.evaluateBoard(new byte[][]{
					{b, 1, 0, 0},
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , R , o },
					{ o , o , o , o   },
					{   o , B , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
				}));
		
		assertEquals("If it's your turn and you have no pieces, you lose.",
				Checkers.LOSS, c.evaluateBoard(new byte[][]{
					{b, 0, 0, 0},
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , R , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
				}));
		
		assertEquals("If it's your turn and you cannot make a move, you lose.",
				Checkers.LOSS, c.evaluateBoard(new byte[][]{
					{r, 0, 0, 0},
					{   o , o , R , o },
					{ o , o , B , b   },
					{   o , b , o , b },
					{ r , o , o , o   },
					{   b , o , o , o },
					{ o , b , o , o   },
					{   B , o , o , o },
					{ R , o , o , o   },
				}));
		
		assertEquals("Kings on the edge of the board are less valuable by two.",
				Checkers.KING_VALUE-2, c.evaluateBoard(new byte[][]{
					{r, 0, 0, 0},
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , R },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
				}));
		
		assertEquals("Kings on the edge of the board are less valuable by two.",
				2, c.evaluateBoard(new byte[][]{
					{b, 0, 0, 0},
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , B , o , o },
					{ o , o , o , o   },
					{   o , o , o , R },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
				}));
		
		assertEquals("Kings near the edge of the board are less valuable by one.",
				1, c.evaluateBoard(new byte[][]{
					{b, 0, 0, 0},
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , B , o , o },
					{ o , o , o , R   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
				}));
		
		// Squares 1, 5, 28, and 32 on an 8x8 board do not count as edges
		// because it is impossible to trap a king there with a single king.
		// See Checkers.isAlongEdge().
		assertEquals("Kings in the top-left and bottom-right are less valuable " +
				"by one. (See comment accompanying this test.)",
				1, c.evaluateBoard(new byte[][]{
					{b, 0, 0, 0},
					{   o , o , o , o },
					{ R , o , o , o   },
					{   o , B , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
				}));
		
		// Squares 1, 5, 28, and 32 on an 8x8 board do not count as edges
		// because it is impossible to trap a king there with a single king.
		// See Checkers.isAlongEdge().
		assertEquals("Kings in the top-left and bottom-right are less valuable " +
				"by one. (See comment accompanying this test.)",
				1, c.evaluateBoard(new byte[][]{
					{b, 0, 0, 0},
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , B , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , R },
					{ o , o , o , o   },
				}));
	}
	
	@Ignore
	@Test
	public void testPawnValues() {
		/* ---------- Black's point of view ---------- */
		
		assertEquals("Black pawns on the 1st rank should have a value of 3.",
				3, c.evaluateBoard(new byte[][]{
					{b, 0, 0, 0},
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , B , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , R , o   },
					{   o , o , o , o },
					{ o , o , o , b   },
				}));
		
		assertEquals("Black pawns on the 2nd rank should have a value of 2.",
				2, c.evaluateBoard(new byte[][]{
					{b, 0, 0, 0},
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , b , o },
					{ o , o , o , o   },
				}));
		
		assertEquals("Black pawns on the 3rd rank should have a value of 2.",
				2, c.evaluateBoard(new byte[][]{
					{b, 0, 0, 0},
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ b , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
				}));
		
		assertEquals("Black pawns on the 4th rank should have a value of 2.",
				2, c.evaluateBoard(new byte[][]{
					{b, 0, 0, 0},
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , b , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
				}));
		
		assertEquals("Black pawns on the 5th rank should have a value of 3.",
				3, c.evaluateBoard(new byte[][]{
					{b, 0, 0, 0},
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , b   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
				}));
		
		assertEquals("Black pawns on the 6th rank should have a value of 4.",
				4, c.evaluateBoard(new byte[][]{
					{b, 0, 0, 0},
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , b },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
				}));
		
		assertEquals("Black pawns on the 7th rank should have a value of 5.",
				5, c.evaluateBoard(new byte[][]{
					{b, 0, 0, 0},
					{   o , o , o , o },
					{ o , b , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
				}));
		
		/* ---------- Red's point of view ---------- */
		
		assertEquals("Red pawns on the 8th rank should have a value of 3." +
				3, c.evaluateBoard(new byte[][]{
					{r, 0, 0, 0},
					{   o , r , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
					{   o , o , o , o },
					{ o , o , o , o   },
				}));
	}
	
	@Test
	public void testConstants() {
		assertTrue("The win constant should be higher than the " +
				"value of the maximum number of kings on a 10x10 board.",
				Checkers.WIN > Checkers.KING_VALUE * 20);
	}

}
