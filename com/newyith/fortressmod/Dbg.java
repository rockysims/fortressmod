package com.newyith.fortressmod;

import java.util.Date;

public class Dbg {
	private static long firstTimestamp = 0;
	
	private static String getElapsedTime() {
		long now = new Date().getTime();
		if (firstTimestamp == 0)
			firstTimestamp = now;
		long elapsed = now - firstTimestamp;
		return String.valueOf(elapsed) + "ms";
	}
	
	public static void print(String s, boolean isRemote) {
//		if (s.equals(lastS)) {
//			count++;
//		} else {
			s += "::";
			s += getElapsedTime() + " ";
			if (isRemote) {
				s += "client";
			} else {
				s += "server";
			}
			System.out.println(s);
//		}
	}

	public static void print(String s) {
		s += "::" + getElapsedTime();
		System.out.println(s);
	}
}
