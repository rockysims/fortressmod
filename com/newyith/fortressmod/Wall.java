package com.newyith.fortressmod;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import com.google.common.collect.ImmutableList;
import com.newyith.fortressmod.Wall.ConnectedThreshold;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class Wall {
	private static boolean blockTypesCreated = false;
	private static ArrayList<Block> wallBlocks = new ArrayList<Block>();
	private static ArrayList<Block> disabledWallBlocks = new ArrayList<Block>();
	private static ArrayList<Block> enabledWallBlocks = new ArrayList<Block>();
	private static ArrayList<Block> notCloggedGeneratorBlocks = new ArrayList<Block>();
	
	public static enum ConnectedThreshold {
		POINTS,
		//LINES,
		FACES
	};

	public static Set<Point> flattenLayers(List<List<Point>> layers) {
		Set<Point> points = new HashSet<Point>();
		
		for (List<Point> layer : layers) {
			for (Point p : layer) {
				points.add(p);
			}
		}
		
		return points;
	}
	
	public static Set<Point> getPointsConnected(World world, Point origin, ArrayList<Block> wallBlocks, ArrayList<Block> returnBlocks, ConnectedThreshold connectedThreshold) {
		Set<Point> originLayer = new HashSet<Point>();
		originLayer.add(origin);
		Set<Point> ignorePoints = new HashSet<Point>();
		List<List<Point>> layers = getPointsConnectedAsLayers(world, origin, originLayer, wallBlocks, returnBlocks, GeneratorCore.generationRangeLimit, ignorePoints, connectedThreshold);
		return flattenLayers(layers);
	}

	public static Set<Point> getPointsConnected(World world, Point origin, ArrayList<Block> wallBlocks, ArrayList<Block> returnBlocks, int rangeLimit, Set<Point> ignorePoints) {
		Set<Point> originLayer = new HashSet<Point>();
		originLayer.add(origin);
		//List<List<Point>> layers = getPointsConnectedAsLayers(world, origin, originLayer, wallBlocks, returnBlocks, rangeLimit, ignorePoints, ConnectedThreshold.POINTS);
		List<List<Point>> layers = getPointsConnectedAsLayers(world, origin, originLayer, wallBlocks, returnBlocks, rangeLimit, ignorePoints, ConnectedThreshold.FACES);
		return flattenLayers(layers);
	}

	public static Set<Point> getPointsConnected(World world, Point origin, Set<Point> originLayer, List<Block> wallBlocks, List<Block> returnBlocks, int rangeLimit, Set<Point> ignorePoints) {
		//List<List<Point>> layers = getPointsConnectedAsLayers(world, origin, originLayer, wallBlocks, returnBlocks, rangeLimit, ignorePoints, ConnectedThreshold.POINTS);
		List<List<Point>> layers = getPointsConnectedAsLayers(world, origin, originLayer, wallBlocks, returnBlocks, rangeLimit, ignorePoints, ConnectedThreshold.FACES);
		return flattenLayers(layers);
	}
	
	public static List<List<Point>> getPointsConnectedAsLayers(World world, Point origin, ArrayList<Block> wallBlocks, ArrayList<Block> returnBlocks, int rangeLimit, Set<Point> ignorePoints) {
		Set<Point> originLayer = new HashSet<Point>();
		originLayer.add(origin);
		//return getPointsConnectedAsLayers(world, origin, originLayer, wallBlocks, returnBlocks, rangeLimit, ignorePoints, ConnectedThreshold.POINTS);
		return getPointsConnectedAsLayers(world, origin, originLayer, wallBlocks, returnBlocks, rangeLimit, ignorePoints, ConnectedThreshold.FACES);
	}
	
	/**
	 * Looks at all blocks connected to the generator by wallBlocks (directly or recursively).
	 * 
	 * @param origin The rangeLimit is calculated relative to this point.
	 * @param originLayer The first point(s) to search outward from.
	 * @param wallBlocks List of connecting block types.
	 * @param returnBlocks List of block types to look for and return when connected to the wall or null to return all block types.
	 * @param rangeLimit The maximum distance away from origin to search. 
	 * @param ignorePoints When searching, these points will be ignored (not traversed or returned). If null, no points ignored.
	 * @param connectedThreshold Whether connected means 3x3x3 area or only the 6 blocks connected by faces.
	 * @return List of all points (blocks) connected to the originLayer by wallBlocks and matching a block type in returnBlocks.
	 */
	public static List<List<Point>> getPointsConnectedAsLayers(World world, Point origin, Set<Point> originLayer, List<Block> wallBlocks, List<Block> returnBlocks, int rangeLimit, Set<Point> ignorePoints, ConnectedThreshold connectedThreshold) {
		//Dbg.start("getPointsConnectedAsLayers() top to bottom");
		
		List<List<Point>> matchesAsLayers = new ArrayList<List<Point>>();
		ArrayList<Point> connected = new ArrayList<Point>();
		
		Set<String> visited = new HashSet<String>();
		Deque<Point> layer = new ArrayDeque<Point>();
		Deque<Point> nextLayer = new ArrayDeque<Point>();
		int layerIndex = -1;
		Block b;
		String key;
		Point center;
		
		//fill nextLayer from originLayer
		for (Point p : originLayer) {
			nextLayer.push(p);
			visited.add(makeKey(p));
		}
		
		//make ignorePoints default to empty
		if (ignorePoints == null)
			ignorePoints = new HashSet<Point>();
		
		int recursionLimit = (int)Math.pow(rangeLimit/2, 3);
		while (!nextLayer.isEmpty()) {
			if (recursionLimit-- <= 0) {
				Dbg.print("FortressWallUpdater.update(): recursionLimit exhausted");
				break;
			}

			layerIndex++;
			layer = nextLayer;
			nextLayer = new ArrayDeque<Point>();
			
			//Dbg.start("process layer");
			//Dbg.print("layer.size(): " + String.valueOf(layer.size()));

			//process layer
			int recursionLimit2 = 6*(int)Math.pow(rangeLimit*2, 2);
			while (!layer.isEmpty()) {
				//Dbg.start("inner loop");
				
				if (recursionLimit2-- <= 0) {
					Dbg.print("FortressWallUpdater.update(): recursionLimit2 exhausted");
					break;
				}
				
				//Dbg.start("find connected points");

				center = layer.pop();
				connected.clear();
				
				//handle ConnectedThreshold.POINTS
				if (connectedThreshold == ConnectedThreshold.POINTS) {
					//iterate over the 27 (3*3*3) blocks around center
					for (int x = center.x-1; x <= center.x+1; x++) {
						for (int y = center.y-1; y <= center.y+1; y++) {
							for (int z = center.z-1; z <= center.z+1; z++) {
								connected.add(new Point(x, y, z));
							}
						}
					}
				}
				
				//handle ConnectedThreshold.FACES
				if (connectedThreshold == ConnectedThreshold.FACES) {
					//iterate over the 6 blocks adjacent to center
					int x = center.x;
					int y = center.y;
					int z = center.z;
					connected.add(new Point(x+1, y, z));
					connected.add(new Point(x-1, y, z));
					connected.add(new Point(x, y+1, z));
					connected.add(new Point(x, y-1, z));
					connected.add(new Point(x, y, z+1));
					connected.add(new Point(x, y, z-1));
				}

				//Dbg.stop("find connected points");
				
				//Dbg.start("process connected points");

				//process connected points
				for (Point p : connected) {
					key = makeKey(p);
					if (!visited.contains(key)) {
						visited.add(key);

						//ignore ignorePoints
						if (ignorePoints.contains(p))
							continue;
						
						//ignore out of range points
						if (!isInRange(p, origin, rangeLimit))
							continue;
						
						b = world.getBlock(p.x, p.y, p.z); //b is one of the blocks connected to the center block
						
						//add to matchesAsLayers if it matches a returnBlocks type
						if (returnBlocks == null || returnBlocks.contains(b)) {
							//"while" not "if" because maybe only matching blocks are far away but connected by wall
							while (layerIndex >= matchesAsLayers.size()) {
								matchesAsLayers.add(new ArrayList<Point>());
							}
							matchesAsLayers.get(layerIndex).add(p);
						}

						//process block
						if (wallBlocks.contains(b)) {
							nextLayer.push(p);
						}
					}
				}
				
				//Dbg.stop("process connected points");
				
				//Dbg.stop("inner loop");

			}
			
			//Dbg.stop("process layer");

		}
		
		Dbg.print("Wall.getPointsConnected visited " + String.valueOf(visited.size()));
		Dbg.print("Wall.getPointsConnected returning " + String.valueOf(matchesAsLayers.size()) + " matchesAsLayers");
		
		//Dbg.stop("getPointsConnectedAsLayers() top to bottom");

		return matchesAsLayers;
	}
	
	private static boolean isInRange(Point p, Point origin, int rangeLimit) {
		boolean inRange = true;
		
		inRange = inRange && (Math.abs(p.x - origin.x)) <= rangeLimit;
		inRange = inRange && (Math.abs(p.y - origin.y)) <= rangeLimit;
		inRange = inRange && (Math.abs(p.z - origin.z)) <= rangeLimit;
		
		return inRange;
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
			
			
			
			//disabledWallBlocks.add(Blocks.glass); //TODO: change back to this line
			disabledWallBlocks.add(Blocks.stone);
			
			
			
			
			disabledWallBlocks.add(Blocks.obsidian);
			disabledWallBlocks.add(Blocks.wooden_door);
			disabledWallBlocks.add(Blocks.iron_door);
			
			//fill generatedWallBlocks (must be added in the same order as degenerated)
			enabledWallBlocks.add(FortressMod.fortressBedrock);
			enabledWallBlocks.add(FortressMod.fortressGlass);
			enabledWallBlocks.add(FortressMod.fortressObsidian);
			enabledWallBlocks.add(FortressMod.fortressWoodenDoor);
			enabledWallBlocks.add(FortressMod.fortressIronDoor);
			
			//fill wallBlocks
			for (Block b : disabledWallBlocks)
				wallBlocks.add(b);
			for (Block b : enabledWallBlocks)
				wallBlocks.add(b);
			
			//fill notCloggedGeneratorBlocks
			notCloggedGeneratorBlocks.add(FortressMod.fortressGenerator);
			notCloggedGeneratorBlocks.add(FortressMod.fortressGeneratorOn);
			notCloggedGeneratorBlocks.add(FortressMod.fortressGeneratorPaused);
			
			blockTypesCreated = true;
		}
	}

	@SideOnly(Side.CLIENT)
	public static void randomDisplayTick(World world, int xParam, int yParam, int zParam, Random random) {
		/*
		if (random.nextFloat() * 100.0F < 2.5F) {
			int meta = world.getBlockMetadata(xParam, yParam, zParam);
			float x = (float)xParam + 0.5F;
			float y = (float)yParam + -0.10F;
			float z = (float)zParam + 0.5F;
			float f3 = 0.52F;
			float f4 = random.nextFloat() * 1.0F - 0.5F;

			y += (random.nextFloat() * 16.0F / 16.0F) - 0.5F;
			
			//all 4 sides
			float side = random.nextFloat() * 4.0F;
			if (side <= 1) {
				spawnWallParticle(world, (double)(x - f3), (double)y, (double)(z + f4), random);
			} else if (side <= 2) {
				spawnWallParticle(world, (double)(x + f3), (double)y, (double)(z - f4), random);
			} else if (side <= 3) {
				spawnWallParticle(world, (double)(x + f4), (double)y, (double)(z - f3), random);
			} else if (side <= 4) {
				spawnWallParticle(world, (double)(x - f4), (double)y, (double)(z + f3), random);
			} 
			//TODO: delete next line
			else { Dbg.print("error: side == " + String.valueOf(side)); }
		}
		//*/
	}
	private static void spawnWallParticle(World world, double x, double y, double z, Random random) {
		world.spawnParticle("portal", x, y, z, 0.0D, 0.0D, 0.0D);
		
		/*
		List<String> particleNames = new ArrayList<String>(Arrays.asList(
//				"hugeexplosion",
//				"largeexplode",
				"bubble",
//				"suspended",
//				"depthsuspend",
//				"townaura",
//				"crit",
//				"magicCrit", //green diamonds
//				"smoke",
//				"mobSpell",
//				"spell",
				"instantSpell",
//				"note",
//				"portal", //nether portal particle
//				"enchantmenttable",
//				"explode",
//				"flame",
//				"lava",
//				"footstep",
//				"splash",
//				"largesmoke",
//				"cloud",
//				"reddust",
//				"snowballpoof",
//				"dripWater",
//				"dripLava",
//				"snowshovel",
//				"slime"
//				"heart"
//				"iconcrack_",
//				"tilecrack_"
				""
				));
				
		int particleIndex = Math.floor(random.nextFloat() * (particleNames.size()-1));
		Dbg.print(String.valueOf(particleIndex));
		world.spawnParticle(particleNames.get(particleIndex), x, y, z, 0.0D, 0.0D, 0.0D);
		//*/
		
		
	}
}
















