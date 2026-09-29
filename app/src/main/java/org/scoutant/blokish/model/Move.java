
package org.scoutant.blokish.model;

/**
 * Represents placing a particular piece at a board origin, with an optional search score.
 * A cloned piece snapshot preserves the orientation used when the move was created.
 */
public class Move implements Comparable<Move> {
	
	public Piece piece;
	public int i;
	public int j;
	public int score;
	public Piece ghost;
	
	/**
	 * Creates a placement candidate and snapshots the piece's current shape.
	 *
	 * @param piece piece and orientation to place
	 * @param i horizontal board origin in cells
	 * @param j vertical board origin in cells
	 */
	public Move (Piece piece, int i, int j) {
		this.piece = piece;
		this.i = i;
		this.j = j;
		this.ghost = piece.clone();
	}
	
	/**
	 * Creates a scored placement candidate.
	 *
	 * @param piece piece and orientation to place
	 * @param i horizontal board origin in cells
	 * @param j vertical board origin in cells
	 * @param score heuristic candidate score
	 */
	public Move (Piece piece, int i, int j, int score) {
		this(piece, i, j);
		this.score = score;
	}
	
	/**
	 * Returns a compact diagnostic representation of the move and its score.
	 * @return textual description of player, piece, origin, and score
	 */
	public String toString() {
		return "" + piece.color + ":" +piece.type + ":"+i+":"+j+":" + score;
	}

	/**
	 * Compares candidates by heuristic score.
	 * @param that candidate to compare with
	 * @return negative, zero, or positive according to the score difference
	 */
	public int compareTo(Move that) {
		return this.score - that.score;
	}

	/**
	 * Encodes a move origin and piece shape for game-history persistence.
	 * @param move move to encode
	 * @return serialized position and piece record
	 */
	public static String serialize(Move move) {
		return String.format( "%s:%s:%s", move.i, move.j, Piece.serialize( move.piece));
	}
}