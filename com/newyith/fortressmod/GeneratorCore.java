package com.newyith.fortressmod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class GeneratorCore {
	//saved in NBT
	private List<List<Point>> generatedLayers = new ArrayList<List<Point>>();
	private List<List<Point>> wallLayers = new ArrayList<List<Point>>();
	private Set<Point> claimedPoints = new HashSet<Point>();
	private boolean isChangingGenerated;
	private boolean isGeneratingWall;
	private long timePlaced = 0;
	private String placedByPlayerName = "none"; //set in onPlaced
	
	private TileEntityFortressGenerator fortressGenerator;
	private World world;
	private boolean animateGeneration = true;
	private long lastFrameTimestamp = 0;
	private long msPerFrame = 150;

	public static final int generationRangeLimit = 24; //TODO: change this back to 64
	
	public GeneratorCore(TileEntityFortressGenerator fortressGenerator) {
		this.fortressGenerator = fortressGenerator;
	}

	public void setWorldObj(World world) {
		this.world = world;
	}

	public void writeToNBT(NBTTagCompound compound) {
		writePointsToNBT(compound, "claimedPoints", this.claimedPoints);
		writeLayersToNBT(compound, "wallLayers", this.wallLayers);
		writeLayersToNBT(compound, "generatedLayers", this.generatedLayers);
		
		//save the other stuff
		compound.setLong("timePlaced", this.timePlaced);
		compound.setString("placedByPlayerName", this.placedByPlayerName);
		compound.setBoolean("isChangingGenerated", this.isChangingGenerated);
		compound.setBoolean("isGeneratingWall", this.isGeneratingWall);
	}
	private void writePointsToNBT(NBTTagCompound compound, String id, Set<Point> points) {
		NBTTagList list = new NBTTagList();
		for (Point p : points) {
			NBTTagCompound item = new NBTTagCompound();
			item.setInteger("x", p.x);
			item.setInteger("y", p.y);
			item.setInteger("z", p.z);
			list.appendTag(item);
		}
		compound.setTag(id, list);
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
		this.claimedPoints = readPointsFromNBT(compound, "claimedPoints");
		this.wallLayers = readLayersFromNBT(compound, "wallLayers");
		this.generatedLayers = readLayersFromNBT(compound, "generatedLayers");
		
		//load the other stuff
		this.timePlaced = compound.getLong("timePlaced");
		this.placedByPlayerName = compound.getString("placedByPlayerName");
		this.isChangingGenerated = compound.getBoolean("isChangingGenerated");
		this.isGeneratingWall = compound.getBoolean("isGeneratingWall");
	}
	private Set<Point> readPointsFromNBT(NBTTagCompound compound, String id) {
		Set<Point> points = new HashSet<Point>();
		
		NBTTagList list = compound.getTagList(id, NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound item = list.getCompoundTagAt(i);
			int x = item.getInteger("x");
			int y = item.getInteger("y");
			int z = item.getInteger("z");
			points.add(new Point(x, y, z));
		}
		
		return points;
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
		if (!world.isRemote) {
			TileEntityFortressGenerator placedFortressGenerator = (TileEntityFortressGenerator) world.getTileEntity(x, y, z);
			GeneratorCore placedCore = placedFortressGenerator.getGeneratorCore();
			
			//set timePlaced
			placedCore.timePlaced = System.currentTimeMillis();
			placedCore.placedByPlayerName = placingPlayerName;
			
			//TODO: delete next 2 lines? double check
			//pretend redstone state just changed in case it is already powered
			placedFortressGenerator.onNeighborBlockChange(world, x, y, z);
			
			//*
			placedCore.degenerateWall(false); //degenerates connected wall if this is the only connected generator
			/*/
			//clog or degenerate
			boolean isOldest = isOldestNotCloggedGeneratorConnectedTo(placedCore);
			if (!isOldest) {
				placedCore.clog();
			} else {
				placedCore.degenerateWall(false);
			}
			//*/
			
			//claim wall + 1 layer
			placedCore.updateClaimedPoints(placedCore.getGeneratableWallLayers());
			
			//add core to list of all cores (saved in NBT)
			ModWorldData.forWorld(world).addGeneratorCorePoint(new Point(x, y, z));
		}
	}

	//Not called when broken and then replaced by different version of fortress generator (on, off, clogged)
	public static void onBroken(World world, int x, int y, int z) {
		if (!world.isRemote) {
			TileEntityFortressGenerator brokenFortressGenerator = (TileEntityFortressGenerator) world.getTileEntity(x, y, z);
			GeneratorCore brokenCore = brokenFortressGenerator.getGeneratorCore();

			//degenerate generated wall (and connected wall if only generator connected and !clogged) without animation
			brokenCore.degenerateWall(false);
			
			/*
			//if (!clogged && oldest)
			if (!brokenFortressGenerator.isClogged() && isOldestNotCloggedGeneratorConnectedTo(brokenCore)) {
				List<TileEntityFortressGenerator> fgs = brokenCore.getConnectedFortressGeneratorsNotClogged();
				for (TileEntityFortressGenerator fg : fgs) {
					fg.getGeneratorCore().clog();
				}
			}
			//*/
			
			//remove core from list of all cores (saved in NBT)
			ModWorldData.forWorld(world).removeGeneratorCorePoint(new Point(x, y, z));
		}
	}

	public void onBurnStateChanged() {
		if (this.fortressGenerator.isBurning()) {
			if (!this.fortressGenerator.isClogged() && !this.isPaused()) {
				//fortress generator was just turned on
				//*
				this.generateWall();
				/*/
				if (isOldestNotCloggedGeneratorConnectedTo(this)) {
					this.generateWall();
				} else {
					this.clog();
				}
				//*/
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
					
//					String s = "";
//					s += "GeneratorCore::updateEntity() updateLayer == " + String.valueOf(updateLayer);
//					s += String.valueOf(layerIndex);
//					Dbg.print(s, this.world.isRemote);

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
		//assumes p is a door block (2 block tall doors)
		Point top = getDoorTop(p);
		if (top != null) {
			Point bottom = new Point(top.x, top.y - 1, top.z);
			Block topBlock = world.getBlock(top.x, top.y, top.z);
			Block bottomBlock = world.getBlock(bottom.x, bottom.y, bottom.z);
		
			int topMeta = world.getBlockMetadata(top.x, top.y, top.z);
			int bottomMeta = world.getBlockMetadata(bottom.x, bottom.y, bottom.z);

			//remove old door
			world.setBlockToAir(bottom.x, bottom.y, bottom.z);
			world.setBlockToAir(top.x, top.y, top.z);

			//create bottom of door
			world.setBlock(bottom.x, bottom.y, bottom.z, Wall.getEnabledWallBlocks().get(index));
			world.setBlockMetadataWithNotify(bottom.x, bottom.y, bottom.z, bottomMeta, 2);

			//create top of door
			world.setBlock(top.x, top.y, top.z, Wall.getEnabledWallBlocks().get(index));
			world.setBlockMetadataWithNotify(top.x, top.y, top.z, topMeta, 2);
		} else { //can't find a matching (door) block above or below (should never happen if door is working correctly)
			int meta = world.getBlockMetadata(p.x, p.y, p.z);
			world.setBlockToAir(p.x, p.y, p.z);
			world.setBlock(p.x, p.y, p.z, Wall.getEnabledWallBlocks().get(index));
			world.setBlockMetadataWithNotify(p.x, p.y, p.z, meta, 2);
		}
	}
	
	private void degenerateDoor(Point p, int index) {
		//assumes p is a door block
		Point top = getDoorTop(p);
		if (top != null) {
			Point bottom = new Point(top.x, top.y - 1, top.z);
			Block topBlock = world.getBlock(top.x, top.y, top.z);
			Block bottomBlock = world.getBlock(bottom.x, bottom.y, bottom.z);
			
			if (topBlock == bottomBlock) {
				int topMeta = world.getBlockMetadata(top.x, top.y, top.z);
				int bottomMeta = world.getBlockMetadata(bottom.x, bottom.y, bottom.z);

				//remove old door
				world.setBlockToAir(bottom.x, bottom.y, bottom.z);
				world.setBlockToAir(top.x, top.y, top.z);

				//create bottom of door
				world.setBlock(bottom.x, bottom.y, bottom.z, Wall.getDisabledWallBlocks().get(index));
				world.setBlockMetadataWithNotify(bottom.x, bottom.y, bottom.z, bottomMeta, 2);

				//create top of door
				world.setBlock(top.x, top.y, top.z, Wall.getDisabledWallBlocks().get(index));
				world.setBlockMetadataWithNotify(top.x, top.y, top.z, topMeta, 2);
			} //else the world is in an invalid state?
		} else { //can't find a matching (door) block above or below (should never happen if door is working correctly)
			int meta = world.getBlockMetadata(p.x, p.y, p.z);
			world.setBlockToAir(p.x, p.y, p.z);
			world.setBlock(p.x, p.y, p.z, Wall.getEnabledWallBlocks().get(index));
			world.setBlockMetadataWithNotify(p.x, p.y, p.z, meta, 2);
		}
	}
	
	private Point getDoorTop(Point p) {
		//assumes p is a door block
		Point a = new Point(p.x, p.y + 1, p.z);
		Point b = new Point(p.x, p.y - 1, p.z);
		Block above = world.getBlock(a.x, a.y, a.z);
		Block below = world.getBlock(b.x, b.y, b.z);
		Block middle = world.getBlock(p.x, p.y, p.z);
		
		if (above == middle) {
			return a;
		} else if (below == middle) {
			return p;
		} else {
			return null;
		}
	}

	/**
	 * Generates (turns on) the wall touching this generator.
	 * Assumes checking for permission to generate walls is already done.
	 */
	private void generateWall() {
		//Dbg.print("generateWall()");
		
		//set this.wallLayers = wall layers its allowed to generate
		this.wallLayers = this.getGeneratableWallLayers();
		//recalculate this.claimedPoints
		this.updateClaimedPoints(this.wallLayers);
		

		Dbg.print("claimedPoints.size(): " + String.valueOf(this.claimedPoints.size())); 
		
		//generate
		this.isGeneratingWall = true;
		this.isChangingGenerated = true;
	}
	
	private void updateClaimedPoints(List<List<Point>> wallLayers) {
		this.claimedPoints .clear();
		
		Set<Point> wallPoints = Wall.flattenLayers(wallLayers);
		this.claimedPoints.addAll(wallPoints);
		
		//add layer around wall to claimed points
		Set<Point> layerAroundWallPoints = getLayerAround(wallPoints);
		this.claimedPoints.addAll(layerAroundWallPoints);
		
		Dbg.print("from wallLayers.size(): " + String.valueOf(wallLayers.size())); 
	}
	
	private List<List<Point>> getGeneratableWallLayers() {
		//claimedPoints = merge of claimedPoints of all nearby generators (nearbyCores)
		Set<Point> claimedPoints = new HashSet();
		Set<GeneratorCore> nearbyCores = this.getOtherCoresInRange(generationRangeLimit*2);
		Dbg.print("getGeneratableWallLayers(): nearbyCores.size(): " + String.valueOf(nearbyCores.size()));
		for (GeneratorCore core : nearbyCores) {
			claimedPoints.addAll(core.getClaimedPoints());
		}
		
		Dbg.print("getGeneratableWallLayers(): claimedPoints.size(): " + String.valueOf(claimedPoints.size()));
				
		//return all connected wall points ignoring (and not traversing) claimedPoints (generationRangeLimit search range) (returns only disabled wall)
		List<List<Point>> allowedWallLayers = getPointsConnectedAsLayers(Wall.getWallBlocks(), Wall.getDisabledWallBlocks(), generationRangeLimit, claimedPoints);
		return allowedWallLayers;
	}

	private Set<GeneratorCore> getOtherCoresInRange(int rangeLimit) {
		int x = this.fortressGenerator.xCoord;
		int y = this.fortressGenerator.yCoord;
		int z = this.fortressGenerator.zCoord;
		Set<Point> allCorePoints = new HashSet<Point>(ModWorldData.forWorld(this.world).getGeneratorCorePoints());
		allCorePoints.remove(new Point(x, y, z)); //ignore this generator
		
		//fill nearbyCores
		Set<GeneratorCore> nearbyCores = new HashSet<GeneratorCore>();
		for (Point p : allCorePoints) {
			boolean inRange = true;
			inRange = inRange && Math.abs(p.x - x) <= rangeLimit;
			inRange = inRange && Math.abs(p.y - y) <= rangeLimit;
			inRange = inRange && Math.abs(p.z - z) <= rangeLimit;
			if (inRange) { //generator at p is in range
				//add core to nearbyCores
				TileEntity tile = this.world.getTileEntity(p.x, p.y, p.z);
				if (tile instanceof TileEntityFortressGenerator) {
					TileEntityFortressGenerator fg = (TileEntityFortressGenerator)tile;
					nearbyCores.add(fg.getGeneratorCore());
				} else { //ModWorldData's allGeneratorCorePoints list is wrong?
					ModWorldData.forWorld(this.world).removeGeneratorCorePoint(p);
					Chat.sendGlobal("Error: getCoresInRange() allCorePoints had non core point.");
				}
			}
		}
		
		return nearbyCores;
	}

	private Set<Point> getClaimedPoints() {
		return this.claimedPoints;
	}

	/**
	 * Degenerates (turns off) the wall being generated by this generator.
	 * Also degenerates wall touching this generator provided this is the oldest generator.
	 */
	private void degenerateWall(boolean animate) {
		//Dbg.print("degenerateWall("+String.valueOf(animate)+")");
		this.wallLayers.clear();
		this.wallLayers.addAll(this.generatedLayers);
		if (!this.isClogged() && this.isOnlyGeneratorConnected()) {
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
	
	private boolean isOnlyGeneratorConnected() {
		Set<Point> connectedFgPoints = getPointsConnected(Wall.getWallBlocks(), Wall.getNotCloggedGeneratorBlocks(), generationRangeLimit*2, null);
		return connectedFgPoints.size() == 0;
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
		
		Set<Point> connectFgPoints = getPointsConnected(Wall.getWallBlocks(), Wall.getNotCloggedGeneratorBlocks());
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

	private Set<Point> getPointsConnected(ArrayList<Block> wallBlocks, ArrayList<Block> returnBlocks) {
		return this.getPointsConnected(wallBlocks, returnBlocks, generationRangeLimit, null);
	}
	
	private Set<Point> getPointsConnected(ArrayList<Block> wallBlocks, ArrayList<Block> returnBlocks, int rangeLimit, Set<Point> ignorePoints) {
		int x = this.fortressGenerator.xCoord;
		int y = this.fortressGenerator.yCoord;
		int z = this.fortressGenerator.zCoord;
		Point p = new Point(x, y, z);
		return Wall.getPointsConnected(this.world, p, wallBlocks, returnBlocks, rangeLimit, ignorePoints);
	}

	private List<List<Point>> getPointsConnectedAsLayers(ArrayList<Block> wallBlocks, ArrayList<Block> returnBlocks) {
		return this.getPointsConnectedAsLayers(wallBlocks, returnBlocks, generationRangeLimit, null);
	}
	
	private List<List<Point>> getPointsConnectedAsLayers(ArrayList<Block> wallBlocks, ArrayList<Block> returnBlocks, int rangeLimit, Set<Point> ignorePoints) {
		int x = this.fortressGenerator.xCoord;
		int y = this.fortressGenerator.yCoord;
		int z = this.fortressGenerator.zCoord;
		Point p = new Point(x, y, z);
		return Wall.getPointsConnectedAsLayers(this.world, p, wallBlocks, returnBlocks, rangeLimit, ignorePoints);
	}

	private Set<Point> getLayerAround(Set<Point> wallPoints) {
		int x = this.fortressGenerator.xCoord;
		int y = this.fortressGenerator.yCoord;
		int z = this.fortressGenerator.zCoord;
		Point p = new Point(x, y, z);
		
		List<Block> wallBlocks = new ArrayList<Block>(); //no wall blocks
		List<Block> returnBlocks = null; //all blocks are return blocks
		int rangeLimit = generationRangeLimit + 1;
		Set<Point> ignorePoints = null; //no points ignored
		return Wall.getPointsConnected(this.world, p, wallPoints, wallBlocks, returnBlocks, rangeLimit, ignorePoints);
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
