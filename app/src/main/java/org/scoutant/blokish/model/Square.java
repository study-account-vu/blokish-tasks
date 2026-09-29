
package org.scoutant.blokish.model;

/** A cell coordinate and optional occupancy value used by pieces and boards. */
public class Square implements Comparable<Square> {
	
	public int i;
	public int j;
	public int value;
	
	/**
	 * Creates a coordinate-only square.
	 *
	 * @param i horizontal coordinate
	 * @param j vertical coordinate
	 */
	public Square(int i, int j) {
		this.i=i;
		this.j=j;
	}

	/**
	 * Creates a square with an occupancy or contact marker.
	 *
	 * @param i horizontal coordinate
	 * @param j vertical coordinate
	 * @param value marker associated with the square
	 */
	public Square(int i, int j, int value) {
		this(i,j);
		this.value = value;
	}
	
	/**
	 * Returns the coordinate as a compact diagnostic string.
	 * @return text representation of the two coordinates
	 */
	public String toString() {
		return "("+i+", "+j+") ";
	}

	/**
	 * Orders squares by squared distance from the board center at coordinate (10, 10).
	 * @param that square to compare with
	 * @return negative, zero, or positive according to relative center distance
	 */
	public int compareTo(Square that) {
		return this.distance()-that.distance();
	}
	
	/** Computes squared distance from the board center without a square root. */
	private int distance() {
		return (i-10)*(i-10)+(j-10)*(j-10);
	}
}
