package com.newyith.fortressmod;

public class Dbg {
	private static String lastS;
	private static int count;
	
	public Dbg() {
		lastS = "";
		count = 0;
	}
	
	public static void print(String s, boolean isRemote) {
		if (s.equals(lastS)) {
			count++;
		} else {
			lastS = s;
			
			s += "::";
			if (count > 1) {
				s = count + " * " + s;
				count = 0;
			}
			if (isRemote) {
				s += "client";
			} else {
				s += "server";
			}
			System.out.println(s);
		}
	}

	public static void print(String s) {
		System.out.println(s);
	}
}
