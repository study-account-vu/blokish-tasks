
package org.scoutant.blokish.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.util.Log;

/**
 * Searches legal placements for each player and ranks candidates using board expansion,
 * reachable corners, opponent access, and follow-up placement opportunities.
 */
public class AI  {

	public static final String tag = "sc";
	private static final int SIZE_WEIGHT = 5;
	private static final int CENTER_WEIGHT = 1;
	private static final int SEEDS_WEIGHT = 3;
	private static final int ENEMY_SEEDS_WEIGHT = 1;
	private static final int CHAINING_WEIGHT = 3;
	public Game game;
	private Random random = new Random();
	
	private int[] maxMoves = { 40, 100, 250, 10000 };

	public int adaptedLevel = 3;

	private int[][] ij = new int [20][20];
	
	/**
	 * Creates a searcher bound to a game model.
	 *
	 * @param game game whose boards and pieces are evaluated
	 */
	public AI(Game game) {
		this.game = game;
	}

	/**
	 * Tests whether a player has any legal placement among its remaining pieces.
	 *
	 * @param color player index whose available moves are examined
	 * @return {@code true} if at least one legal placement exists
	 */
	public boolean hasMove(int color) {
		Board board = game.boards.get(color);
		for (Square seed : board.seeds()) {
			for (int p=0; p<board.pieces.size(); p++) {
				Piece piece = board.pieces.get(p);
				for( int f=0; f<piece.flips; f++, piece.flip()) {
					for (int r=0; r<piece.rotations; r++, piece.rotate(1)) {
						for (Square s : piece.squares()) {
							int i = seed.i - s.i;
							int j = seed.j - s.j;
							if ( !board.outside(s, i, j) && game.fits(piece, i, j)) {
								Log.d(tag, "possible move : " + new Move(piece, i, j));
								game.boards.get(color).over = false;
								return true;
							}
						}
					}
				}
			}
		}
		game.boards.get(color).over = true;
		return false;
	}
	
	/**
	 * Selects a candidate move at the requested bounded search level.
	 *
	 * @param color player index for which to choose a move
	 * @param level search depth/effort tier, bounded by {@link #adaptedLevel}
	 * @return selected move, or {@code null} when no candidate is available
	 */
	public Move think(int color, int level) {
		if (game.boards.get(color).pieces.isEmpty()) {
			Log.d(tag, "no more pieces for player : " + color);
			game.boards.get(color).over = true;
			return null;
		}
		Log.d(tag, "--------------------------------------------------------------------------------");
		level = Math.min(level, adaptedLevel);
		if (level>1 && color!=1 ) level--;
		Log.d(tag, "thinking for player : " + color + ", upto # moves : " + maxMoves[level]);
		List<Move> moves = thinkUpToNMoves(color, level);
		Log.d(tag, "# moves : " + moves.size());
		if (moves.size()==0) {
			game.boards.get(color).over = true;
			return null;
		}
		Collections.sort(moves);
		Collections.reverse(moves);
		Move move = moves.get( 0);
		Log.d(tag, "best move actually is : " + move);
		if (moves.size()>20) {
			for (int k=moves.size()-1; k>=2; k--) {
				if (moves.get(k).piece.count<=2) moves.remove(k);
			}
			if (moves.size()> 10) {
				move = moves.get( random.nextInt(3));
			} else {
				Log.d(tag, "keeping best move!");
			}
		}
		move.piece.reset(move.ghost);
		return move;
	}
	
