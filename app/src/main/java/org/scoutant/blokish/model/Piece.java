
package org.scoutant.blokish.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a player's polyomino shape and its current orientation.
 * Local cells are stored in a bounded matrix, with coordinates centered around the shape
 * so rotations, flips, board placement, and reachable-corner calculation share one model.
 */
public class Piece {
	public String type;
	public int size;
	public int color = 0;
	private int[][] a;
	private int[][] b;
	private boolean actual = true;
	public int rotations = 4;
	public int flips = 2;
	public int count=0;
	private int h;
	private int r;
	private int f;
	private boolean odd;

	public static final String tag = "sc";

	/**
	 * Creates a shape assigned to a player.
	 *
	 * @param color owning player index
	 * @param size side length of the local shape matrix
	 * @param type stable shape identifier
	 * @param rotations number of distinct quarter-turn orientations
	 * @param flips number of distinct mirrored states
	 */
	public Piece( int color, int size, String type, int rotations, int flips ) {
		this(size, type, rotations, flips);
		this.color = color;
	}

	/**
	 * Creates an unassigned shape definition and initializes its empty local matrix.
	 *
	 * @param size side length of the local shape matrix
	 * @param type stable shape identifier
	 * @param rotations number of distinct quarter-turn orientations
	 * @param flips number of distinct mirrored states
	 */
	public Piece( int size, String type, int rotations, int flips ) {
		this.size = size;
		this.type = type;
		this.rotations = rotations;
		this.flips = flips;
		h = (size+1)/2 -1;
		odd = (size % 2) == 1; 
		reset();
	}
	/** Clears all occupied cells and returns the shape to its original orientation. */
	public void reset() {
		r=0;
		f=0;
		a = new int[size][size];
		b = new int[size][size];
		actual = true;
		count=0;
	}
	
	/**
	 * Restores this piece to the shape represented by a saved snapshot.
	 * @param ghost saved shape snapshot to copy
	 */
	public void reset(Piece ghost) {
		reset();
		for (Square s : ghost.squares()) {
			add(s.i, s.j);
		}
	}

	/**
	 * Returns an independent copy of this piece's current occupied-cell pattern.
	 * @return cloned piece in the current orientation
	 */
	public Piece clone(){
		Piece clone = new Piece(size, type, rotations, flips);
		for (Square s : squares()) clone.add(s);
		return clone;
	}
	
	/**
	 * Adds a local cell from a coordinate object.
	 * @param s local cell coordinate to add
	 * @return this piece, allowing chained shape construction
	 */
	public Piece add(Square s) {
		return add(s.i, s.j);
	}

	/**
	 * Adds an occupied cell at local coordinates and updates the shape's cell count.
	 *
	 * @param x horizontal local coordinate
	 * @param y vertical local coordinate
	 * @return this piece, allowing chained shape construction
	 * @throws IllegalArgumentException if the coordinate is outside the local matrix or already occupied
	 */
	public Piece add(int x, int y) {
		int i=x+h;
		int j=y+h;
		if (i<0 || i>= size) throw new IllegalArgumentException(); 
		if (j<0 || j>= size) throw new IllegalArgumentException(); 
		if (a[i][j]>0) throw new IllegalArgumentException(); 
		a[i][j] = 1;
		count++;
		return this;
	}

	private int[][] v() {
		return ( actual ? a : b);
	}
	private int[][] w() {
		return ( actual ? b : a);
	}

	private void toggle() {
		actual = ! actual;
	}
	
	private int get(int x, int y) {
		return v()[x+h][y+h];
	}
	private void set(int x, int y, int value) {
		w()[x+h][y+h] = value;
	}
	
	/**
	 * Returns the occupancy value at a local coordinate, treating out-of-range cells as empty.
	 *
	 * @param x horizontal local coordinate
	 * @param y vertical local coordinate
	 * @return one for an occupied cell, or zero otherwise
	 */
	public int getValue(int x, int y) {
		if (x<-h || x>=-h+size || y<-h || y>=-h+size ) return 0;
		return get(x,y);
	}
	/**
	 * Tests whether a local coordinate is occupied by this piece.
	 * @param x horizontal local coordinate
	 * @param y vertical local coordinate
	 * @return {@code true} if the coordinate is occupied
	 */
	public boolean isValue(int x, int y) {
		return getValue(x, y)>0;
		
	}
	
	/**
	 * Rotates the shape one quarter turn clockwise or counterclockwise.
	 * @param dir positive for clockwise, negative for counterclockwise
	 * @return this piece in its updated orientation
	 */
	public Piece rotate(int dir) {
		for (int y=-h; y<-h+size; y++) {
			for (int x=-h; x<-h+size; x++) {
				if (odd) set(x,y, (dir>0 ? get(y,-x) : get(-y,x)) );
				else {
					if (dir<0) set(x,y, get(-y+1,x) );
					else set(x,y, get( y, -x+1) );
				}
			}
		}
		r = (r+1) % rotations ;
		toggle();
		return this;
	}

