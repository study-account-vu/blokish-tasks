
package org.scoutant.blokish.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores one player's 20-by-20 occupancy grid, remaining pieces, score, and reachable corners.
 * A game owns four boards; placements update every board so each player's overlap rules can be checked.
 */
public class Board {
	public static final String tag = "sc";
	public int color;
	public int size = 20;
	int[][] ij = new int [20][20];
	public List<Piece> pieces = new ArrayList<Piece>();
	public int nbPieces;
	public int score;
	public boolean over=false;

	/**
	 * Creates a board with the starting corner and standard set of pieces for one player.
	 *
	 * @param color player index, from zero through three
	 */
	public Board(int color) {
		this.color = color;
		if (color==0) ij[0][0]=1;
		if (color==1) ij[size-1][0]=1;
		if (color==2) ij[size-1][size-1]=1;
		if (color==3) ij[0][size-1]=1;

		pieces.add( new Piece(color, 3, "X5", 1, 1).add(0,0).add(-1,0).add(0,-1).add(1,0).add(0,1));
		pieces.add( new Piece(color, 3, "W5", 4, 1).add(0,0).add(0,-1).add(1,-1).add(-1,0).add(-1,1));
		pieces.add( new Piece(color, 3, "F5", 4, 2).add(0,0).add(0,-1).add(1,-1).add(-1,0).add(0,1));
		pieces.add( new Piece(color, 3, "T5", 4, 1).add(-1,-1).add(0,-1).add(1,-1).add(0,0).add(0,1));
		pieces.add( new Piece(color, 3, "Z5", 2, 2).add(-1,-1).add(0,-1).add(0,0).add(0,1).add(1,1));

		pieces.add( new Piece(color, 4, "Y5", 4, 2).add(0,-1).add(0,0).add(0,1).add(0,2).add(1,0));
		pieces.add( new Piece(color, 4, "N5", 4, 2).add(0,-1).add(0,0).add(1,0).add(1,1).add(1,2));
		
		pieces.add( new Piece(color, 3, "U5", 4, 1).add(1,-1).add(0,-1).add(0,0).add(0,1).add(1,1));
		pieces.add( new Piece(color, 3, "V5", 4, 1).add(1,-1).add(0,-1).add(-1,-1).add(-1,0).add(-1,1));
		pieces.add( new Piece(color, 3, "P5", 4, 2).add(0,-1).add(0,0).add(0,1).add(1,-1).add(1,0));

		pieces.add( new Piece(color, 4, "L5", 4, 2).add(1,-1).add(1,0).add(1,1).add(1,2).add(0,2));

		pieces.add( new Piece(color, 5, "I5", 2, 1).add(0,-2).add(0,-1).add(0,0).add(0,1).add(0,2));
		
		pieces.add( new Piece(color, 2, "O4", 1, 1).add(0,0).add(1,0).add(0,1).add(1,1) );
		
		pieces.add( new Piece(color, 3, "S4", 2, 2).add(-1,-1).add(-1,0).add(0,0).add(0,1) );
		pieces.add( new Piece(color, 3, "T4", 4, 1).add(-1,-1).add(-1,0).add(-1,1).add(0,0) );
		pieces.add( new Piece(color, 3, "L4", 4, 2).add(0,-1).add(0,0).add(0,1).add(1,1));

		pieces.add( new Piece(color, 4, "I4", 2, 1).add(0,-1).add(0,0).add(0,1).add(0,2));
		
		pieces.add( new Piece(color, 3, "I3", 2, 1).add(0,-1).add(0,0).add(0,1));

		pieces.add( new Piece(color, 2, "L3", 4, 1).add(0,0).add(0,1).add(1,1));
		pieces.add( new Piece(color, 2, "I2", 2, 1).add(0,0).add(0,1));

		pieces.add( new Piece(color, 1, "O1", 1, 1).add(0,0));
		nbPieces = pieces.size();
	}
	
	/**
	 * Finds a remaining piece by its stable shape identifier.
	 *
	 * @param type piece identifier such as {@code "T5"}
	 * @return matching remaining piece, or {@code null} if it is not in the player's tray
	 */
	public Piece findPieceByType(String type) {
		for (Piece piece:pieces) {
			if (piece.type.equals(type)) return piece;
		}
		return null;
	}
	
