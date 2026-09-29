
package org.scoutant.blokish.model;

import java.util.ArrayList;
import java.util.List;

import org.scoutant.blokish.R;

import android.util.Log;

public class Game {
	public static final String tag = "sc";
	public List<Board> boards = new ArrayList<Board>();
	public int size = 20;

	public int[] colors = { R.string.Red, R.string.Green, R.string.Blue, R.string.Orange };
	
	public Game() {
		reset();
	}
	
	public void reset() {
		boards.clear();
		for(int k=0; k<4; k++) {
			boards.add(new Board(k));
		}
	}
	
	public List<Move> moves = new ArrayList<Move>();
	
	public void historize(Move move) {
		moves.add(move);
	}
	
	public boolean over() {
		return boards.get(0).over && boards.get(1).over && boards.get(2).over && boards.get(3).over; 
	}
	
	public int winner() {
		int highscore = 0;
		for (int p=0; p<4; p++) highscore = Math.max(highscore, boards.get(p).score);
		for (int p=3; p>=0; p--) {
			if (boards.get(p).score == highscore) return p;
		}
		return -1;
	}
	
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
	
	protected void add( Piece piece, int i, int j) {
		for(int k=0; k<4; k++) {
			boards.get(k).add(piece, i, j);
		}
	}
	public boolean valid( Move move) {
		return valid( move.piece, move.i, move.j);
	}
	public boolean valid( Piece piece, int i, int j) {
		return fits(piece, i, j)&& boards.get(piece.color).onseed(piece, i, j);
	}
	
	public boolean fits( Piece p, int i, int j) {
		return boards.get(0).fits(0,p, i, j) && boards.get(1).fits(1,p, i, j) && boards.get(2).fits(2,p, i, j) && boards.get(3).fits(3,p, i, j);
	}
	
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
	
	public String toString() {
		String msg = "# moves : " + moves.size();
		for (Move move: moves) {
			msg += "\n" + Move.serialize(move);
		}
		return msg;
	}

	public List<Move> deserialize(String msg) {
		List<Move> list = new ArrayList<Move>();
		return list;
	}
	
	
	int[][] ab = new int [20][20];
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
	
	public int scoreEnemySeedsIfAdding(int color, Piece piece, int i, int j) {
		int result =0;
		result += scoreEnemySeedsIfAdding( boards.get(0), piece, i, j );
		return result;
	}
	
}