	/**
	 * Mirrors the shape across its vertical axis.
	 * @return this piece in its mirrored orientation
	 */
	public Piece flip() {
		for (int x=-h; x<-h+size; x++) {
			for (int y=-h; y<-h+size; y++) {
				if (odd) set(x,y, get(-x,y));
				else set(x,y, get(-x+1,y));
			}
		}
		f = (f+1) % flips ;
		toggle();
		return this;
	}

	/**
	 * Returns the stable identifier used to label this shape.
	 * @return shape identifier
	 */
	public String toLabel() {
		return type;
	}

	/**
	 * Renders the local occupancy matrix as a diagnostic string.
	 * @return text representation of the shape matrix
	 */
	@Override
	public String toString() {
		String str = type + "\n";
		for (int y=-h; y<-h+size; y++) {
			for (int x=-h; x<-h+size; x++) {
				str += get(x,y) + (x==-h+size-1? "\n" : " | ") ; 	
			}
		}
		return str;
		
	}
	
	/**
	 * Tests whether an empty local cell shares an edge with this shape.
	 * @param x horizontal local coordinate
	 * @param y vertical local coordinate
	 * @return {@code true} if the cell is empty and edge-adjacent to the shape
	 */
	public boolean touches(int x, int y) {
		if (isValue(x, y)) return false;
		return ( isValue(x-1, y) || isValue(x, y-1) || isValue(x+1, y) || isValue(x, y+1));
	}
	/**
	 * Tests whether an empty local cell touches this shape diagonally but not by an edge.
	 * @param x horizontal local coordinate
	 * @param y vertical local coordinate
	 * @return {@code true} if the cell is an empty diagonal contact without an edge contact
	 */
	public boolean crosses(int x, int y) {
		if (isValue(x, y)) return false;
		if (touches(x, y)) return false;
		return ( isValue(x-1, y-1) || isValue(x+1, y-1) || isValue(x+1, y+1) || isValue(x-1, y+1));
	}

	/**
	 * Tests whether this shape and another shape share any occupied cells at a relative offset.
	 * @param that other shape to compare
	 * @param X horizontal offset of the other shape
	 * @param Y vertical offset of the other shape
	 * @return {@code true} if the two shapes share an occupied cell
	 */
	public boolean overlaps(Piece that, int X, int Y) {
		if ( Math.abs( X ) > (this.size + that.size)/2 ) return false;  
		if ( Math.abs( Y ) > (this.size + that.size)/2 ) return false;  
		for (int y=-h; y<-h+size; y++) {
			for (int x=-h; x<-h+size; x++) {
				if ( that.isValue(X+x, Y+y)) return true; 	
			}
		}
		return false;
	}
	
	/**
	 * Compares the stable type and occupied-cell pattern of this shape with another object.
	 * @param obj object to compare
	 * @return {@code true} when the object has the same type and occupied-cell pattern
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		Piece other = (Piece) obj;
		if (other.type != this.type) return false;
		for (int y=-h; y<-h+size; y++) {
			for (int x=-h; x<-h+size; x++) {
				if (this.get(x,y) !=  other.get(x,y)) return false; 	
			}
		}
		return true;
	}

	/**
	 * Returns the occupied cells in the current orientation using local coordinates.
	 * @return occupied local coordinates
	 */
	public List<Square> squares() {
		List<Square> list = new ArrayList<Square>();
		for (int y=-h; y<-h+size; y++) {
			for (int x=-h; x<-h+size; x++) {
				if ( isValue(x, y)) list.add(new Square(x, y, 3)); 	
			}
		}
		return list;
	}
	
	/**
	 * Returns occupied cells and, for the owning player, edge-adjacent contact cells.
	 * @param color player index whose board contact markers are requested
	 * @return occupied cells, with owner-specific adjacent cells when applicable
	 */
	public List<Square> squares(int color) {
		List<Square> list = new ArrayList<Square>();
		if (color != this.color) {
			return squares();
		} else {
			for (int y=-h-1; y<-h+size+1; y++) {
				for (int x=-h-1; x<-h+size+1; x++) {
					if ( isValue(x, y)) list.add(new Square(x, y, 3)); 	
					else if ( touches(x, y)) list.add(new Square(x, y, 2)); 	
				}
			}
		}
		return list;
	}

	/**
	 * Returns empty local cells that touch this shape diagonally and can extend play.
	 * @return diagonal contact coordinates around this shape
	 */
	public List<Square> seeds() {
		List<Square> list = new ArrayList<Square>();
		for (int y=-h-1; y<-h+size+1; y++) {
			for (int x=-h-1; x<-h+size+1; x++) {
				if ( crosses(x, y)) list.add(new Square(x, y)); 	
			}
		}
		return list;
	}
	
	/**
	 * Encodes a piece's owner, identifier, and occupied local coordinates.
	 * @param piece piece to encode
	 * @return serialized owner, type, and cell-coordinate record
	 */
	public static String serialize(Piece piece) {
		String msg = "" + piece.color;
		msg += ":"+piece.type;
		for (Square s : piece.squares()) {
			msg+=":"+s.i+","+s.j;
		}
		return msg;
	}
	
}
