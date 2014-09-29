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
		int hash = (int) (x ^ (x >>> 16));
		hash = 15 * hash + (int) (y ^ (y >>> 16));
        hash = 15 * hash + (int) (z ^ (z >>> 16));
		//Dbg.print("hashCode() for " + this + ": " + String.valueOf(hash));
        return hash;
    }
	
	@Override
	public boolean equals(Object obj) {
		Point p = (Point)obj;
		boolean samePoint = (x == p.x) && (y == p.y) && (z == p.z);
		//Dbg.print("equals(): " + this + " ?= " + p + " (" + String.valueOf(samePoint) + ")");
		return samePoint;
	}
}
