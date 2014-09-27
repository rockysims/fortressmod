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
		Dbg.print("hashCode() for " + this);
		return this.toString().hashCode();
    }
	
	@Override
	public boolean equals(Object obj) {
		Point p = (Point)obj;
		boolean samePoint = (x == p.x) && (y == p.y) && (z == p.z);
		Dbg.print("equals(): " + this + " ?= " + p + " (" + String.valueOf(samePoint) + ")");
		return samePoint;
	}
}
