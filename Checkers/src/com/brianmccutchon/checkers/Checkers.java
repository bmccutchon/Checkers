package com.brianmccutchon.checkers;

import java.awt.Point;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Stream;

import framework.DSArrayList;
import framework.DSNode;
import framework.TwoPlayer;

/**
 * <p>
 * Implements the game of Checkers in a CLI.
 * </p><p>
 * The specification for {@link Game#board} is that it is a
 * 2D array of bytes, where WHITE_SQUARE is an unoccupied
 * square and all pieces are represented by constants.
 * White squares are omitted, as pieces cannot rest on
 * them. Index 0 of the array is an array of metadata.
 * </p><p>
 * <code>board[0][0]</code> is equal to {@link
 * Checkers.P1_PAWN P1_PAWN} if it is player 1's
 * turn and {@link Checkers.P2_PAWN P2_PAWN} if
 * it is player 2's turn.
 * </p><p>
 * <code>board[0][1]</code> represents how how deep the
 * tree should go from this board; it is set to
 * {@link Checkers#maxTreeDepth maxTreeDepth} in
 * {@link Checkers#computerMove(int) computerMove()}
 * and decremented for each child in
 * {@link Checkers#getChildren(Object)
 * getChildren()}.
 * </p>
 * @author Brian McCutchon
 * @version 0.1.0
 */
public class Checkers extends TwoPlayer<byte[][]> {

	/**
	 * The maximum depth of a tree, or the number
	 * of moves that the computer should think ahead.
	 * @see #board
	 */
	protected byte maxTreeDepth = 6;

	/**
	 * <code>true</code> if the game is suicide checkers,
	 * in which case the object of the game is to lose.
	 */
	private boolean isSuicideCheckers = false;

	@Override
	protected void getHumanOrComputer() {
		isHuman[1] = prefs.p1IsHuman;
		isHuman[2] = prefs.p2IsHuman;
	}

	/**
	 * Scanner for getting input.
	 */
	Scanner scan = new Scanner(System.in);

	/**
	 * The undo stack; holds boards for every other turn.
	 */
	protected DSArrayList<byte[][]> undoStack;

	/**
	 * The preferences of this game.
	 */
	private Preferences prefs;

	/**
	 * The height of the board, including white squares.
	 * @see #WIDTH
	 */
	public static final int HEIGHT = 8;

	/**
	 * The width of the board, including white squares.
	 * @see #HEIGHT
	 */
	public static final int WIDTH = 8;

	/**
	 * The number of rows of pieces on 
	 * each side at the start of a game.
	 */
	public static final int NUM_RANKS = 3;

	/**
	 * A <code>byte</code> representing one of
	 * player 1's pawns.
	 * @see #P1_KING
	 * @see #P2_PAWN
	 * @see #P2_KING
	 */
	public static final byte P1_PAWN = 1;

	/**
	 * A <code>byte</code> representing one of
	 * player 1's kings.
	 * @see #P1_PAWN
	 * @see #P2_PAWN
	 * @see #P2_KING
	 */
	public static final byte P1_KING = 2;

	/**
	 * A <code>byte</code> representing one of
	 * player 2's pawns.
	 * @see #P1_PAWN
	 * @see #P1_KING
	 * @see #P2_KING
	 */
	public static final byte P2_PAWN = 7;

	/**
	 * A <code>byte</code> representing one of
	 * player 2's kings.
	 * @see #P1_PAWN
	 * @see #P1_KING
	 * @see #P2_PAWN
	 */
	public static final byte P2_KING = 8;

	/**
	 * A value representing a win.
	 * @see #LOSS
	 * @see #CONT
	 */
	public static final int WIN = 1000;

	/**
	 * A value representing a loss.
	 * @see #WIN
	 * @see #CONT
	 */
	public static final int LOSS = -1000;

	/**
	 * The value of a regular piece, used by
	 * {@link #evaluateBoard(Object)} and
	 * {@link #evaluateNode(DSGameNode)}
	 * to score an incomplete game.
	 * Must be an even number.
	 * @see #KING_VALUE
	 */
	public static final int PAWN_VALUE = 2;

	/**
	 * The value of a king, used by
	 * {@link #evaluateBoard(Object)} and
	 * {@link #evaluateNode(DSGameNode)}
	 * to score an incomplete game.
	 * Must be an even number.
	 * @see #PAWN_VALUE
	 */
	public static final int KING_VALUE = 8;

