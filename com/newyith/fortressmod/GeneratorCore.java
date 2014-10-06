package com.newyith.fortressmod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class GeneratorCore {
	//saved in NBT
	private List<List<Point>> generatedLayers = new ArrayList<List<Point>>();
	private List<List<Point>> animationWallLayers = new ArrayList<List<Point>>();
	private Set<Point> claimedPoints = new HashSet<Point>();
	private Set<Point> claimedWallPoints = new HashSet<Point>();
	private boolean isChangingGenerated;
	private boolean isGeneratingWall;
	private long timePlaced = 0;
	private String placedByPlayerName = "none"; //set in onPlaced
	
	private TileEntityFortressGenerator fortressGenerator;
	private World world;
	private boolean animateGeneration = true;
	private long lastFrameTimestamp = 0;
	private long msPerFrame = 150;
	private List<Long> generateWallTimeStamps = new ArrayList<Long>();

	public static final int generationRangeLimit = 32;
	
	public GeneratorCore(TileEntityFortressGenerator fortressGenerator) {
		this.fortressGenerator = fortressGenerator;
	}

	public void setWorldObj(World world) {
		this.world = world;
	}

	public void writeToNBT(NBTTagCompound compound) {
		writePointsToNBT(compound, "claimedPoints", this.claimedPoints);
		writePointsToNBT(compound, "claimedWallPoints", this.claimedWallPoints);
		writeLayersToNBT(compound, "animationWallLayers", this.animationWallLayers);
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
		this.claimedWallPoints = readPointsFromNBT(compound, "claimedWallPoints");
		this.animationWallLayers = readLayersFromNBT(compound, "animationWallLayers");
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
			
			//pretend redstone state just changed in case it is already powered
			placedFortressGenerator.onNeighborBlockChange(world, x, y, z);
			
			//set overlapWithClaimed = true if placed generator is connected (by faces) to another generator's claimed points
			Set<Point> alreadyClaimedPoints = placedCore.getClaimedPointsOfNearbyGenerators();
			Set<Point> layerAroundGenerator = placedCore.getTouchingFaces();
			boolean overlapWithClaimed = !Collections.disjoint(alreadyClaimedPoints, layerAroundGenerator); //disjoint means no points in common
			
			if (overlapWithClaimed) {
				placedCore.sendMessage("Fortress generator is too close to another generator's wall.");
				placedCore.clog();
			} else {
				//claim wall + 1 layer (and 1 layer around generator)
				List<List<Point>> generatableWallLayers = placedCore.getGeneratableWallLayers();
				placedCore.updateClaimedPoints(generatableWallLayers);
				int foundWallPointsCount = Wall.flattenLayers(generatableWallLayers).size();
				
				//to give a way to degenerate fortress wall if it some how gets left behind by some bug:
				//search adjacent blocks for enabled wall blocks not claimed by any generator (and if found, degenerate connected unclaimed blocks)
				Set<Point> claimedPoints = placedCore.getClaimedPointsOfNearbyGenerators();
				Set<Point> adjacentPoints = placedCore.getTouchingFaces();
				for (Point p : adjacentPoints) {
					Block b = world.getBlock(p.x, p.y, p.z);
					if (Wall.getEnabledWallBlocks().contains(b)) { //found adjacent block that is in its generated form
						if (!claimedPoints.contains(p)) { //found unclaimed generated adjacent block at p
							//degenerate
							List<List<Point>> degeneratableWallLayers = placedCore.getDegeneratableWallLayers();
							if (degeneratableWallLayers.size() > 0) { //found wall to degenerate
								//degenerate degeneratbaleWallLayers (make generator pretend it was generating degeneratbaleWallLayers and degenerate)
								placedCore.updateClaimedPoints(degeneratableWallLayers);
								foundWallPointsCount += Wall.flattenLayers(degeneratableWallLayers).size();
								placedCore.generatedLayers.addAll(degeneratableWallLayers);
								placedCore.degenerateWall(true);
							}
							
							break;
						}
					}
				}
				
				//tell player how many wall blocks were found
				placedCore.sendMessage("Fortress generator found " + String.valueOf(foundWallPointsCount) + " wall blocks.");
			}
			
			//add core to list of all cores (saved in NBT)
			ModWorldData.forWorld(world).addGeneratorCorePoint(new Point(x, y, z));
		}
	}
	
	public void sendMessage(String msg) {
		msg = EnumChatFormatting.AQUA + msg;
		Chat.sendMessageToPlayer(msg, world.getPlayerEntityByName(this.placedByPlayerName));
	}

	//Not called when broken and then replaced by different version of fortress generator (on, off, clogged)
	public static void onBroken(World world, int x, int y, int z) {
		if (!world.isRemote) {
			TileEntityFortressGenerator brokenFortressGenerator = (TileEntityFortressGenerator) world.getTileEntity(x, y, z);
			GeneratorCore brokenCore = brokenFortressGenerator.getGeneratorCore();

			//degenerate generated wall
			brokenCore.degenerateWall(false);
			
			//remove core from list of all cores (saved in NBT)
			ModWorldData.forWorld(world).removeGeneratorCorePoint(new Point(x, y, z));
		}
	}

	public void onBurnStateChanged() {
		if (this.fortressGenerator.isBurning()) {
			if (!this.fortressGenerator.isClogged() && !this.isPaused()) {
				//fortress generator was just turned on
				this.generateWall();
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
				for (int i = 0; i < this.animationWallLayers.size(); i++) {
					int layerIndex = i;
					//if (degenerating) reverse direction
					if (!this.isGeneratingWall) {
						layerIndex = (animationWallLayers.size()-1) - i;
					}
					
					List<Point> layer = new ArrayList<Point>(this.animationWallLayers.get(layerIndex)); //make copy to avoid concurrent modification errors
					
					//set allOfLayerIsGenerated and anyOfLayerIsGenerated
					boolean allOfLayerIsGenerated = true;
					boolean anyOfLayerIsGenerated = false;
					for (Point p : layer) {
						Block b = world.getBlock(p.x, p.y, p.z);
						boolean isWallBlock = Wall.getWallBlocks().contains(b);
						if (isWallBlock) {
							boolean isGeneratedBlock = Wall.getEnabledWallBlocks().contains(b);
							if (isGeneratedBlock) {
								anyOfLayerIsGenerated = true;
							} else {
								allOfLayerIsGenerated = false;
							}
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
	 * Clogs generator if called too often (more than once per second).
	 */
	private void generateWall() {
		//Dbg.print("generateWall()");
		
		//if generateWall is called too often, clog to prevent lag else generate wall
		if (this.getRecentGenerateWallCallCount() > 10) {
			this.clog();
		} else {
			generateWallTimeStamps.add(System.currentTimeMillis());
			
			//generate wall
			
			//set this.wallLayers = wall layers its allowed to generate
			this.animationWallLayers = this.getGeneratableWallLayers();
			//recalculate this.claimedPoints
			this.updateClaimedPoints(merge(this.animationWallLayers, this.generatedLayers));

			//start generating
			this.isGeneratingWall = true;
			this.isChangingGenerated = true;
		}
	}
	private int getRecentGenerateWallCallCount() {
		//set recentGenerateWallCalls and remove expired stamps
		long now = System.currentTimeMillis();
		int stampLifetimeMs = 10*1000;
		int recentGenerateWallCalls = 0;
		for (Iterator<Long> itr = generateWallTimeStamps.iterator(); itr.hasNext(); ) {
		    Long stamp = itr.next();
			if (now - stamp < stampLifetimeMs) {
				recentGenerateWallCalls++;
			} else {
				itr.remove();
			}
		}
		
		return recentGenerateWallCalls;
	}

	private void updateClaimedPoints(List<List<Point>> wallLayers) {
		this.claimedPoints.clear();
		
		this.claimedWallPoints = Wall.flattenLayers(wallLayers);
		this.claimedPoints.addAll(this.claimedWallPoints);
		
		//add layer around wall to claimed points
		Set<Point> layerAroundWallPoints = getLayerAround(this.claimedWallPoints);
		this.claimedPoints.addAll(layerAroundWallPoints);
		
		//add layer around generator
		Set<Point> layerAroundGenerator = getLayerAround(Wall.flattenLayers(this.getGeneratorPointAsLayers()));
		this.claimedPoints.addAll(layerAroundGenerator);
	}
	
	private List<List<Point>> getDegeneratableWallLayers() {
		return getAllowedWallLayers(Wall.getEnabledWallBlocks());
	}
	
	private List<List<Point>> getGeneratableWallLayers() {
		return getAllowedWallLayers(Wall.getDisabledWallBlocks());
	}

	private List<List<Point>> getAllowedWallLayers(List<Block> returnBlocks) {
		Set<Point> claimedPoints = this.getClaimedPointsOfNearbyGenerators();
				
		//return all connected wall points ignoring (and not traversing) claimedPoints (generationRangeLimit search range)
		List<List<Point>> allowedWallLayers = getPointsConnectedAsLayers(Wall.getWallBlocks(), returnBlocks, generationRangeLimit, claimedPoints);
		return allowedWallLayers;
	}

	private Set<Point> getClaimedPointsOfNearbyGenerators() {
		//claimedPoints = merge of claimedPoints of all nearby generators (nearbyCores)
		Set<Point> claimedPoints = new HashSet();
		Set<GeneratorCore> nearbyCores = this.getOtherCoresInRange(generationRangeLimit*2);
		for (GeneratorCore core : nearbyCores) {
			claimedPoints.addAll(core.getClaimedPoints());
		}
		return claimedPoints;
	}
	
	private Set<GeneratorCore> getOtherCoresInRange(int rangeLimit) {
		int x = this.x();
		int y = this.y();
		int z = this.z();
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
				} else { //ModWorldData's allGeneratorCorePoints list is wrong
					ModWorldData.forWorld(this.world).removeGeneratorCorePoint(p);
				}
			}
		}
		
		return nearbyCores;
	}
	
	private Set<Point> getClaimedPoints() {
		//update claimedPoints if claimedWallPoints are not all wall type blocks
		for (Point p : this.claimedWallPoints) {
			Block claimedWallBlock = world.getBlock(p.x, p.y, p.z);
			if (!Wall.getWallBlocks().contains(claimedWallBlock)) { //claimedWallBlock isn't a wall type block
				this.unclaimDisconnect();
				break;
			}
		}
		
		return this.claimedPoints;
	}

	private void unclaimDisconnect() {
		//fill pointsToUnclaim
		Set<Point> pointsToUnclaim = new HashSet<Point>();
		Set<Point> connectedPoints = getPointsConnected(Wall.getWallBlocks(), Wall.getWallBlocks(), generationRangeLimit, null, this.claimedWallPoints);
		for (Point claimedWallPoint : this.claimedWallPoints) {
			if (!connectedPoints.contains(claimedWallPoint)) { //found claimed wall point that is now disconnected
				//add claimedWallPoint to poitnsToUnclaim
				pointsToUnclaim.add(claimedWallPoint);
			}
		}
		
		//remove pointsToUnclaim from this.claimedWallPoints
		this.claimedWallPoints.removeAll(pointsToUnclaim);

		//update this.claimedPoints
		this.claimedPoints.clear();
		this.claimedPoints.addAll(this.claimedWallPoints);
		//add layer around wall to claimed points
		Set<Point> layerAroundWallPoints = getLayerAround(this.claimedWallPoints);
		this.claimedPoints.addAll(layerAroundWallPoints);
		//add layer around generator
		Set<Point> layerAroundGenerator = getLayerAround(Wall.flattenLayers(this.getGeneratorPointAsLayers()));
		this.claimedPoints.addAll(layerAroundGenerator); //TODO: uncomment out this line
		
		//degenerate overlap between pointsToUnclaim and this.generatedLayers
		Set<Point> pointsToDegenerate = new HashSet<Point>(pointsToUnclaim);
		pointsToDegenerate.retainAll(Wall.flattenLayers(this.generatedLayers));
		this.animationWallLayers.clear();
		this.animationWallLayers.add(new ArrayList<Point>(pointsToDegenerate));
		this.isGeneratingWall = false;
		this.isChangingGenerated = true;
		this.animateGeneration = false;
		this.updateEntity();
		this.animateGeneration = true;
		
		//remove pointsToDegenerate from this.generatedLayers
		for (Iterator<List<Point>> itr = this.generatedLayers.iterator(); itr.hasNext(); ) {
			List<Point> layer = itr.next();
			layer.removeAll(pointsToDegenerate);
			if (layer.size() == 0) {
				itr.remove();
			}
		}
		this.isGeneratingWall = this.generatedLayers.size() > 0;
	}

	/**
	 * Degenerates (turns off) the wall being generated by this generator.
	 */
	private void degenerateWall(boolean animate) {
		//Dbg.print("degenerateWall("+String.valueOf(animate)+")");
		this.animationWallLayers.clear();
		this.animationWallLayers.addAll(this.generatedLayers);
		
		this.isGeneratingWall = false;
		this.isChangingGenerated = true;
		
		if (!animate) {
			this.animateGeneration = false;
			this.updateEntity();
			this.animateGeneration = true;
		}
	}
	private static List<List<Point>> merge(List<List<Point>> layers1, List<List<Point>> layers2) {
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
		this.claimedPoints.clear();
		this.fortressGenerator.setState(FortressGeneratorState.CLOGGED);
	}

	private Set<Point> getPointsConnected(ArrayList<Block> wallBlocks, ArrayList<Block> returnBlocks) {
		return this.getPointsConnected(wallBlocks, returnBlocks, generationRangeLimit, null);
	}

	private Set<Point> getPointsConnected(ArrayList<Block> wallBlocks, ArrayList<Block> returnBlocks, int rangeLimit, Set<Point> ignorePoints) {
		return Wall.getPointsConnected(this.world, this.point(), wallBlocks, returnBlocks, rangeLimit, ignorePoints);
	}

	private Set<Point> getPointsConnected(ArrayList<Block> wallBlocks, ArrayList<Block> returnBlocks, int rangeLimit, Set<Point> ignorePoints, Set<Point> searchablePoints) {
		return Wall.getPointsConnected(this.world, this.point(), wallBlocks, returnBlocks, rangeLimit, ignorePoints, searchablePoints);
	}
	
	private List<List<Point>> getPointsConnectedAsLayers(ArrayList<Block> wallBlocks, ArrayList<Block> returnBlocks) {
		return this.getPointsConnectedAsLayers(wallBlocks, returnBlocks, generationRangeLimit, null);
	}
	
	private List<List<Point>> getPointsConnectedAsLayers(List<Block> wallBlocks, List<Block> returnBlocks, int rangeLimit, Set<Point> ignorePoints) {
		return Wall.getPointsConnectedAsLayers(this.world, this.point(), wallBlocks, returnBlocks, rangeLimit, ignorePoints);
	}

	private Set<Point> getLayerAround(Set<Point> wallPoints) {
		List<Block> wallBlocks = new ArrayList<Block>(); //no wall blocks
		List<Block> returnBlocks = null; //all blocks are return blocks
		int rangeLimit = generationRangeLimit + 1;
		Set<Point> ignorePoints = null; //no points ignored
		return Wall.getPointsConnected(this.world, this.point(), wallPoints, wallBlocks, returnBlocks, rangeLimit, ignorePoints, Wall.ConnectedThreshold.POINTS);
	}
	
	/**
	 * Gets the 6 points (blocks) touching the generator (connected by faces).
	 * 
	 * @return The 6 points touching the generator.
	 */
	private Set<Point> getTouchingFaces() {
		Point origin = this.point();
		
		Set<Point> originLayer = new HashSet<Point>();
		originLayer.add(origin);
		List<Block> wallBlocks = new ArrayList<Block>(); //no wall blocks
		List<Block> returnBlocks = null; //all blocks are return blocks
		int rangeLimit = generationRangeLimit + 1;
		Set<Point> ignorePoints = null; //no points ignored
		return Wall.getPointsConnected(this.world, origin, originLayer, wallBlocks, returnBlocks, rangeLimit, ignorePoints, Wall.ConnectedThreshold.FACES);
	}
	
	public String getPlacedByPlayerName() {
		return this.placedByPlayerName;
	}
	
	private List<List<Point>> getGeneratorPointAsLayers() {
		List<List<Point>> generatorAsLayers = new ArrayList();
		generatorAsLayers.add(this.getGeneratorPointAsLayer());
		return generatorAsLayers;
	}
	private List<Point> getGeneratorPointAsLayer() {
		Point generatorPoint = new Point(this.x(), this.y(), this.z());
		List<Point> generatorAsLayer = new ArrayList();
		generatorAsLayer.add(generatorPoint);
		return generatorAsLayer;
	}
	
	private int x() {
		return this.fortressGenerator.xCoord;
	}
	private int y() {
		return this.fortressGenerator.yCoord;
	}
	private int z() {
		return this.fortressGenerator.zCoord;
	}
	private Point point() {
		return new Point(this.x(), this.y(), this.z());
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
