package com.newyith.fortressmod;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

	private static Map<String, Long> timestamps = new HashMap<String, Long>();
	private static Map<String, Integer> durations = new HashMap<String, Integer>();
	
	public static void start(String key) {
		String extraStr = "";
		if (timestamps.containsKey(key)) {
			extraStr = " WAS ALREADY STARTED";
		}
		
		long now = System.currentTimeMillis();
		timestamps.put(key, now);
		
		if (extraStr.length() > 0) {
			Dbg.print("Timer \"" + key + "\" started." + extraStr);
		}
	}
	
	public static void stop(String key) {
		long now = System.currentTimeMillis();
		long stamp = timestamps.get(key);
		timestamps.remove(key);
		int durationMs = (int)(now - stamp);
		
		if (!durations.containsKey(key)) {
			durations.put(key, 0);
		}
		durations.put(key, durations.get(key) + durationMs);
		
		Dbg.print("Timer \"" + key + "\" stopped after " + String.valueOf(durationMs) + "ms.");
		
	}
	
	public static void duration(String key) {
		int durationMs = durations.get(key);
		Dbg.print("Timer \"" + key + "\" total duration is " + String.valueOf(durationMs) + "ms.");
	}
}