	/**
	 * Represents an unoccupied square.
	 */
	public static final byte UNOCCUPIED_SQUARE = 0;

	/**
	 * Represents a square on which a checker piece cannot rest.
	 */
	public static final char ILLEGAL_SQUARE = ' ';

	/**
	 * The ASCII value of <code>'A'</code>.
	 */
	public static final int ASCII_A = 65;

	/**
	 * The number of memos to store. If this limit is reached, old memos will
	 * be deleted.
	 */
	private static final int NUM_MEMOS = 100_003;

	/**
	 * Creates a new Checkers game with the given preferences.
	 * @param prefs The game settings
	 */
	public Checkers(Preferences prefs) {
		// The board's width is cut in half to
		// eliminate white squares, and its height
		// is increased by one to accommodate metadata.
		board = new byte[HEIGHT + 1][WIDTH / 2];

		maxTreeDepth = (byte) prefs.treeDepth;
		isSuicideCheckers = (prefs.modeName == "Suicide");
		this.prefs = prefs;
	}

	@Override
	public void play() {
		for (int i = 1; i<=HEIGHT; i++) {
			if (i <= NUM_RANKS) { // fill the p2 pieces
				Arrays.fill(board[i], P2_PAWN);
			} else if (i-1 >= HEIGHT - NUM_RANKS) { // p1 pieces
				Arrays.fill(board[i], P1_PAWN);
			} else { // unoccupied squares
				Arrays.fill(board[i], UNOCCUPIED_SQUARE);
			}
		}

		/*byte _ = Checkers.UNOCCUPIED_SQUARE;
		byte b = Checkers.P1_PAWN;
		byte B = Checkers.P1_KING;
		byte r = Checkers.P2_PAWN;
		byte R = Checkers.P2_KING;

		board = new byte[][]{
				{b, 0, 0, 0},
				{   B , B , B , _ },
				{ _ , _ , _ , _   },
				{   _ , _ , _ , _ },
				{ _ , _ , _ , _   },
				{   _ , _ , _ , _ },
				{ _ , _ , _ , _   },
				{   _ , _ , _ , _ },
				{ _ , R , R , R   },
		};*/

		board[0][0] = P1_PAWN; // It is player one's turn

		undoStack = new DSArrayList<byte[][]>();
		undoStack.add(cloneBoard(board));

		boardValues = new LimitedMap<>(NUM_MEMOS);
		boardNodes  = new LimitedMap<>(NUM_MEMOS);

		MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
		MemoryUsage usage = bean.getHeapMemoryUsage();
		System.out.println(usage.getMax() / 1000);

		super.play();
	}

	@Override
	protected void humanMove(int turn) {
		while (true) {
			Move m = getHumanMove();
			if (moveIsLegal(m)) {
				undoStack.add(cloneBoard(board));
				makeMove(m, board, true);
				break;
			} else {
				tellUserMoveIsInvalid();
			}
		}
	}

	// TODO Remove after debugging
	public static void drawBoard(byte[][] board) {
		System.out.println();
		for (int row = 1; row <= HEIGHT; row++) {
			if (row % 2 == 1) // Odd numbered row
				System.out.print(" " + ILLEGAL_SQUARE);
			for (int col = 0; col < WIDTH / 2; col++)
				System.out.print(" " + board[row][col] +
					((col == WIDTH/2-1 && row % 2 == 1) ?
							"" : " " + ILLEGAL_SQUARE));
			System.out.println(" " + (HEIGHT - row + 1));
		}
		for (int col = 0; col < WIDTH; col++) {
			System.out.print(" " + intToAlphaString(col));
		}

		System.out.println();
	}

	/**
	 * Determines whether a given move is legal.
	 * @param m
	 *   The {@link Move Move} to evaluate.
	 * @return
	 *   <code>true</code> if <code>m</code> is legal.
	 */
	protected boolean moveIsLegal(Move m) {
		return getLegalMoves(board).contains(m);
	}

	/**
	 * Gets the human's move, including double jumping,
	 * if applicable. Intended to be overridden by
	 * GUI subclasses.
	 * @return A Move with 0-based coordinates on the board.
	 * @see Checkers#humanMove(int)
	 */
	protected Move getHumanMove() {
		if (scan.hasNextLine()) {
			try {
				return new Move(scan.nextLine());
			} catch(IllegalArgumentException e) {
				// The user gave invalid input
				System.out.println(e.getMessage());
				return getHumanMove(); // recurse
			}
		} else { // End of input
			System.exit(0); // Quit the program
			return null; // Probably unreachable, but makes the compiler happy
		}
	}

