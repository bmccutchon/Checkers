/*
 * "public" means all other classes
 * are free to inherit this class.
 */

package framework;

/**
 * @author UD DS1, Spring 2014
 * @version 0.1
 * @see Game
 */
public abstract class TwoPlayer<B> extends Game<B> {
	
	/**
	 * Constant representing a win for player 1.
	 * @see TwoPlayer#PLAYER2WIN
	 * @see TwoPlayer#CONTINUE
	 * @see TwoPlayer#DRAW
	 */
	public static final int PLAYER1WIN = 1;
	
	/**
	 * Constant representing a win for player 2.
	 * @see TwoPlayer#PLAYER1WIN
	 * @see TwoPlayer#CONTINUE
	 * @see TwoPlayer#DRAW
	 */
	public static final int PLAYER2WIN = 2;
	
	/**
	 * Constant representing a drawn game.
	 * @see TwoPlayer#PLAYER1WIN
	 * @see TwoPlayer#PLAYER2WIN
	 * @see TwoPlayer#CONTINUE
	 */
	public static final int DRAW  = -1;
	
	/**
	 * Constant representing a "continue" state.
	 * @see TwoPlayer#PLAYER1WIN
	 * @see TwoPlayer#PLAYER2WIN
	 * @see TwoPlayer#DRAW
	 */
	public static final int CONTINUE  = -2;

	/**
	 * This field says whose turn it is.
	 */
	int whoseTurn;

	// This is how all 2-player games are played:
	// +------.
	// |      V
	// | draw board
	// | Player t goes
	// | end check
	// | make t be other player
	// L______/
	/**
	 * Call this method to start the game.
	 * This method can be overriden by subclasses for the
	 * purpose of resetting fields between games so that
	 * this method can be called more than once per instance.
	 * However, implementors should usually call this method
	 * via <code>super.play();</code>.
	 * @see TwoPlayer#computerPlaySelf()
	 */
	public void play() {
		getHumanOrComputer();
		
		whoseTurn = 1;
		int e = CONTINUE; // endCheck value
		while (e == CONTINUE) {
			drawBoard();
			move(whoseTurn);
			e = endCheck();
			whoseTurn = 3 - whoseTurn; // slick way to change turns
		}
		
		drawBoard();
		endstate = e;
		if(endstate == PLAYER1WIN)
			System.out.println("Player 1 is the winner!");
		if(endstate == PLAYER2WIN)
			System.out.println("Player 2 is the winner!");
	}

	/**
	 * Makes the computer play itself.
	 * @return An int representing who won,
	 *   as described by the referenced constants.
	 * @see TwoPlayer#PLAYER1WIN
	 * @see TwoPlayer#PLAYER2WIN
	 * @see TwoPlayer#CONTINUE
	 * @see TwoPlayer#DRAW
	 */
	public int computerPlaySelf() {
		isHuman[1] = false;
		isHuman[2] = false;

		whoseTurn = 1;
		int e = CONTINUE; // endCheck value
		while (e == CONTINUE) {
			move(whoseTurn);
			e = endCheck();
			whoseTurn = 3 - whoseTurn; // slick way to change turns
		}

		return e;
	}

	/**
	 * Used to determine which players are human.
	 * Initializes indices 1 and 2 of the
	 * {@link Game#isHuman isHuman} array.
	 * @see Game#computerMove(int)
	 * @see Game#humanMove(int)
	 */
	protected void getHumanOrComputer() {
		isHuman[1] = true;
		isHuman[2] = false;
	}
	
}

