package com.newyith.fortressmod;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Stack;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class Wall {
	private static boolean blockTypesCreated = false;
	private static ArrayList<Block> wallBlocks = new ArrayList<Block>();
	private static ArrayList<Block> disabledWallBlocks = new ArrayList<Block>();
	private static ArrayList<Block> enabledWallBlocks = new ArrayList<Block>();
	private static ArrayList<Block> notCloggedGeneratorBlocks = new ArrayList<Block>();

	/**
	 * Looks at all blocks connected to the generator by wallBlocks (directly or recursively).
	 * Connected means within 3x3x3.
	 * 
	 * @param wallBlocks List of connecting block types.
	 * @param returnBlocks List of block types to look for and return when connected to the wall.
	 * @return List of all points (blocks) connected to the generator by wallBlocks and matching a block type in returnBlocks.
	 */
	public static ArrayList<Point> getPointsConnected(World world, Point origin, ArrayList<Block> wallBlocks, ArrayList<Block> returnBlocks) {
		ArrayList<Point> matches = new ArrayList<Point>();
		
		HashSet<String> visited = new HashSet<String>();
		Stack<Point> layer = new Stack<Point>();
		Stack<Point> nextLayer = new Stack<Point>();
		//Deque<Point> layer = new ArrayDeque<Point>(); //TODO: switch to using Deque
		//Deque<Point> nextLayer = new ArrayDeque<Point>();
		Block b;
		String key;
		Point p;
		Point center;
		
		nextLayer.push(origin);
		visited.add(makeKey(origin));
		
		int recursionLimit = 50; //TODO: increase this?
		while (!nextLayer.isEmpty()) {
			if (recursionLimit-- <= 0) {
				Dbg.print("FortressWallUpdater.update(): recursionLimit exhausted");
				break;
			}

			layer = nextLayer;
			nextLayer = new Stack<Point>();
			//nextLayer = new ArrayDeque<Point>();
			
			//process layer
			int recursionLimit2 = 150; //TODO: increase this?
			while (!layer.isEmpty()) {
				if (recursionLimit2-- <= 0) {
					Dbg.print("FortressWallUpdater.update(): recursionLimit2 exhausted");
					break;
				}
				
				center = layer.pop();
				//iterate over the 27 (3*3*3) blocks around center
				for (int x = center.x-1; x <= center.x+1; x++) {
					for (int y = center.y-1; y <= center.y+1; y++) {
						for (int z = center.z-1; z <= center.z+1; z++) {
							p = new Point(x, y, z);
							key = makeKey(p);
							if (!visited.contains(key)) {
								visited.add(key);

								b = world.getBlock(x, y, z); //b is one of the 26 blocks around the center block
								
								//add to matches if matches a returnBlocks type
								if (returnBlocks.contains(b))
									matches.add(p);

								//process block
								if (wallBlocks.contains(b)) {
									nextLayer.push(p);
								}
							}
						}
					}
				}
			}
		}
		
		Dbg.print("Wall.getPointsConnected visited " + String.valueOf(visited.size()));
		Dbg.print("Wall.getPointsConnected returning " + String.valueOf(matches.size()) + " matches");
		
		return matches;
	}

	private static String makeKey(Point p) {
		return makeKey(p.x, p.y, p.z);
	}
	
	private static String makeKey(int x, int y, int z) {
		String key;
		key = Integer.valueOf(x) + "," + Integer.valueOf(y) + "," + Integer.valueOf(z);
		return key;
	}
	
	//-------------------
	
	public static ArrayList<Block> getWallBlocks() {
		ensureBlockTypeConstantsExist();
		return wallBlocks;
	}

	public static ArrayList<Block> getEnabledWallBlocks() {
		ensureBlockTypeConstantsExist();
		return enabledWallBlocks;
	}

	public static ArrayList<Block> getDisabledWallBlocks() {
		ensureBlockTypeConstantsExist();
		return disabledWallBlocks;
	}

	public static ArrayList<Block> getNotCloggedGeneratorBlocks() {
		ensureBlockTypeConstantsExist();
		return notCloggedGeneratorBlocks;
	}

	private static void ensureBlockTypeConstantsExist() {
		if (!blockTypesCreated) {
			//fill degeneratedWallBlocks (must be added in the same order as generated)
			disabledWallBlocks.add(Blocks.cobblestone);
			
			//fill generatedWallBlocks (must be added in the same order as degenerated)
			enabledWallBlocks.add(FortressMod.fortressBedrock);
			
			//fill wallBlocks
			for (Block b : disabledWallBlocks)
				wallBlocks.add(b);
			for (Block b : enabledWallBlocks)
				wallBlocks.add(b);
			
			//fill notCloggedGeneratorBlocks
			notCloggedGeneratorBlocks.add(FortressMod.fortressGenerator);
			notCloggedGeneratorBlocks.add(FortressMod.fortressGeneratorOn);
			
			blockTypesCreated = true;
		}
	}
}
