	/**
	 * Tells the user that s/he has attempted to make an
	 * illegal move.
	 * Intended to be overridden by GUI subclasses.
	 * @return A Move with 0-based coordinates on the board.
	 * @see Checkers#humanMove(int)
	 */
	protected void tellUserMoveIsInvalid() {
		System.out.println("Sorry, that move is illegal.");
	}

	/*
	 * TODO Improvements to computerMove():
	 * 1) Use a continuously-built tree.
	 * 2) Use better game tree nodes.
	 * 3) Add the following logic: Good things are better if they
	 *    come sooner. Bad things are better if they come later.
	 *    Depends on (2).
	 *    Will probably be instituted in evaluateNode().
	 * 4) Fix field-of-vision problem: Currently, the computer
	 *    thinks that sacrificing its pieces will prevent an
	 *    inevitable evil if sacrifing said pieces pushes said
	 *    evil beyond the depth of the tree, and thus out of the
	 *    computer's field of vision. Presumably, this would also
	 *    cause the computer to allow its opponent to take its
	 *    pieces if it sees itself getting a king just within
	 *    its field of vision.
	 */
	@Override
	protected void computerMove(int turn) {
		undoStack.add(board);

		/*int numPieces = 0;
		for (int row = 1; row < board.length; row++) {
			for (int col = 0; col < board[row].length; col++) {
				if (board[row][col] != UNOCCUPIED_SQUARE) {
					numPieces++;
				}
			}
		}
		if (numPieces < 5) {
			maxTreeDepth = 20;
		}*/

		board[0][1] = maxTreeDepth;

		super.computerMove(turn);

		MemoryUsage heapUsage = ManagementFactory
				.getMemoryMXBean().getHeapMemoryUsage();
		System.out.println("Memory used: " +
				heapUsage.getUsed() / 1_000_000 + "m");
		
		System.out.println("Number of memos: " + boardNodes.size());

		/*DSArrayList<DSGameNode<byte[][]>> candidates =
				new DSArrayList<DSGameNode<byte[][]>>();

		int topValue = Integer.MIN_VALUE;

		DSArrayList<byte[][]> children = getChildren(board);

		// If there's only one move, take it.
		if (children.size() == 1) {
			board = children.get(0);
			return;
		}

		for (byte[][] b : children) {
			DSGameNode<byte[][]> node = buildTree(b);
			int val = evaluateNode(node);
			if (val > topValue) {
				topValue = val;
				candidates = new DSArrayList<DSGameNode<byte[][]>>();
				candidates.add(node);
			} else if (val == topValue)
				candidates.add(node);
		}

		board = cloneBoard((byte[][]) candidates.get(
				(int)(Math.random() * candidates.size())).returnThing());*/
	}

	/**
	 * Makes a move on a board.
	 * @param move The move to make.
	 * @param board The board on which to make it.
	 * @param endOfTurn
	 *   <code>true</code> if this move ends the turn. If so, the method changes
	 *   whose turn it is and performs a coronation if necessary.
	 */
	protected static void makeMove(Move move,
			byte[][] board, boolean endOfTurn) {
		move.toArrayIndices();
		byte piece = board[move.oldSquare.y][move.oldSquare.x];

		Point oldSquare = move.oldSquare;

		for (int i = 0; i < move.newSquares.size(); i++) {
			Point newSquare = move.newSquares.get(i);

			// Promote if necessary.
			if (endOfTurn && (newSquare.y == 1 || newSquare.y == HEIGHT))
				if (piece == P1_PAWN)
					piece = P1_KING;
				else if (piece == P2_PAWN)
					piece = P2_KING;

			board[newSquare.y][newSquare.x] = piece;
			board[oldSquare.y][oldSquare.x] = UNOCCUPIED_SQUARE;

			Point jumped = getJumpedSquare(oldSquare, newSquare);
			if (jumped != null) // remove the jumped piece
				board[jumped.y][jumped.x] = UNOCCUPIED_SQUARE;

			oldSquare = newSquare; // Preparing for next jump
		}

		if (endOfTurn)
			changeTurn(board);
		move.toBoardValues();
	}

