package com.newyith.fortressmod;

public class Point {

	public int x;
	public int y;
	public int z;

	public Point(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public String toString() {
		return String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(z);
	}

	@Override
	public int hashCode() {
		int hash = x;
		hash = 49999 * hash + y;
		hash = 49999 * hash + z;
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		Point p = (Point)obj;
		return (x == p.x) && (y == p.y) && (z == p.z);
	}
}
