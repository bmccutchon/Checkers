package com.brianmccutchon.checkers;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * This panel holds the checkerboard and
 * handles painting the pieces on the board.
 * @author Brian McCutchon
 * @version 0.1.0
 */
public class CheckerBoardGUI extends JPanel {

	private static final long serialVersionUID =
			5248081983546538071L;

	private Image drAndrews;

	public CheckerBoardGUI() {
		try {
			drAndrews = ImageIO.read(getClass()
					.getResource("/resources/drAndrews.png"))
					.getScaledInstance(
							CheckersGUI.SQUARE_SIZE,
							CheckersGUI.SQUARE_SIZE,
							Image.SCALE_SMOOTH);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Draws an oval that is half the size of a
	 * checkerboard square using the graphics
	 * context provided.
	 * @param g The Graphics2d with which to draw.
	 */
	/*private static void drawPiece(Graphics2D g) {
		g.fillOval(0, 0,
				CheckersGUI.SQUARE_SIZE / 2,
				CheckersGUI.SQUARE_SIZE / 2);
	}*/

	/*
	 * @return A BufferedImage that is half the
	 * size of a checkerboard square.
	 */
	/*private static BufferedImage makeBuffImg() {
		return new BufferedImage(
				CheckersGUI.SQUARE_SIZE/2,
				CheckersGUI.SQUARE_SIZE/2,
				BufferedImage.TYPE_INT_RGB);
	}*/

	/**
	 * The board which will be used to
	 * determine where to paint the pieces.
	 * @see Checkers#board
	 */
	private byte[][] board;

	boolean easterEggEnabled;

	@Override
	public void paint(Graphics g1) {
		super.paint(g1);
		Graphics2D g = (Graphics2D) g1;
		for (int row = 0; row < Checkers.HEIGHT; row++) {
			// The count starts at one for odd rows or zero for even
			// rows and increases by 2 in order to skip light squares.
			for (int col = 1-row%2; col < Checkers.WIDTH; col+=2) {
				//board[row][col].setText("" + board[row+1][col/2]);
				boolean isKing = false;
				switch (board[row+1][col/2]) {
					case Checkers.P1_KING:
						isKing = true;
					case Checkers.P1_PAWN:
						g.setColor(Color.BLACK);
						break;
					case Checkers.P2_KING:
						isKing = true;
					case Checkers.P2_PAWN:
						if (easterEggEnabled) {
							g.drawImage(drAndrews,
									col * CheckersGUI.SQUARE_SIZE,
									row * CheckersGUI.SQUARE_SIZE, null);
							if (isKing)
								drawCrown(row, col, g, 0);
							continue;
						}
						g.setColor(Color.RED);
						break;
					default: continue;
				}

				g.fillOval(
						(int)((col + 0.25)*CheckersGUI.SQUARE_SIZE),
						(int)((row + 0.25)*CheckersGUI.SQUARE_SIZE),
						CheckersGUI.SQUARE_SIZE / 2,
						CheckersGUI.SQUARE_SIZE / 2);

				if (isKing)
					drawCrown(row, col, g, 0.5);
			}
		}
	}

	/**
	 * Draws a board to the screen.
	 * @param board The board to draw.
	 */
	public void drawBoard(byte[][] board) {
		this.board = board;
		repaint();
	}

	/**
	 * Draws a crown for a piece at the specified position.
	 * @param row The row of the piece.
	 * @param col The column of the piece.
	 * @param percentY The distance from the top of the square
	 * as a percentage of the height of the square,
	 * where <code>1.0</code> equals 100%.
	 * @param g The Graphics2D with which to draw.
	 */
	private void drawCrown(int row, int col, Graphics2D g, double percentY) {
		g.setColor(Color.YELLOW);
		Polygon crown = new Polygon(
				new int[]{-20, -10,  0, 10, 20, 16, -16},
				new int[]{ -14,  0, -14, 0, -14, 12,  12}, 7);
		crown.translate(
				(int)((col + 0.5)*CheckersGUI.SQUARE_SIZE),
				(int)((row + percentY)*CheckersGUI.SQUARE_SIZE));
		g.fill(crown);
	}

}
