package com.newyith.fortressmod;

import java.util.HashSet;
import java.util.Stack;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class FortressWallUpdater {
	public void update(boolean isGenerating, boolean bedrockMode, World world, int xCoord, int yCoord, int zCoord) {
		//TODO: change this so it discovers generators rather than being passed isGenerating and bedrockMode
		
		Dbg.print("FortressWallUpdater.update()");
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
		
		int recursionLimit = 15; //TODO: increase this
		while (!nextLayer.isEmpty()) {
			if (recursionLimit-- <= 0) {
				Dbg.print("FortressWallUpdater.update(): recursionLimit exhausted");
				break;
			}

			layer = nextLayer;
			nextLayer = new Stack<Point>();
			
			//process layer
			int recursionLimit2 = 50; //TODO: increase this
			while (!layer.isEmpty()) {
				if (recursionLimit2-- <= 0) {
					Dbg.print("FortressWallUpdater.update(): recursionLimit2 exhausted");
					break;
				}
				
				center = layer.pop();
				Dbg.print("FortressWallUpdater.update() processing layer centered at " + makeKey(center.x, center.y, center.z));
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
							} else {
								//Dbg.print("already visited");
							}
						}
					}
				}
			}
		}
		
		Dbg.print("update " + generated.size() + " wall blocks");

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

	private Block getDegeneratedBlock(Block b) {
		if (b == FortressMod.fortressBedrock) {
			return Blocks.cobblestone;
		}
		
		return null;
	}

	private Block getGeneratedBlock(Block b, boolean bedrockMode) {
		if (bedrockMode) {
			if (b == Blocks.cobblestone) {
				Dbg.print("is cobblestone");
				return FortressMod.fortressBedrock;
			} else {
				Dbg.print("not cobblestone: " + b.getLocalizedName());
			}
		} //TODO: implement obsidian fortress walls?

		return null;
	}

	private boolean isWall(Block b) {
		if (b == Blocks.cobblestone)
			return true;
		if (b == FortressMod.fortressBedrock)
			return true;
		
		return false;
	}

	private String makeKey(int x, int y, int z) {
		String key;
		key = Integer.valueOf(x) + "," + Integer.valueOf(y) + "," + Integer.valueOf(z);
		return key;
	}
}
