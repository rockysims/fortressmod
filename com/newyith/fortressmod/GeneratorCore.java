package com.newyith.fortressmod;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import com.google.common.collect.Lists;

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
	//saved in NBT
	private List<List<Point>> generatedLayers = new ArrayList<List<Point>>();
	private List<List<Point>> wallLayers = new ArrayList<List<Point>>();
	private boolean isChangingGenerated;
	private boolean isGeneratingWall;
	private long timePlaced = 0;
	private String placedByPlayerName = "none"; //set in onPlaced
	
	private TileEntityFortressGenerator fortressGenerator;
	private World world;
	private boolean animateGeneration = true;
	private long lastFrameTimestamp = 0;
	private long msPerFrame = 150;

	public GeneratorCore(TileEntityFortressGenerator fortressGenerator) {
		this.fortressGenerator = fortressGenerator;
	}

	public void setWorldObj(World world) {
		this.world = world;
	}
	
	public void writeToNBT(NBTTagCompound compound) {
		writeLayersToNBT(compound, "wallLayers", this.wallLayers);
		writeLayersToNBT(compound, "generatedLayers", this.generatedLayers);
		
		//save the other stuff
		compound.setLong("timePlaced", this.timePlaced);
		compound.setString("placedByPlayerName", this.placedByPlayerName);
		compound.setBoolean("isChangingGenerated", this.isChangingGenerated);
		compound.setBoolean("isGeneratingWall", this.isGeneratingWall);
	}
	private void writeLayersToNBT(NBTTagCompound compound, String id, List<List<Point>> layers) {
		NBTTagList layersList = new NBTTagList();
		
		for (List<Point> layer : layers) {
			//fill list
			NBTTagList list = new NBTTagList();
			for (Point p : layer) {
				NBTTagCompound item = new NBTTagCompound();
				item.setInteger("x", p.x);
				item.setInteger("y", p.y);
				item.setInteger("z", p.z);
				list.appendTag(item);
			}

			//add list to layersList (wrapped in compound)
			NBTTagCompound listCompound = new NBTTagCompound();
			listCompound.setTag("list", list);
			layersList.appendTag(listCompound);
		}
		
		compound.setTag(id, layersList);
	}
	
	public void readFromNBT(NBTTagCompound compound) {
		this.wallLayers = readLayersFromNBT(compound, "wallLayers");
		this.generatedLayers = readLayersFromNBT(compound, "generatedLayers");
		
		//load the other stuff
		this.timePlaced = compound.getLong("timePlaced");
		this.placedByPlayerName = compound.getString("placedByPlayerName");
		this.isChangingGenerated = compound.getBoolean("isChangingGenerated");
		this.isGeneratingWall = compound.getBoolean("isGeneratingWall");
	}
	
	private List<List<Point>> readLayersFromNBT(NBTTagCompound compound, String id) {
		NBTTagList layersList = compound.getTagList(id, NBT.TAG_COMPOUND);
		
		//fill layersRead
		List<List<Point>> layersRead = new ArrayList<List<Point>>();
		for (int layerIndex = 0; layerIndex < layersList.tagCount(); layerIndex++) {
			//get list from layersList
			NBTTagCompound listCompound = layersList.getCompoundTagAt(layerIndex);
			NBTTagList list = listCompound.getTagList("list", NBT.TAG_COMPOUND);
			
			//fill layerRead
			List<Point> layerRead = new ArrayList<Point>();
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound item = list.getCompoundTagAt(i);
				int x = item.getInteger("x");
				int y = item.getInteger("y");
				int z = item.getInteger("z");
				layerRead.add(new Point(x, y, z));
			}
			layersRead.add(layerRead);
		}
		
		return layersRead;
	}

	public static void onPlaced(World world, int x, int y, int z, String placingPlayerName) {
		//clog unless its the only none clogged generator (in which case degenerated connected wall)
		if (!world.isRemote) {
			TileEntityFortressGenerator placedFortressGenerator = (TileEntityFortressGenerator) world.getTileEntity(x, y, z);
			GeneratorCore placedCore = placedFortressGenerator.getGeneratorCore();
			
			//set timePlaced
			placedCore.timePlaced = System.currentTimeMillis();
			placedCore.placedByPlayerName = placingPlayerName;
			
			//pretend redstone state just changed in case it is already powered
			placedFortressGenerator.onNeighborBlockChange(world, x, y, z);
			
			//clog or degenerate
			boolean isOldest = isOldestNotCloggedGeneratorConnectedTo(placedCore);
			if (!isOldest) {
				placedCore.clog();
			} else {
				placedCore.degenerateWall(true);
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
				brokenCore.degenerateWall(false);

				//if (oldestGenerator) clog the others
				if (isOldestNotCloggedGeneratorConnectedTo(brokenCore)) {
					List<TileEntityFortressGenerator> fgs = brokenCore.getConnectedFortressGeneratorsNotClogged();
					for (TileEntityFortressGenerator fg : fgs) {
						fg.getGeneratorCore().clog();
					}
				}
			}
		}
	}

	public void onBurnStateChanged() {
		if (this.fortressGenerator.isBurning()) {
			if (!this.fortressGenerator.isClogged() && !this.isPaused()) {
				//fortress generator was just turned on
				if (isOldestNotCloggedGeneratorConnectedTo(this)) {
					this.generateWall();
				} else {
					this.clog();
				}
			}
		} else {
			this.degenerateWall(true);
		}
	}

	public void onPoweredMightHaveChanged() {
		if (this.isGeneratingWall && this.isPaused()) {
			//just turned on redstone power so degenerate wall
			this.degenerateWall(true);
		}
		
		if (!this.isGeneratingWall && this.isActive()) {
			//just turned off redstone power so try to start generating again
			this.onBurnStateChanged();
		}
	}

	public void updateEntity() {
		if (this.isChangingGenerated) {
			long now = (new Date()).getTime();
			//if (ready to update to next frame)
			if (!this.animateGeneration  || now - this.lastFrameTimestamp > this.msPerFrame ) {
				this.lastFrameTimestamp  = now;
				boolean updateLayer = false;

				//update to next frame
				for (int i = 0; i < this.wallLayers.size(); i++) {
					int layerIndex = i;
					//if (degenerating) reverse direction
					if (!this.isGeneratingWall) {
						layerIndex = (wallLayers.size()-1) - i;
					}
					
					List<Point> layer = new ArrayList<Point>(this.wallLayers.get(layerIndex)); //make copy to avoid concurrent modification errors
					//TODO: consider not making copy of wall layer on line above since we really shouldn't need to
					
					//set allOfLayerIsGenerated and anyOfLayerIsGenerated
					boolean allOfLayerIsGenerated = true;
					boolean anyOfLayerIsGenerated = false;
					for (Point p : layer) {
						boolean isGeneratedBlock = Wall.getEnabledWallBlocks().contains(world.getBlock(p.x, p.y, p.z));
						if (isGeneratedBlock) {
							anyOfLayerIsGenerated = true;
						} else {
							allOfLayerIsGenerated = false;
						}
					}

					//set updateLayer
					updateLayer = false;
					if (this.isGeneratingWall && !allOfLayerIsGenerated) {
						updateLayer = true;
					}
					if (!this.isGeneratingWall && anyOfLayerIsGenerated) {
						updateLayer = true;
					}
					
					//update layer if needed
					if (updateLayer) {
						for (Point p : layer) {
							Block wallBlock = world.getBlock(p.x, p.y, p.z);
							
							if (this.isGeneratingWall) {
								//generate wallBlock
								int index = Wall.getDisabledWallBlocks().indexOf(wallBlock);
								if (index != -1) {
									while (layerIndex >= this.generatedLayers.size()) {
										this.generatedLayers.add(new ArrayList<Point>());
									}
									this.generatedLayers.get(layerIndex).add(p);
									
									if (wallBlock == Blocks.wooden_door || wallBlock == Blocks.iron_door) {
										generateDoor(p, index);
									} else {
										world.setBlock(p.x, p.y, p.z, Wall.getEnabledWallBlocks().get(index));
									}
								}
							} else {
								//degenerate wallBlock
								int index = Wall.getEnabledWallBlocks().indexOf(wallBlock);
								if (index != -1) {
									if (layerIndex < this.generatedLayers.size()) {
										this.generatedLayers.get(layerIndex).remove(p);
									} //else we are degenerating another generators wall
									
									if (wallBlock == FortressMod.fortressWoodenDoor || wallBlock == FortressMod.fortressIronDoor) {
										degenerateDoor(p, index);
									} else {
										world.setBlock(p.x, p.y, p.z, Wall.getDisabledWallBlocks().get(index));
									}
								}
							}
						} // end for (Point p : layer)
						
						if (this.animateGeneration && updateLayer) {
							//updated a layer so we're done with this frame
							break;
						}
					} // end if (updateLayer)
				} // end for (List<Point> layer : this.wallPoints)
				
				//if (there was no next frame) we are done
				if (!updateLayer) {
					this.isChangingGenerated = false;
				}
				//if (not animating) we finished all at once
				if (!this.animateGeneration) {
					this.isChangingGenerated = false;
				}
			}
		}
	}
	
	// --------- Internal Methods ---------

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
	
	/**
	 * Generates (turns on) the wall touching this generator.
	 * Assumes checking for permission to generate walls is already done.
	 */
	private void generateWall() {
		//*
		this.wallLayers = getPointsConnectedAsLayers(Wall.getWallBlocks(), Wall.getDisabledWallBlocks());
		this.isGeneratingWall = true;
		this.isChangingGenerated = true;
		/*/
		//change the wall blocks touching this generator into generated blocks		
		ArrayList<Point> wallPoints = getPointsConnected(Wall.getWallBlocks(), Wall.getDisabledWallBlocks());
		for (Point p : wallPoints) {
			this.generatedLayers.add(p);
			
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
		//*/
	}

	/**
	 * Degenerates (turns off) the wall being generated by this generator.
	 * Also degenerates wall touching this generator provided this is the oldest generator.
	 */
	private void degenerateWall(boolean animate) {
		//*
		this.wallLayers.clear();
		this.wallLayers.addAll(this.generatedLayers);
		if (isOldestNotCloggedGeneratorConnectedTo(this)) {
			List<List<Point>> connectedPoints = getPointsConnectedAsLayers(Wall.getWallBlocks(), Wall.getEnabledWallBlocks());
			this.wallLayers = merge(this.wallLayers, connectedPoints);
		}
		
		this.isGeneratingWall = false;
		this.isChangingGenerated = true;
		
		if (!animate) {
			this.animateGeneration = false;
			this.updateEntity();
			this.animateGeneration = true;
		}
		/*/
		//degenerate the walls it generated
		for (Point p : this.generatedLayers) {
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
		this.generatedLayers.clear();
		
		//if (oldest) degenerate walls touching this generator even if this wasn't the generator that generated them
		if (isOldestNotCloggedGeneratorConnectedTo(this)) {
			degenerateConnectedWall();
		}
		//*/
	}

	private List<List<Point>> merge(List<List<Point>> layers1, List<List<Point>> layers2) {
		List<List<Point>> layers = new ArrayList<List<Point>>();
		
		int biggestSize = Math.max(layers1.size(), layers2.size());
		for (int i = 0; i < biggestSize; i++) {
			//process layer i
			layers.add(new ArrayList<Point>());
			if (i < layers1.size()) {
				layers.get(i).addAll(layers1.get(i));
			}
			if (i < layers2.size()) {
				layers.get(i).addAll(layers2.get(i));
			}
		}
		
		return layers;
	}

	private void degenerateConnectedWall() {
		Block b;
		List<Point> wallPoints = getPointsConnected(Wall.getWallBlocks(), Wall.getEnabledWallBlocks());
		for (Point p : wallPoints) {
			b = world.getBlock(p.x, p.y, p.z);
			int index = Wall.getEnabledWallBlocks().indexOf(b);
			if (index != -1) { //if (b is a generated block)
				//assume this.generatedLayers is already empty

				//degenerate block
				world.setBlock(p.x, p.y, p.z, Wall.getDisabledWallBlocks().get(index));
			}
		}
	}

	private static boolean isOldestNotCloggedGeneratorConnectedTo(GeneratorCore core) {
		List<TileEntityFortressGenerator> fgs = core.getConnectedFortressGeneratorsNotClogged(); 
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
	
	private List<TileEntityFortressGenerator> getConnectedFortressGeneratorsNotClogged() {
		List<TileEntityFortressGenerator> matches = new ArrayList<TileEntityFortressGenerator>();
		
		List<Point> connectFgPoints = getPointsConnected(Wall.getWallBlocks(), Wall.getNotCloggedGeneratorBlocks());
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
		this.degenerateWall(true);
		this.fortressGenerator.setState(FortressGeneratorState.CLOGGED);
	}
	
	/**
	 * Looks at all blocks connected to the generator by wallBlocks (directly or recursively).
	 * Connected means within 3x3x3.
	 * 
	 * @param wallBlocks List of connecting block types.
	 * @param returnBlocks List of block types to look for and return when connected to the wall.
	 * @return List of all points (x,y,z) with a block of a type in returnBlocks that is connected to the generator by wallBlocks.
	 */
	private List<Point> getPointsConnected(ArrayList<Block> wallBlocks, ArrayList<Block> returnBlocks) {
		int x = this.fortressGenerator.xCoord;
		int y = this.fortressGenerator.yCoord;
		int z = this.fortressGenerator.zCoord;
		Point p = new Point(x, y, z);
		return Wall.getPointsConnected(this.world, p, wallBlocks, returnBlocks);
	}

	private List<List<Point>> getPointsConnectedAsLayers(ArrayList<Block> wallBlocks, ArrayList<Block> returnBlocks) {
		int x = this.fortressGenerator.xCoord;
		int y = this.fortressGenerator.yCoord;
		int z = this.fortressGenerator.zCoord;
		Point p = new Point(x, y, z);
		return Wall.getPointsConnectedAsLayers(this.world, p, wallBlocks, returnBlocks);
	}

	public String getPlacedByPlayerName() {
		return this.placedByPlayerName;
	}

	private boolean isActive() {
		return this.fortressGenerator.isActive();
	}

	public boolean isPaused() {
		return this.fortressGenerator.isPaused();
	}

	private boolean isClogged() {
		return this.fortressGenerator.isClogged();
	}
}
