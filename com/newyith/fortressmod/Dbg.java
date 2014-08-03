package com.newyith.fortressmod;

public class Dbg {
	private static String lastS;
	private static int count;
	
	public Dbg() {
		lastS = "";
		count = 0;
	}
	
	public static void print(String s, boolean isRemote) {
		if (s == lastS) {
			count ++;
		} else {
			lastS = s;
			count = 0;
			
			s += "::";
			if (count > 1) {
				s += count + " * ";
			}
			if (isRemote) {
				s += "remote";
			} else {
				s += "local";
			}
			System.out.println(s);
		}
	}
}
