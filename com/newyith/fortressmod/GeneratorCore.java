package com.newyith.fortressmod;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class GeneratorCore {
	private ArrayList<Point> generatedPoints = new ArrayList<Point>(); //contains currently generated blocks (points)
	public long timePlaced = 0; //TODO: change this back to private
	private TileEntityFortressGenerator fortressGenerator;
	private World world;
	
	private String placedByPlayerName = "none"; //set in onPlaced


	public GeneratorCore(TileEntityFortressGenerator fortressGenerator) {
		this.fortressGenerator = fortressGenerator;
	}

	public void setWorldObj(World world) {
		this.world = world;
	}
	
	public void writeToNBT(NBTTagCompound compound) {
		//save list of generated blocks
		NBTTagList list = new NBTTagList();
		for (Point p : this.generatedPoints) {
			NBTTagCompound item = new NBTTagCompound();
			item.setInteger("x", p.x);
			item.setInteger("y", p.y);
			item.setInteger("z", p.z);
			list.appendTag(item);
		}
		compound.setTag("generated", list);
		
		//save the other stuff
		compound.setLong("timePlaced", this.timePlaced);
		compound.setString("placedByPlayerName", this.placedByPlayerName);
	}
	
	public void readFromNBT(NBTTagCompound compound) {
		//load list of generated blocks
		Point p;
		NBTTagList list = compound.getTagList("generated", NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound item = list.getCompoundTagAt(i);
			int x = item.getInteger("x");
			int y = item.getInteger("y");
			int z = item.getInteger("z");
			p = new Point(x, y, z);
			this.generatedPoints.add(p);
		}
		
		//load the other stuff
		this.timePlaced = compound.getLong("timePlaced");
		this.placedByPlayerName = compound.getString("placedByPlayerName");
	}
	
	public static void onPlaced(World world, int x, int y, int z, String placingPlayerName) {
		//clog unless its the only none clogged generator (in which case degenerated connected wall)
		if (!world.isRemote) {
			TileEntityFortressGenerator placedFortressGenerator = (TileEntityFortressGenerator) world.getTileEntity(x, y, z);
			GeneratorCore placedCore = placedFortressGenerator.getGeneratorCore();
			
			//set timePlaced
			placedCore.timePlaced = System.currentTimeMillis();
			placedCore.placedByPlayerName = placingPlayerName;
			
			//clog or degenerate
			boolean isOldest = isOldestNotCloggedGeneratorConnectedTo(placedCore);
			if (!isOldest) {
				placedCore.clog();
			} else {
				placedCore.degenerateWall();
			}
		}
	}
	
	//Not called when broken and then replaced by different version of fortress generator (on, off, clogged)
	public static void onBroken(World world, int x, int y, int z) {
		if (!world.isRemote) {
			TileEntityFortressGenerator brokenFortressGenerator = (TileEntityFortressGenerator) world.getTileEntity(x, y, z);
			
			if (!brokenFortressGenerator.isClogged()) {
				GeneratorCore brokenCore = brokenFortressGenerator.getGeneratorCore();

				//degenerate generated wall (and connected wall if oldest)
				brokenCore.degenerateWall();

				//if (oldestGenerator) clog the others
				if (isOldestNotCloggedGeneratorConnectedTo(brokenCore)) {
					ArrayList<TileEntityFortressGenerator> fgs = brokenCore.getConnectedFortressGeneratorsNotClogged();
					for (TileEntityFortressGenerator fg : fgs) {
						fg.getGeneratorCore().clog();
					}
				}
			}
		}
	}

	public void onBurnStateChanged() {
		if (this.fortressGenerator.isBurning()) {
			if (!this.fortressGenerator.isClogged()) {
				//fortress generator was just turned on
				if (isOldestNotCloggedGeneratorConnectedTo(this)) {
					this.generateWall();
				} else {
					this.clog();
				}
			}
		} else {
			this.degenerateWall();
		}
	}

	// --------- Internal Methods ---------
	
	/**
	 * Generates (turns on) the wall touching this generator.
	 * Assumes checking for permission to generate walls is already done.
	 */
	private void generateWall() {
		//change the wall blocks touching this generator into generated blocks		
		ArrayList<Point> wallPoints = getPointsConnected(Wall.getWallBlocks(), Wall.getDisabledWallBlocks());
		for (Point p : wallPoints) {
			this.generatedPoints.add(p);
			
			//generate block
			Block blockToGenerate = world.getBlock(p.x, p.y, p.z);
			int index = Wall.getDisabledWallBlocks().indexOf(blockToGenerate);
			if (index != -1) {
				if (blockToGenerate == Blocks.wooden_door || blockToGenerate == Blocks.iron_door) {
					generateDoor(p, index);
				} else {
					world.setBlock(p.x, p.y, p.z, Wall.getEnabledWallBlocks().get(index));
				}
			}
		}
	}
	private void generateDoor(Point p, int index) {
		//assumes p is a door block
		Block block = world.getBlock(p.x, p.y, p.z);
		Block blockAbove = world.getBlock(p.x, p.y + 1, p.z);
		
		if (block == blockAbove) {
			int metaBottom = world.getBlockMetadata(p.x, p.y, p.z);
			int metaTop = world.getBlockMetadata(p.x, p.y + 1, p.z);
			
			//remove old door
			world.setBlockToAir(p.x, p.y, p.z);
			world.setBlockToAir(p.x, p.y + 1, p.z);

			//create bottom of door
			world.setBlock(p.x, p.y, p.z, Wall.getEnabledWallBlocks().get(index));
			world.setBlockMetadataWithNotify(p.x, p.y, p.z, metaBottom, 2);

			//create top of door
			world.setBlock(p.x, p.y + 1, p.z, Wall.getEnabledWallBlocks().get(index));
			world.setBlockMetadataWithNotify(p.x, p.y + 1, p.z, metaTop, 2);
		}
	}

	/**
	 * Degenerates (turns off) the wall being generated by this generator.
	 * Also degenerates wall touching this generator provided this is the oldest generator.
	 */
	private void degenerateWall() {
		//degenerate the walls it generated
		for (Point p : this.generatedPoints) {
			//degenerate block
			Block blockToDegenerate = world.getBlock(p.x, p.y, p.z);
			int index = Wall.getEnabledWallBlocks().indexOf(blockToDegenerate);
			if (index != -1) {
				if (blockToDegenerate == FortressMod.fortressWoodenDoor || blockToDegenerate == FortressMod.fortressIronDoor) {
					degenerateDoor(p, index);
				} else {
					world.setBlock(p.x, p.y, p.z, Wall.getDisabledWallBlocks().get(index));
				}
			}
		}
		this.generatedPoints.clear();
		
		//if (oldest) degenerate walls touching this generator even if this wasn't the generator that generated them
		if (isOldestNotCloggedGeneratorConnectedTo(this)) {
			degenerateConnectedWall();
		}
	}
	private void degenerateDoor(Point p, int index) {
		//assumes p is a door block
		Block block = world.getBlock(p.x, p.y, p.z);
		Block blockAbove = world.getBlock(p.x, p.y + 1, p.z);
		
		if (block == blockAbove) {
			int metaBottom = world.getBlockMetadata(p.x, p.y, p.z);
			int metaTop = world.getBlockMetadata(p.x, p.y + 1, p.z);

			//remove old door
			world.setBlockToAir(p.x, p.y, p.z);
			world.setBlockToAir(p.x, p.y + 1, p.z);

			//create bottom of door
			world.setBlock(p.x, p.y, p.z, Wall.getDisabledWallBlocks().get(index));
			world.setBlockMetadataWithNotify(p.x, p.y, p.z, metaBottom, 2);

			//create top of door
			world.setBlock(p.x, p.y + 1, p.z, Wall.getDisabledWallBlocks().get(index));
			world.setBlockMetadataWithNotify(p.x, p.y + 1, p.z, metaTop, 2);
		}
	}

	private void degenerateConnectedWall() {
		Block b;
		ArrayList<Point> wallPoints = getPointsConnected(Wall.getWallBlocks(), Wall.getEnabledWallBlocks());
		for (Point p : wallPoints) {
			b = world.getBlock(p.x, p.y, p.z);
			int index = Wall.getEnabledWallBlocks().indexOf(b);
			if (index != -1) { //if (b is a generated block)
				//assume this.generatedPoints is already empty

				//degenerate block
				world.setBlock(p.x, p.y, p.z, Wall.getDisabledWallBlocks().get(index));
			}
		}
	}

	private static boolean isOldestNotCloggedGeneratorConnectedTo(GeneratorCore core) {
		ArrayList<TileEntityFortressGenerator> fgs = core.getConnectedFortressGeneratorsNotClogged(); 
		boolean foundOlderGenerator = false;
		for (TileEntityFortressGenerator fg : fgs) {
			//if (otherFg was placed before thisFg)
			if (fg.getGeneratorCore().timePlaced < core.timePlaced) {
				//found older generator
				foundOlderGenerator = true;
				break;
			}
		}
		return !foundOlderGenerator;
	}
	
	private ArrayList<TileEntityFortressGenerator> getConnectedFortressGeneratorsNotClogged() {
		ArrayList<TileEntityFortressGenerator> matches = new ArrayList<TileEntityFortressGenerator>();
		
		ArrayList<Point> connectFgPoints = getPointsConnected(Wall.getWallBlocks(), Wall.getNotCloggedGeneratorBlocks());
		for (Point p : connectFgPoints) {
			TileEntityFortressGenerator fg = (TileEntityFortressGenerator) world.getTileEntity(p.x, p.y, p.z);
			matches.add(fg);
		}

		return matches;
	}

	/**
	 * Clogs the generator after degenerating walls.
	 * Assumes checking for permission to clog generator and degenerate walls is already done.
	 */
	void clog() {
		this.degenerateWall();
		FortressGenerator.clog(this.world, this.fortressGenerator);
	}
	
	/**
	 * Looks at all blocks connected to the generator by wallBlocks (directly or recursively).
	 * Connected means within 3x3x3.
	 * 
	 * @param wallBlocks List of connecting block types.
	 * @param returnBlocks List of block types to look for and return when connected to the wall.
	 * @return List of all points (x,y,z) with a block of a type in returnBlocks that is connected to the generator by wallBlocks.
	 */
	private ArrayList<Point> getPointsConnected(ArrayList<Block> wallBlocks, ArrayList<Block> returnBlocks) {
		int x = this.fortressGenerator.xCoord;
		int y = this.fortressGenerator.yCoord;
		int z = this.fortressGenerator.zCoord;
		Point p = new Point(x, y, z);
		return Wall.getPointsConnected(this.world, p, wallBlocks, returnBlocks);
	}

	public String getPlacedByPlayerName() {
		return this.placedByPlayerName;
	}
}
