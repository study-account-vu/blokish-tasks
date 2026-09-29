
package org.scoutant.blokish.model;

public class Move implements Comparable<Move> {
	
	public Piece piece;
	public int i;
	public int j;
	public int score;
	public Piece ghost;
	
	public Move (Piece piece, int i, int j) {
		this.piece = piece;
		this.i = i;
		this.j = j;
		this.ghost = piece.clone();
	}
	
	public Move (Piece piece, int i, int j, int score) {
		this(piece, i, j);
		this.score = score;
	}
	
	public String toString() {
		return "" + piece.color + ":" +piece.type + ":"+i+":"+j+":" + score;
	}

	public int compareTo(Move that) {
		return this.score - that.score;
	}

	public static String serialize(Move move) {
		return String.format( "%s:%s:%s", move.i, move.j, Piece.serialize( move.piece));
	}
}