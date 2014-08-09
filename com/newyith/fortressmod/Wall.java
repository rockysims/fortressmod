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
		
		p = new Point(origin.x, origin.y, origin.z); //fortress generator's coordinates
		nextLayer.push(p);
		visited.add(makeKey(p));
		
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
							key = makeKey(x, y, z);
							if (!visited.contains(key)) {
								visited.add(key);
								
								if (world == null)
									Dbg.print("null world here");

								//process block
								b = world.getBlock(x, y, z); //b is one of the 26 blocks around the center block
								if (wallBlocks.contains(b)) {
									p = new Point(x, y, z);
									nextLayer.push(p);
									if (returnBlocks.contains(b))
										matches.add(p);
								}
							}
						}
					}
				}
			}
		}
		
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

	/*
	public void update(boolean isGenerating, boolean bedrockMode, World world, int xCoord, int yCoord, int zCoord) {
		//TODO: change this so it discovers generators rather than being passed isGenerating and bedrockMode
		
		HashSet<String> visited = new HashSet<String>();
		Stack<Point> generated = new Stack<Point>();
		Stack<Point> layer = new Stack<Point>();
		Stack<Point> nextLayer = new Stack<Point>();
		Block b;
		String key;
		Point p;
		Point center;
		
		p = new Point(xCoord, yCoord, zCoord); //fortress generator's coordinates
		nextLayer.push(p);
		visited.add(makeKey(p.x, p.y, p.z));
		
		int recursionLimit = 50; //TODO: increase this
		while (!nextLayer.isEmpty()) {
			if (recursionLimit-- <= 0) {
				Dbg.print("FortressWallUpdater.update(): recursionLimit exhausted");
				break;
			}

			layer = nextLayer;
			nextLayer = new Stack<Point>();
			
			//process layer
			int recursionLimit2 = 150; //TODO: increase this
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
							key = makeKey(x, y, z);
							if (!visited.contains(key)) {
								visited.add(key);
								
								//process block
								b = world.getBlock(x, y, z); //b is one of the 26 blocks around the center block
								if (isWall(b)) {
									p = new Point(x, y, z);
									nextLayer.push(p);
									generated.push(p);
								}
							}
						}
					}
				}
			}
		}
		
		while (!generated.isEmpty()) {
			p = generated.pop();
			b = world.getBlock(p.x, p.y, p.z);
			if (isGenerating)
				b = getGeneratedBlock(b, bedrockMode);
			else
				b = getDegeneratedBlock(b);
			
			if (b != null) {
				world.setBlock(p.x, p.y, p.z, b);
			}
		}
		
	}
	//*/
}
