	/**
	 * Enumerates and scores legal placements, stopping at the configured candidate limit.
	 * @param color player index whose candidates are searched
	 * @param level search-effort tier selecting the candidate limit
	 * @return legal candidate moves with heuristic scores
	 */
	protected List<Move> thinkUpToNMoves(int color, int level) {
		List<Move> moves = new ArrayList<Move>();
		Board board = game.boards.get(color);
		int nbSeeds = board.seeds().size();
		Log.d(tag, "# of seeds : " + nbSeeds);
		if (nbSeeds==0) return moves;
		long startedAt = new Date().getTime();
		List<Square> seeds = board.seeds();
		Collections.sort(seeds);
		if (board.pieces.size()> board.nbPieces - 4) {
			seeds = seeds.subList(0, Math.min(seeds.size(), 2));
		}
		for (Square seed : seeds) {
			int movesAgainstSeed=0;
			Log.d(tag, "---- seed : " + seed);
			int maxMovesAgainstSeed = maxMoves[level] / nbSeeds;
			for (int p=0; p<board.pieces.size() && movesAgainstSeed<maxMovesAgainstSeed; p++) {
				Piece piece = board.pieces.get(p);
				for (int r=0; r<piece.rotations; r++, piece.rotate(1)) {
					for( int f=0; f<piece.flips; f++, piece.flip()) {
						for (Square s : piece.squares()) {
							int i = seed.i - s.i;
							int j = seed.j - s.j;
							if ( !board.outside(s, i, j) && game.fits(piece, i, j)) {
								Move move = new Move(piece, i, j);
								if (!game.valid(move)) {
									Log.e(tag, "Inconsistant ! "+move);
								}
								int score = SIZE_WEIGHT * piece.count;
								if (board.pieces.size()> board.nbPieces-5) {
									int io = game.size/2 - i;
									int jo = game.size/2 - j;
									score -= CENTER_WEIGHT * (io*io + jo*jo);
								}
								int seedsIfAdding = board.scoreSeedsIfAdding(piece, i, j);
								score += SEEDS_WEIGHT * seedsIfAdding ;
								int enemyscore = game.scoreEnemySeedsIfAdding(board.color, piece, i, j);
								score -= ENEMY_SEEDS_WEIGHT * enemyscore;
								if (board.pieces.size() < 9) {
									score += CHAINING_WEIGHT*chainingScore(color, move);
								}
								move.score = score;
								if (board.pieces.size()<= board.nbPieces-4 || piece.count>=5) {
									moves.add(move);
								}
								
								movesAgainstSeed++;
								if (moves.size()>= maxMoves[level]) {
									autoAdaptLevel(startedAt);
									return moves;
								}
							}
						}
					}
				}
			}
		}
		autoAdaptLevel(startedAt);
		return moves;
	}
	
	/**
	 * Scores how many cells of a large remaining piece could be played immediately afterward.
	 * @param color player index whose follow-up options are evaluated
	 * @param move hypothetical first move
	 * @return occupied-cell count of the best immediately available follow-up piece
	 */
	protected int chainingScore(int color, Move move) {
		Board board = game.boards.get(color);
		Piece played = move.piece;
		List<Piece> pieces = new ArrayList<Piece>();
		for (Piece piece : board.pieces) {
			if (!piece.equals(played)) {
				pieces.add( piece.clone());
			}
		}
		
		for (int j=0;j<20;j++) {
			for (int i=0;i<20;i++) {
				ij[i][j] = board.ij[i][j];
			}
		}
		for(Square s : played.squares(color)) {
			int I = move.i+s.i;
			int J = move.j+s.j;
			if (I>=0 && I<20 && J>=0 && J<20) ij[I][J] = s.value;
		}

		int score = 0;
		Move second = null;
		Log.d(tag, "considering # of pieces : " + pieces.size());
		for (Square seed : played.seeds()) {
			for (Piece piece : pieces) {
				for (int r=0; r<piece.rotations; r++, piece.rotate(1)) {
					for( int f=0; f<piece.flips; f++, piece.flip()) {
						for (Square s : piece.squares()) {
							int i = move.i + seed.i - s.i;
							int j = move.j + seed.j - s.j;
							boolean overlaps = overlaps(color, piece, i, j);
							boolean outside = board.outside(s, i, j); 
							boolean fits =  game.fits(piece, i, j);
							if ( !overlaps && !outside && fits ){
								if (piece.count > score) {
									second = new Move(piece, i, j);
									score = piece.count;
								}
							}
						}
					}
				}
			}
		}
		if (score>1) Log.d(tag, "may CHAIN with : " + second);
		return score;
	}
	
	/**
	 * Checks the candidate piece against the temporary board used by chaining search.
	 *
	 * @param color player index associated with the candidate
	 * @param piece candidate shape in its current orientation
	 * @param i candidate horizontal board origin
	 * @param j candidate vertical board origin
	 * @return {@code true} if any occupied candidate cell intersects a recorded cell
	 */
	public boolean overlaps( int color, Piece piece, int i, int j) {
		for(Square s : piece.squares()) {
			int I = i+s.i;
			int J = j+s.j;
			if ( I>=0&&I<20&&J>=0&&J<20 &&  ij[I][J] > 1 ) return true;
		}
		return false;
	}

	
	/** Lowers the allowed search tier when the most recent candidate search exceeds its time budget. */
	private void autoAdaptLevel(long startedAt) {
		long duration = new Date().getTime()- startedAt;
		Log.d(tag, "lasted : " + duration );
		if (duration > 2500 && adaptedLevel>0) {
			Log.i(tag, "Decreasing AI level! * * * * * * * * * * * * * * * * * * * *");
			adaptedLevel --;
		}
	}
}
