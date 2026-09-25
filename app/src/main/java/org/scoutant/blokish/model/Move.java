/*
* Copyright (C) 2011- stephane coutant
*
* This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
* See the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>
*/

package org.scoutant.blokish.model;

public class Move implements Comparable<Move> {
	
	public Piece piece;
	public int i;
	public int j;
	public int score;
	public Piece ghost;
	
	// Creates a move and captures the piece's current shape.
	public Move (Piece piece, int i, int j) {
		this.piece = piece;
		this.i = i;
		this.j = j;
		this.ghost = piece.clone();
	}
	
	// Creates a scored move and captures the piece's current shape.
	public Move (Piece piece, int i, int j, int score) {
		this(piece, i, j);
		this.score = score;
	}
	
	// Returns a compact description of this move.
	public String toString() {
		return "" + piece.color + ":" +piece.type + ":"+i+":"+j+":" + score;
	}

	// Orders moves by their score.
	public int compareTo(Move that) {
		return this.score - that.score;
	}

	// Serializes a move for storage or replay.
	public static String serialize(Move move) {
		return String.format( "%s:%s:%s", move.i, move.j, Piece.serialize( move.piece));
	}
}