	/**
	 * Changes whose turn it is on the board.
	 * @param board
	 */
	private static void changeTurn(byte[][] board) {
		board[0][0] = (board[0][0] == P1_PAWN) ? P2_PAWN : P1_PAWN;
	}

	/**
	 * Returns the a Point representing the square that has been jumped
	 * if a jump has occurred, or <code>null</code> if no jump has occurred. 
	 * @param oldSquare The square from which the piece has moved.
	 * @param newSquare The square to which the piece is moving.
	 * @return The coordinates of the jumped square as an array
	 * index or <code>null</code> if no piece was jumped.
	 */
	private static Point getJumpedSquare(Point oldSquare, Point newSquare) {
		oldSquare = new Point(oldSquare.x * 2 + oldSquare.y%2, oldSquare.y);
		newSquare = new Point(newSquare.x * 2 + newSquare.y%2, newSquare.y);

		if (Math.abs(oldSquare.x - newSquare.x) == 2 &&
				Math.abs(oldSquare.y - newSquare.y) == 2) {
			return new Point((oldSquare.x + newSquare.x) / 4,
					(oldSquare.y + newSquare.y) / 2);
		} else {
			return null;
		}
	}

	@Override
	protected void drawBoard() {
		System.out.println();
		for (int row = 1; row <= HEIGHT; row++) {
			if (row % 2 == 1) // Odd numbered row
				System.out.print(" " + ILLEGAL_SQUARE);
			for (int col = 0; col < WIDTH / 2; col++)
				System.out.print(" " + board[row][col] +
					((col == WIDTH/2-1 && row % 2 == 1) ?
							"" : " " + ILLEGAL_SQUARE));
			System.out.println(" " + (HEIGHT - row + 1));
		}
		for (int col = 0; col < WIDTH; col++) {
			System.out.print(" " + intToAlphaString(col));
		}

		System.out.println();
	}

	@Override
	protected int endCheck() {
		if (!moveIsPossible(board)) {
			return ((board[0][0] == P1_PAWN) ^ isSuicideCheckers) ?
					PLAYER2WIN : PLAYER1WIN;
		}

		return CONTINUE;
	}

	@Override
	protected DSArrayList<byte[][]> getChildren(byte[][] b) {
		DSArrayList<byte[][]> boards = new DSArrayList<byte[][]>();

		if (b[0][1] == 0) // depth limit reached
			return boards;

		for (Move m : getLegalMoves(b)) {
			byte[][] lb = cloneBoard(b);
			lb[0][1]--;
			makeMove(m, lb, true);
			boards.add(lb);
		}

		return boards;
	}

	/**
	 * Returns all possible moves for the player whose
	 * turn it is on the given board.
	 * @param localBoard The board.
	 * @return A DSArrayList of all possible Moves.
	 * @see Checkers#getChildren(Object)
	 */
	private DSArrayList<Move> getLegalMoves(byte[][] localBoard) {
		DSArrayList<Move> moves = new DSArrayList<Move>();

		// Define the pieces belonging to the player whose turn it is
		final byte myPawn = localBoard[0][0];
		final byte myKing = (localBoard[0][0] == P1_PAWN) ? P1_KING : P2_KING;

		// Find all possible jumps.
		for (int row = 0; row < HEIGHT; row++) {
			// The count starts at one for odd rows or zero for even
			// rows and increases by 2 in order to skip light squares.
			for (int col = 1-row%2; col < WIDTH; col+=2) {
				if (localBoard[row+1][col/2] == myPawn ||
						localBoard[row+1][col/2] == myKing) {
					moves.append(getMultipleJumps(localBoard, row, col));
				}
			}
		}

		// If no jumps were found, find all possible normal moves.
		if (moves.size() == 0) { // TODO Add mode for non-forced jumping.
			// Find all possible non-jump moves
			for (int row = 0; row < HEIGHT; row++) {
				// The count starts at one for odd rows or zero for even
				// rows and increases by 2 in order to skip light squares.
				for (int col = 1-row%2; col < WIDTH; col+=2) {
					if (localBoard[row+1][col/2] == myKing) {
						addMoves(localBoard, row, col, true,  moves);
						addMoves(localBoard, row, col, false, moves);
					} else if (localBoard[row+1][col/2] == myPawn) {
						addMoves(localBoard, row, col,
								myPawn == P1_PAWN, moves);
					}
				}
			}
		}

		return moves;
	}

