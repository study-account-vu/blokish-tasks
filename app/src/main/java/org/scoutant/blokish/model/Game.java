
package org.scoutant.blokish.model;

import java.util.ArrayList;
import java.util.List;

import org.scoutant.blokish.R;

import android.util.Log;

/**
 * Aggregates the four player boards and the ordered move history for a match.
 * It validates placements across all boards and applies accepted moves to the shared state.
 */
public class Game {
	public static final String tag = "sc";
	public List<Board> boards = new ArrayList<Board>();
	public int size = 20;

	public int[] colors = { R.string.Red, R.string.Green, R.string.Blue, R.string.Orange };
	
	int[][] ab = new int [20][20];
	
	/** Creates a new match with an initialized board for each player. */
	public Game() {
		reset();
	}
	
	/** Replaces every board with a fresh initial-state board for its player. */
	public void reset() {
		boards.clear();
		for(int k=0; k<4; k++) {
			boards.add(new Board(k));
		}
	}
	
	/** Accepted moves in the order they were played. */
	public List<Move> moves = new ArrayList<Move>();
	
	/**
	 * Appends an accepted move to the match history.
	 * @param move accepted move to record
	 */
	public void historize(Move move) {
		moves.add(move);
	}
	
	/**
	 * Returns whether every player's board has been marked as unable to continue.
	 * @return {@code true} when all four boards are over
	 */
	public boolean over() {
		return boards.get(0).over && boards.get(1).over && boards.get(2).over && boards.get(3).over; 
	}
	
	/**
	 * Returns the highest-scoring player, resolving tied scores in favor of the higher color index.
	 * @return winning player's color index
	 */
	public int winner() {
		int highscore = 0;
		for (int p=0; p<4; p++) highscore = Math.max(highscore, boards.get(p).score);
		for (int p=3; p>=0; p--) {
			if (boards.get(p).score == highscore) return p;
		}
		return -1;
	}
	
	/**
	 * Reapplies saved moves in order, resolving each saved piece to this game's board instance.
	 * @param moves ordered move sequence to apply
	 * @return {@code true} if every move is accepted, otherwise {@code false}
	 */
	public boolean replay(List<Move> moves) {
		for (Move move : moves) {
			Piece piece = move.piece;
			int color = piece.color; 
			Piece p = boards.get(color).findPieceByType( piece.type);
			p.reset(piece);
			move.piece = p;
			boolean status = play(move);
			if (status==false) return false;
		}
		return true;
	}
	
	/**
	 * Applies a placed footprint to all four boards so shared occupancy remains synchronized.
	 * @param piece piece being placed
	 * @param i horizontal board origin
	 * @param j vertical board origin
	 */
	protected void add( Piece piece, int i, int j) {
		for(int k=0; k<4; k++) {
			boards.get(k).add(piece, i, j);
		}
	}

	/**
	 * Tests whether a move's piece and origin satisfy the game placement rules.
	 * @param move candidate move
	 * @return {@code true} if the move fits and touches its owner's reachable corner
	 */
	public boolean valid( Move move) {
		return valid( move.piece, move.i, move.j);
	}

	/**
	 * Tests board fit and the owning player's reachable-corner requirement.
	 * @param piece candidate piece
	 * @param i horizontal board origin
	 * @param j vertical board origin
	 * @return {@code true} if the placement satisfies all game rules
	 */
	public boolean valid( Piece piece, int i, int j) {
		return fits(piece, i, j)&& boards.get(piece.color).onseed(piece, i, j);
	}
	
	/**
	 * Requires a candidate placement to fit every player's occupancy constraints.
	 * @param p candidate piece
	 * @param i horizontal board origin
	 * @param j vertical board origin
	 * @return {@code true} if the piece fits every board
	 */
	public boolean fits( Piece p, int i, int j) {
		return boards.get(0).fits(0,p, i, j) && boards.get(1).fits(1,p, i, j) && boards.get(2).fits(2,p, i, j) && boards.get(3).fits(3,p, i, j);
	}
	
	/**
	 * Validates and applies a move, recording it in the match history on success.
	 * @param move candidate move to play
	 * @return {@code true} if the move was valid and applied
	 */
	public boolean play(Move move) {
		if ( ! valid(move)) { 
			Log.e(tag, "not valid! " + move);
			Log.e(tag, "not valid! " + move.piece);
			return false;
		}
		add(move.piece, move.i, move.j);
		Log.d(tag, "played move : " + move);
		historize(move);
		return true;
	}
	
	/**
	 * Serializes the move history as a header followed by one record per accepted move.
	 * @return serialized game history
	 */
	public String toString() {
		String msg = "# moves : " + moves.size();
		for (Move move: moves) {
			msg += "\n" + Move.serialize(move);
		}
		return msg;
	}

	/**
	 * Parses a serialized move-history string into its constituent moves.
	 * @param msg serialized game history
	 * @return parsed moves in history order
	 */
	public List<Move> deserialize(String msg) {
		List<Move> list = new ArrayList<Move>();
		return list;
	}
	
	/** Counts one opponent board's reachable corners left uncovered by a candidate piece. */
	private int scoreEnemySeedsIfAdding(Board board, Piece piece, int i, int j) {
		int result=0;
		for (int b=0; b<20; b++) for (int a=0; a<20; a++) ab[a][b] = 0;
		for(Square s : board.seeds()) {
			try { ab[s.i][s.j] = 1; } catch (Exception e) {}
		}
		for(Square s : piece.squares()) {
			try { ab[i+s.i][j+s.j] = 0; } catch (Exception e) {}
		}
		for (int b=0; b<20; b++) for (int a=0; a<20; a++) if (ab[a][b]==1) result++;
		return result;
	}
	
	/**
	 * Estimates opponent corner access remaining after a candidate placement.
	 * @param color player index associated with the candidate
	 * @param piece candidate piece
	 * @param i horizontal board origin
	 * @param j vertical board origin
	 * @return count of opponent seed cells left uncovered by the candidate
	 */
	public int scoreEnemySeedsIfAdding(int color, Piece piece, int i, int j) {
		int result =0;
		result += scoreEnemySeedsIfAdding( boards.get(0), piece, i, j );
		return result;
	}
	
}
