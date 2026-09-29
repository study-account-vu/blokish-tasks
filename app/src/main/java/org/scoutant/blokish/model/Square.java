
package org.scoutant.blokish.model;

public class Square implements Comparable<Square> {
	
	public int i;
	public int j;
	public int value;
	
	public Square(int i, int j) {
		this.i=i;
		this.j=j;
	}
	public Square(int i, int j, int value) {
		this(i,j);
		this.value = value;
	}
	
	public String toString() {
		return "("+i+", "+j+") ";
	}

	public int compareTo(Square that) {
		return this.distance()-that.distance();
	}
	
	private int distance() {
		return (i-10)*(i-10)+(j-10)*(j-10);
	}
	
}
