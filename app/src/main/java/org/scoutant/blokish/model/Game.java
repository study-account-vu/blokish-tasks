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

import java.util.ArrayList;
import java.util.List;

import org.scoutant.blokish.R;

import android.util.Log;

public class Game {
	public static final String tag = "sc";
	public List<Board> boards = new ArrayList<Board>();
	public int size = 20;

	//	public String[] colors = { "Red", "Green", "Blue", "Orange" };
	public int[] colors = { R.string.Red, R.string.Green, R.string.Blue, R.string.Orange };
	
	// Creates a new game with fresh player boards.
	public Game() {
		reset();
	}
	
	// Restores all player boards to their initial state.
	public void reset() {
		boards.clear();
		for(int k=0; k<4; k++) {
			boards.add(new Board(k));
		}
	}
	
	public List<Move> moves = new ArrayList<Move>();
	
	// Adds a move to the game history.
	public void historize(Move move) {
		moves.add(move);
	}
	
	// Checks whether every player has finished the game.
	public boolean over() {
		return boards.get(0).over && boards.get(1).over && boards.get(2).over && boards.get(3).over; 
	}
	
	// Returns the highest-scoring player, favoring the last player on ties.
	public int winner() {
		int highscore = 0;
		for (int p=0; p<4; p++) highscore = Math.max(highscore, boards.get(p).score);
		for (int p=3; p>=0; p--) {
			if (boards.get(p).score == highscore) return p;
		}
		return -1;
	}
	
	// Replays a sequence of moves on a fresh game.
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
	
	// Adds a played piece to every player's board view.
	protected void add( Piece piece, int i, int j) {
		for(int k=0; k<4; k++) {
			boards.get(k).add(piece, i, j);
		}
	}
	// Checks whether a recorded move is legal.
	public boolean valid( Move move) {
		return valid( move.piece, move.i, move.j);
	}
	// Checks whether a piece placement is legal.
	public boolean valid( Piece piece, int i, int j) {
		return fits(piece, i, j)&& boards.get(piece.color).onseed(piece, i, j);
	}
	
	// Checks whether a piece fits all player board constraints.
	public boolean fits( Piece p, int i, int j) {
		return boards.get(0).fits(0,p, i, j) && boards.get(1).fits(1,p, i, j) && boards.get(2).fits(2,p, i, j) && boards.get(3).fits(3,p, i, j);
	}
	
	// Plays a legal move and records it in the history.
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
	
	// Returns a serialized summary of the move history.
	public String toString() {
		String msg = "# moves : " + moves.size();
		for (Move move: moves) {
			msg += "\n" + Move.serialize(move);
		}
		return msg;
	}

	// Parses serialized moves into a move list.
	public List<Move> deserialize(String msg) {
		List<Move> list = new ArrayList<Move>();
		return list;
	}
	
	
	int[][] ab = new int [20][20];
	// Counts enemy seeds remaining after a hypothetical placement.
	private int scoreEnemySeedsIfAdding(Board board, Piece piece, int i, int j) {
		// how many of the board's seeds happen to be under piece?
		int result=0;
		for (int b=0; b<20; b++) for (int a=0; a<20; a++) ab[a][b] = 0;
		for(Square s : board.seeds()) {
			try { ab[s.i][s.j] = 1; } catch (Exception e) {}
		}
		for(Square s : piece.squares()) {
			try { ab[i+s.i][j+s.j] = 0; } catch (Exception e) {}
		}
		for (int b=0; b<20; b++) for (int a=0; a<20; a++) if (ab[a][b]==1) result++;
		//		Log.d(tag, "scoreEnemySeedsIfAdding : " + result + ". color : " + board.color);
		return result;
	}
	
	// Scores the enemy seeds remaining after a hypothetical placement.
	public int scoreEnemySeedsIfAdding(int color, Piece piece, int i, int j) {
		int result =0;
		//		for (int c=0; c<4; c++) {
		//			if (c!=color) {
		//				result += scoreEnemySeedsIfAdding( boards.get(c), piece, i, j );
		//			}
		//		}
		// try consider only Red as enemy, for machine to compete with human!
		result += scoreEnemySeedsIfAdding( boards.get(0), piece, i, j );
		return result;
	}
	
}