	/**
	 * Finds all possible sequences of jumps or
	 * multiple jumps in one turn for a given piece.
	 * @param b The board in question.
	 * @param row The row of the piece in question.
	 * @param col The column of the piece in question.
	 * @return A DSArrayList containing all
	 * legal moves involving jumps.
	 */
	private DSArrayList<Move> getMultipleJumps(
			byte[][] b, int row, int col) {

		// Define the pieces belonging to the player whose turn it is
		final byte myPawn = b[0][0];
		final byte myKing = (b[0][0] == P1_PAWN) ? P1_KING : P2_KING;

		// Get the first jump.
		DSArrayList<Move> singleJumps;

		if (b[row+1][col/2] == myKing) {
			singleJumps = getJumps(b, row, col, true);
			singleJumps.append(getJumps(b, row, col, false));
		} else if (b[row+1][col/2] == myPawn) {
			singleJumps = getJumps(b, row, col,
					myPawn == P1_PAWN);
		} else { // Not my piece -> no legal moves
			singleJumps = new DSArrayList<Move>();
		}

		// Get the multiple jumps recursively.
		DSArrayList<Move> multiJumps = new DSArrayList<Move>();

		for (Move m : singleJumps) {
			byte[][] newBoard = cloneBoard(b);
			makeMove(m, newBoard, false);

			// recurse
			DSArrayList<Move> moves = getMultipleJumps(newBoard,
					m.newSquares.get(0).y, m.newSquares.get(0).x);

			if(moves.size() == 0) // no multiple jumps exist
				moves.add(m);
			else
				m.prependToAll(moves);

			multiJumps.append(moves);
		}

		return multiJumps;
	}

