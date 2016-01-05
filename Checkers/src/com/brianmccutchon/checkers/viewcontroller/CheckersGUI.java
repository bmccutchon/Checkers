package com.brianmccutchon.checkers.viewcontroller;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Semaphore;

import javax.swing.*;

import com.brianmccutchon.checkers.model.Checkers;
import com.brianmccutchon.checkers.model.Move;
import com.brianmccutchon.checkers.model.Preferences;

/**
 * Implements the game of checkers in a GUI.
 * @author Brian McCutchon
 * @version 0.1.0
 */
public class CheckersGUI extends Checkers {

	/**
	 * The length of a side of a square on the board.
	 */
	public static final int SQUARE_SIZE = 100;
	
	/**
	 * The margin around the board.
	 */
	public static final int BOARD_MARGIN = 0;
	
	/**
	 * The width of the border around the board.
	 */
	public static final int BOARD_BOARDER = 0;
	
	/**
	 * ImageIcon representing a dark square.
	 */
	public final ImageIcon DARK_SQUARE_IMG =
			new ImageIcon(getClass().getResource(
					"/resources/dark-square.png"));
	
	/**
	 * ImageIcon representing a light square.
	 */
	public final ImageIcon LIGHT_SQUARE_IMG =
			new ImageIcon(getClass().getResource(
					"/resources/light-square.png"));
	
	/**
	 * The main window.
	 */
	JFrame gui;
	
	/**
	 * The panel that holds the board.
	 */
	CheckerBoardGUI boardGUI;
	
	/**
	 * Holds the buttons representing squares on the board.
	 */
	JButton[][] squares;
	
	/**
	 * Semaphore used to wait for human input
	 * in {@link CheckersGUI#getHumanMove()}.
	 */
	Semaphore holdForHuman = new Semaphore(0);
	
	/**
	 * The coordinates of the last clicked square.
	 */
	Point lastClicked = new Point();

	public void undo(ActionEvent e) {
		if (undoStack.size() == 0) { // redundant case
			undoItem.setEnabled(false);
			return;
		}

		if (undoStack.size() != 1) { // go back two boards
			undoStack.pop(); 
		}

		board = undoStack.pop();

		if (undoStack.size() == 0) {
			undoItem.setEnabled(false);
		}

		drawBoard();
	}

	private MenuItem undoItem;
	
	/**
	 * <code>true</code> if it is the human's turn.
	 * If so, the dark square buttons will respond to button presses.
	 */
	private boolean isAcceptingInput = false;
	
	/*
	 * ActionListener that starts a new game.
	 * Prompts the user about what sort of game, etc.
	 */
	/*public void newGame(ActionEvent arg0) {
		// TODO Write method newGame()
		System.out.println("User wants to start a new game");
		//new CheckersGUI();
	}*/
	