	/**
	 * Adds a piece footprint to the grid and, for its owner, updates score and reachable corners.
	 * @param piece shape being placed
	 * @param i horizontal board origin
	 * @param j vertical board origin
	 */
	public void add( Piece piece, int i, int j) {
		for(Square s : piece.squares(this.color)) {
			int I = i+s.i;
			int J = j+s.j;
			if (I>=0 && I<size && J>=0 && J<size) ij[I][J] = s.value;
		}
		
		if (piece.color == this.color) {
			pieces.remove( piece);
			score += piece.count;
			for(Square seed : piece.seeds()) {
				try { if (ij[i+seed.i][j+seed.j] ==0 ) ij[i+seed.i][j+seed.j] = 1; } catch (Exception e) {}   
			}
		}
	}

	/** Reusable scratch grid for estimating the reachable-corner count after a placement. */
	int[][] ab = new int [20][20];
	/**
	 * Counts reachable corners that would remain after hypothetically placing a piece.
	 * @param piece candidate shape
	 * @param i horizontal board origin
	 * @param j vertical board origin
	 * @return number of reachable-corner cells after the hypothetical placement
	 */
	public int scoreSeedsIfAdding(Piece piece, int i, int j) {
		int result=0;
		for (int b=0; b<20; b++) for (int a=0; a<20; a++) ab[a][b] = ij[a][b];
		for(Square s : piece.squares(this.color)) {
			try { ab[i+s.i][j+s.j] = s.value; } catch (Exception e) {}
		}
		for(Square seed : piece.seeds()) {
			try { if (ab[i+seed.i][j+seed.j] ==0 ) ab[i+seed.i][j+seed.j] = 1; } catch (Exception e) {}   
		}
		for (int b=0; b<20; b++) for (int a=0; a<20; a++) if (ab[a][b]==1) result++;
		return result;
	}
	
	
	/**
	 * Tests whether a piece cell translated to an origin lies beyond this board's bounds.
	 *
	 * @param s cell within the piece's local coordinate system
	 * @param i candidate horizontal board origin
	 * @param j candidate vertical board origin
	 * @return {@code true} if the translated cell is outside the board
	 */
	public boolean outside(Square s, int i, int j) {
		return ( s.i+i<0 || s.i+i>=size || s.j+j<0 || s.j+j>=size );
	}
	
	/**
	 * Checks board bounds and color-specific contact restrictions for a placement.
	 *
	 * @param color player whose contact rules are being evaluated
	 * @param piece piece shape to test
	 * @param i candidate horizontal board origin
	 * @param j candidate vertical board origin
	 * @return {@code true} when a cell is out of bounds or conflicts with the board
	 */
	public boolean overlaps( int color, Piece piece, int i, int j) {
		for(Square s : piece.squares()) {
			if (outside(s, i, j)) return true;
			if (ij[i+s.i][j+s.j] > (piece.color == color ? 1: 2) ) return true;
		}
		return false;
	}
	
	/**
	 * Determines whether the piece satisfies this board's bounds and contact rules.
	 *
	 * @param color player whose placement is being tested
	 * @param piece candidate piece
	 * @param i candidate horizontal board origin
	 * @param j candidate vertical board origin
	 * @return {@code true} if the piece does not overlap or violate board limits
	 */
	public boolean fits( int color, Piece piece, int i, int j) {
		if (i<-1 || i> size || j<-1 || j>size) return false; 
		return ! overlaps( color, piece, i, j);
	}
	
	/**
	 * Checks whether any occupied cell of a piece covers one of this board's reachable corners.
	 *
	 * @param piece candidate piece
	 * @param i candidate horizontal board origin
	 * @param j candidate vertical board origin
	 * @return {@code true} if the candidate touches a reachable corner cell
	 */
	public boolean onseed( Piece piece, int i, int j) {
		for(Square s : piece.squares()) {
			if ( !outside(s, i, j) && ij[i+s.i][j+s.j]==1) return true;
		}
		return false;
	}
	
	/**
	 * Returns a text rendering of all board rows.
	 * @return textual board grid
	 */
	public String toString() {
		return toString(size);
	}
	
	/**
	 * Renders the first requested number of rows as a diagnostic grid.
	 * @param jmax number of rows to include
	 * @return textual rendering of the requested board rows
	 */
	public String toString(int jmax) {
		String str = "";
		for (int j=0; j<jmax; j++) {
			for (int i=0; i<size; i++) {
				str += ij[i][j] + (i==size-1 ? "\n" : " | ");
			}
		}
		return str;
	}

	/**
	 * Returns the board coordinates currently marked as reachable corners.
	 * @return list of reachable-corner coordinates
	 */
	public List<Square> seeds() {
		List<Square> list = new ArrayList<Square>();
		for (int j=0; j<size; j++) {
			for (int i=0; i<size; i++) {
				if ( ij[i][j]==1 ) list.add(new Square(i, j)); 	
			}
		}
		return list;
	}
}