	/**
	 * Determines whether a jump is possible for a particular piece.
	 * @param b A game board to examine.
	 * @param row The row of the piece.
	 * @param col The column of the piece.
	 * @return Whether or not a jump is possible.
	 */
	protected static boolean jumpIsPossible(byte[][] b, int row, int col) {
		// Define the pieces belonging to the player whose turn it is
		byte myPawn = b[0][0];
		byte myKing = (b[0][0] == P1_PAWN) ? P1_KING : P2_KING;
		byte thePiece = b[row+1][col/2];

		// Make sure it's my piece
		if (thePiece == myPawn || thePiece == myKing) {
			// If it's a king or one of p1's pawns and it's not
			// in one of the back two ranks, check moving upwards
			if ((thePiece == P1_PAWN || thePiece == myKing) && row > 1
					&& (canJump(b, row, col, true, true) ||
						canJump(b, row, col, true, false))) {
				return true;
			}

			// If it's a king or one of p2's pawns and it's not
			// in one of the two front ranks, check moving downwards
			if ((thePiece == P2_PAWN || thePiece == myKing) && row < HEIGHT - 2
					&& (canJump(b, row, col, false, true) ||
						canJump(b, row, col, false, false))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Computes all possible moves (max: 2) of a specified piece in a specified
	 * direction (up or down) and stores the result of these moves, if any, to a
	 * given DSArrayList containing moves. Does NOT check whether the piece
	 * being moved belongs to the player whose turn it is.
	 * 
	 * @param b The board in question.
	 * @param row The row of the piece in question.
	 * @param col The column of the piece in question.
	 * @param movingUp <code>true</code> if and only if the method should
	 * currently consider moves made upwards.
	 * @param children The place to store the moves.
	 */
	private static void addMoves(byte[][] b, int row, int col,
			boolean movingUp, DSArrayList<Move> children) {

		int deltaY = movingUp ? -1 : 1;

		if (row + deltaY < HEIGHT && row + deltaY >= 0) {
			if (col != 0 && b[row+1+deltaY][(col-1)/2]
					== UNOCCUPIED_SQUARE) {
				children.add(new Move(col, row,
						new Point(col-1, row+deltaY)));
			}
			if (col != WIDTH - 1 && b[row + 1 + deltaY][(col+1)/2]
					== UNOCCUPIED_SQUARE) {
				children.add(new Move(col, row,
						new Point(col+1, row+deltaY)));
			}
		}
	}

	/**
	 * Determines whether any move, jump or non-jump,
	 * is possible on the given board.
	 * @param b A game board to examine.
	 * @return Whether any move is possible on the given board.
	 */
	protected static boolean moveIsPossible(byte[][] b) {
		// Define the pieces belonging to the player whose turn it is
		final byte myPawn = b[0][0];
		final byte myKing = (b[0][0] == P1_PAWN) ? P1_KING : P2_KING;

		for (int row = 0; row < HEIGHT; row++) {
			// The count starts at one for odd rows or zero for even
			// rows and increases by 2 in order to skip light squares.
			for (int col = 1-row%2; col < WIDTH; col+=2) {
				if (b[row+1][col/2] == myKing) {
					if (canMove(b, row, col, true) ||
							canMove(b, row, col, false)) {
						return true;
					}
				} else if (b[row+1][col/2] == myPawn) {
					if (canMove(b, row, col, (myPawn == P1_PAWN))) {
						return true;
					}
				}
			}
		}

		return jumpIsPossible(b);
	}

	/**
	 * Determines whether or not a piece can make a move in a specified
	 * direction (up or down).
	 * 
	 * @param b The board in question.
	 * @param row The row of the piece in question.
	 * @param col The column of the piece in question.
	 * @param movingUp <code>true</code> if this method should check for upward
	 * moves, <code>false</code> if it should check for downward moves.
	 * @return Whether or not the piece can make a move in the direction
	 * specified.
	 */
	private static boolean canMove(byte[][] b,
			int row, int col, boolean movingUp) {
		int deltaY = (movingUp) ? -1 : 1;

		if (row + deltaY < HEIGHT && row + deltaY >= 0) {
			if (col != 0 && b[row+1+deltaY][(col-1)/2]
					== UNOCCUPIED_SQUARE) {
				return true;
			}
			if (col != WIDTH - 1 && b[row + 1 + deltaY][(col+1)/2]
					== UNOCCUPIED_SQUARE) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Computes all possible jumps (max: 2) of a specified piece in a specified
	 * direction (up or down) and returns them as a DSArrayList of Moves. Does
	 * NOT check whether the piece being moved belongs to the player whose turn
	 * it is.
	 * 
	 * @param b The board in question.
	 * @param row The row of the piece in question.
	 * @param col The column of the piece in question.
	 * @param movingUp <code>true</code> if the method should currently consider
	 * moves made upwards, <code>false</code> if it should consider downward
	 * moves.
	 * @return A DSArrayList containing Moves.
	 */
	protected static DSArrayList<Move> getJumps(
			byte[][] b, int row, int col, boolean movingUp) {

		int deltaY = movingUp ? -2 : 2;

		DSArrayList<Move> jumps = new DSArrayList<Move>();

		// Define the opponent's pieces
		byte oppPawn = (b[0][0] == P1_PAWN) ? P2_PAWN : P1_PAWN;
		byte oppKing = (b[0][0] == P1_PAWN) ? P2_KING : P1_KING;

		if (row + deltaY < HEIGHT && row + deltaY >= 0) {
			if (col > 1 &&
					b[row+1+deltaY][(col-2)/2] == UNOCCUPIED_SQUARE &&
					(b[row+1+deltaY/2][(col-1)/2] == oppPawn ||
							b[row+1+deltaY/2][(col-1)/2] == oppKing))
				jumps.add(new Move(col, row,
						new Point(col-2, row+deltaY)));
			if (col < WIDTH - 2 &&
					b[row+1+deltaY][(col+2)/2] == UNOCCUPIED_SQUARE &&
					(b[row+1+deltaY/2][(col+1)/2] == oppPawn ||
							b[row+1+deltaY/2][(col+1)/2] == oppKing))
				jumps.add(new Move(col, row,
						new Point(col+2, row+deltaY)));
		}

		return jumps;
	}

	/**
	 * Computes all possible jumps (max: 4) of a specified
	 * piece and returns them as a DSArrayList of Moves.
	 * Does NOT check whether the piece being moved
	 * belongs to the player whose turn it is.
	 * @param b The board in question.
	 * @param row The row of the piece in question.
	 * @param col The column of the piece in question.
	 * @return A DSArrayList containing Moves.
	 */
	protected static DSArrayList<Move> getJumps(
			byte[][] b, int row, int col) {
		switch (b[row+1][col/2]) {
			case P1_PAWN: return getJumps(b, row, col, true);
			case P2_PAWN: return getJumps(b, row, col, false);
			case P1_KING: case P2_KING:
				DSArrayList<Move> jumps = getJumps(b, row, col, true);
				jumps.append(getJumps(b, row, col, false));
				return jumps;
			default: return new DSArrayList<Move>();
		}

	}

	/**
	 * Creates a new copy of the board that
	 * shares no references with the old board.
	 * @param b The board to clone.
	 * @return A "deep" copy of the board.
	 */
	protected byte[][] cloneBoard(byte[][] b) {
		byte[][] newBoard = new byte[HEIGHT+1][WIDTH/2];

		for (int i = 0; i < newBoard.length; i++) {
			for (int ii = 0; ii < newBoard[i].length; ii++) {
				newBoard[i][ii] = b[i][ii];
			}
		}

		return newBoard;
	}

	@Override
	protected String boardHash(byte[][] b) {
		String retVal = "" + b[0][0] + b[0][1];
		for (int i = 1; i < b.length; i++) {
			for (int j = 0; j < b[i].length; j++) {
				retVal += b[i][j];
			}
		}
		return retVal;
	}

	/**
	 * Like {@link #boardHash(Object)}, but ignores <code>b[0][1]</code>.
	 * @param b The board to hash.
	 * @return A stringification of the board.
	 * @see #maxTreeDepth
	 * @see #board
	 */
	protected String boardHash2(byte[][] b) {
		String retVal = "" + b[0][0];
		for (int i = 1; i < b.length; i++) {
			for (int j = 0; j < b[i].length; j++) {
				retVal += b[i][j];
			}
		}
		return retVal;
	}

	@Override
	protected int whoseTurn(byte[][] board) {
		return board[0][0];
	}

	/**
	 * Evaluates the board and returns a value representing
	 * the favorability of a board to the player whose turn
	 * it is on the board. This method should only be called
	 * on boards contained in leaves of a game tree.
	 * @param b The board to evaluate.
	 * @return
	 * <ul>
	 *   <li>{@link #LOSS} if the player has lost.</li>
	 *   <li>if {@link #maxTreeDepth} has been reached,
	 *     a value representing who is winning based on the
	 *     number and type of pieces on the board, where
	 *     greater numbers are more favorable to the player
	 *     whose turn it is.</li>
	 * </ul>
	 * All values except {@link #CONT} are centered around
	 * zero in such a way that they can be negated to show
	 * the favorability of the board to the other player.
	 * @see #evaluateNode(DSGameNode)
	 */
	/*
	 * TODO New board scoring ideas:
	 * 1) A king has a value of 3 if he is along one of the
	 *    margins and 3.5 if he is one square away from a margin.
	 *    This helps the computer to understand trapped kings.
	 * 2) A pawn has a value of 2 if it is along the back rank,
	 *    unless the opponent already has a king.
	 * 3) More advanced pawns are more valuable.
	 * 4) The less pieces are on the board, the more valuable a piece is.
	 *    This could be implemented with a curve?
	 */
	@Override
	protected int evaluateBoard(byte[][] lb) {

		String bh = boardHash2(lb);
		synchronized (boardValues) {
			if (boardValues.containsKey(bh))
				return boardValues.get(bh);
		}

		int rv;

		// If we have not reached the tree size limit but this
		// is a leaf, it must be a loss. (evaluateBoard is only
		// called on leaves.) If we are at the bottom, check for
		// legal moves.
		if (lb[0][1] != 0 || !moveIsPossible(lb)) {
			rv = LOSS;
		} else {
			// Score the board
			rv = 0;

			// At first, get score for P1
			// Start at row 1 to skip metadata 
			for (int row = 0; row < HEIGHT; row++) {
				for (int col = 1-row%2; col < WIDTH; col += 2) {
					int pieceVal;

					switch (lb[row+1][col/2]) {
						case P1_PAWN: case P2_PAWN:
							pieceVal = PAWN_VALUE; break;
						case P1_KING: case P2_KING:
							pieceVal = KING_VALUE;
							if (isTrapped(lb, row, col)) {
								pieceVal -= 2;
							} else if (isAlongEdge(row, col)) {
								pieceVal -= 2;
							} else if (isAlmostAlongEdge(row, col)) {
								pieceVal--;
							}
							break;
						default: continue;
					}

					if (lb[row+1][col/2] == P2_PAWN || lb[row+1][col/2] == P2_KING) {
						pieceVal = -pieceVal;
					}

					rv += pieceVal;
				}
			}

			// Oops, it's player 2's turn
			if (lb[0][0] == P2_PAWN)
				rv = -rv;
		}

		synchronized (boardValues) {
			boardValues.put(bh, rv);
		}

		return rv;
	}

	/**
	 * @param row
	 * @param col
	 * @return
	 */
	private boolean isAlmostAlongEdge(int row, int col) {
		return (row == 1 || col == 1 || row == HEIGHT-2 || col == WIDTH-2);
	}

	/**
	 * @param row
	 * @param col
	 * @return
	 */
	private boolean isAlongEdge(int row, int col) {
		return (row == 0 || col == 0 || row == HEIGHT-1 || col == WIDTH-1) &&
				(row + col != 1) && (HEIGHT-row-1 + WIDTH-col-1 != 1);
	}

	private boolean isTrapped(byte[][] lb, int row, int col) {
		// TODO Write method isTrapped()
		return false;
	}

	/**
	 * @param node A game tree to examine.
	 * @return The value of the node, or the favorability
	 * of its board to the player who does NOT have the
	 * turn on said board.
	 * @see #evaluateBoard(Object)
	 */
	@Override
	protected int evaluateNode(DSGameNode<byte[][]> node) {
		if (node.countChildren() == 0)
			return evaluateBoard(node.returnThing()) *
					((isSuicideCheckers) ? 1 : -1);
		else {
			Stream<DSNode<byte[][]>> stream = node.returnChildren().stream();

			if (maxTreeDepth - getDepth(node.returnThing()) < THREAD_DEPTH-1) {
				stream = stream.parallel();
			}

			int maxVal = stream
					.map(c -> evaluateNode((DSGameNode<byte[][]>) c))
					.max(Integer::compare).get();

			// negate for the other player's benefit
			return -maxVal;
		}
	}

	private byte getDepth(byte[][] board) {
		return board[0][1];
	}

	/**
	 * @param b A game board to examine.
	 * @return Whether or not a jump is possible by the 
	 * player whose turn it is on the board provided.
	 */
	protected static boolean jumpIsPossible(byte[][] b) {

		for (int row = 0; row < HEIGHT; row++) {
			// The count starts at one for odd rows or zero for even rows
			// and increases by 2 in order to skip light squares.
			for (int col = 1-row%2; col < WIDTH; col += 2) {
				if (jumpIsPossible(b, row, col)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Determines whether a piece can jump on a given board
	 * in a specified direction.
	 * @param b the board
	 * @param row the row of the piece's location.
	 * @param col the column of the piece's location.
	 * @param deltaY <code>-1</code> if the piece is moving up,
	 * <code>1</code> if the piece is moving down.
	 * @param deltaX <code>-1</code> if the piece is moving left,
	 * <code>1</code> if the piece is moving right.
	 * @return Whether or not a jump is possible
	 */
	protected static boolean canJump(byte[][] b, int row, int col,
			boolean movingUp, boolean movingLeft) {

		// These really only show half of the real change in x/y
		int deltaY = (movingUp)   ? -1 : 1;
		int deltaX = (movingLeft) ? -1 : 1;

		// Define the opponent's pieces
		byte oppPawn = (b[0][0] == P1_PAWN) ? P2_PAWN : P1_PAWN;
		byte oppKing = (b[0][0] == P1_PAWN) ? P2_KING : P1_KING;

		return col + deltaX*2 >= 0 && col + deltaX*2 < WIDTH &&
				(b[row+1+deltaY][(col+deltaX)/2] == oppKing ||
				b[row+1+deltaY][(col+deltaX)/2] == oppPawn)
				&& b[row+1+deltaY*2][(col+deltaX*2)/2] == UNOCCUPIED_SQUARE;
	}

	/**
	 * Converts an int into a String, such that
	 * <code>0 => "A", 1 => "B", 2 => "C", ... 25 => "Z",
	 * 26 => "AA", 27 => "AB" ...</code>
	 * 
	 * XXX Currently doesn't work for numbers bigger than 25
	 * @param toConvert The <code>int</code> to convert.
	 * @return The <code>String</code> in question.
	 * @see Move#parseCol(String)
	 */
	static String intToAlphaString(int toConvert) {
		return "" + (char)(toConvert + ASCII_A);
	}

}