	public CheckersGUI(boolean applet) {
		super(Preferences.load());
		
		gui = new JFrame();
		
		//gui.setUndecorated(true);
		//gui.setBackground(new Color(0, 0, 0, 0));
		
		// Lets the OS decide where to put the window
		gui.setLocationByPlatform(true);
		
		if (!applet) {
			// The app closes when the window closes.
			gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		
		// TODO Add resize handler
		//gui.setResizable(false);
		
		// Create the board
		boardGUI = new CheckerBoardGUI();
		boardGUI.setPreferredSize(new Dimension(WIDTH * SQUARE_SIZE, HEIGHT * SQUARE_SIZE));
		boardGUI.setLayout(new GridLayout(HEIGHT, WIDTH)); // Absolute positioning

		gui.add(boardGUI);
		
		squares = new JButton[HEIGHT][WIDTH];
		
		for (int row = 0; row < HEIGHT; row++) {
			for (int col = 0; col < WIDTH; col++) {
				squares[row][col] = new JButton();
				squares[row][col].addActionListener(new SquareListener(col, row));
				
				squares[row][col].setContentAreaFilled(false);
				squares[row][col].setBorderPainted(false);
				/*squares[row][col].setBounds(
						col * SQUARE_SIZE,
						row * SQUARE_SIZE,
						SQUARE_SIZE, SQUARE_SIZE);*/
				
				if (row%2+col%2 == 1) {
					squares[row][col].setIcon(DARK_SQUARE_IMG);
				} else {
					squares[row][col].setIcon(LIGHT_SQUARE_IMG);
				}
				
				/*
				 * This is for right-click double jumping
				squares[row][col].addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						// T/ODO Write method mousePressed()
						if (e.isPopupTrigger()) {
							System.out.println("Right click at " + x + ", " + y);
						}
					}
				});
				*/
				
				boardGUI.add(squares[row][col]);
			}
		}
		
		MenuBar menus = new MenuBar();
		gui.setMenuBar(menus);
		
		Menu gameMenu = menus.add(new Menu("Game"));
		
		//MenuItem newGame = gameMenu.add(new MenuItem("New Game"));
		//newGame.addActionListener(newGameStarter);
		
		undoItem = gameMenu.add(new MenuItem("Undo"));
		undoItem.addActionListener(this::undo);
		undoItem.setEnabled(false);
		
		/*
		Menu edit = new Menu("Edit");
		MenuItem save = new MenuItem("Save");
		menus.add(edit);
		edit.add(save);
		save.addActionListener();
		*/
		
		gui.pack();
		
		gui.setVisible(true);
		
		play();
		
		if (endstate == PLAYER1WIN)
			gui.setTitle("Black wins!");
		else
			gui.setTitle("Red wins!");
	}

	@Override
	protected Move getHumanMove() {
		this.isAcceptingInput = true;
		
		waitForHuman();
		
		byte myKing = (board[0][0] == P1_PAWN) ? P1_KING : P2_KING;
		if (board[lastClicked.y + 1][lastClicked.x / 2] != board[0][0] &&
				board[lastClicked.y + 1][lastClicked.x / 2] != myKing) {
			tellUserMoveIsInvalid();
			return getHumanMove();
		}
		
		Move m = new Move(lastClicked.x, lastClicked.y);
		
		Point oldSquare = m.oldSquare;
		boolean canJump;
		
		do { // Get multiple jumps
			waitForHuman();
			// Creating new Point instance
			Point newSquare = new Point(lastClicked);
			m.newSquares.add(newSquare);
			
			byte[][] newBoard = cloneBoard(board);
			
			makeMove(m, newBoard, false);
			
			// canJump is set to true if a jump is possible 
			// and the last move was a jump.
			canJump = Math.abs(oldSquare.x - newSquare.x) == 2 &&
					Math.abs(oldSquare.y - newSquare.y) == 2 &&
					getJumps(newBoard, newSquare.y, newSquare.x).size() != 0;
			
			oldSquare = newSquare;
		} while (canJump);
		
		this.isAcceptingInput = false;
		
		return m;
	}
	
	/**
	 * Acquires a permit from the {@link #holdForHuman}
	 * Semaphore, with error handling.
	 */
	private void waitForHuman() {
		try {
			holdForHuman.acquire();
		} catch (InterruptedException e) {
			System.out.println("Thread interrupted");
			e.printStackTrace();
		}
	}

	@Override
	protected void tellUserMoveIsInvalid() {
		Toolkit.getDefaultToolkit().beep();
	}

	@Override
	protected void drawBoard() {
		boardGUI.drawBoard(board);
		
		/*
		for (int row = 0; row < squares.length; row++) {
			// The count starts at one for odd rows or zero for even rows
			// and increases by 2 in order to skip light squares.
			for (int col = 1-row%2; col < squares[row].length; col+=2) {
				squares[row][col].setText("" + board[row+1][col/2]);
			}
		}*/
		
		gui.setTitle((board[0][0] == P1_PAWN) ?
				"Black's turn" : "Red's turn");
		
		if (undoStack.size() != 0) {
			undoItem.setEnabled(true);
		}
	}
	
	/**
	 * Creates a new CheckersGUI instance.
	 * @param args
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new CheckersGUI(false);
		//SwingUtilities.invokeLater(CheckersGUI::new);
	}
	
	private class SquareListener implements ActionListener {
		
		private int x, y;
		
		public SquareListener(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (x%2+y%2 == 1) { // Dark square
				if (isAcceptingInput) {
					lastClicked.x = x;
					lastClicked.y = y;
					holdForHuman.release();
				} else { // Not your turn!
					Toolkit.getDefaultToolkit().beep();
				}
			} else { // Light square
				if (lastClicked.x == 0 && lastClicked.y == 0) {
					if (y == HEIGHT - 1 && x == WIDTH - 1) {
						boardGUI.easterEggEnabled = true;
						boardGUI.repaint();
					}
				} else { 
					lastClicked.x = x;
					lastClicked.y = y;
				}
			}
		}

	}
	
}
