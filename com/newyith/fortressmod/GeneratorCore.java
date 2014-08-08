package com.newyith.fortressmod;


import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class GeneratorCore {
	private ArrayList<Point> generated = new ArrayList<Point>(); //contains currently generated blocks (points)
	public long timePlaced = 0; //TODO: change this back to private
	private TileEntityFortressGenerator fortressGenerator;

	public GeneratorCore(TileEntityFortressGenerator fortressGenerator) {
		this.fortressGenerator = fortressGenerator;
	}
	
	public void writeToNBT(NBTTagCompound compound) {
		//save list of generated blocks
		NBTTagList list = new NBTTagList();
		for (Point p : generated) {
			NBTTagCompound item = new NBTTagCompound();
			item.setInteger("x", p.x);
			item.setInteger("y", p.y);
			item.setInteger("z", p.z);
			list.appendTag(item);
		}
		compound.setTag("Generated", list);
		
		//save timePlaced
		compound.setLong("TimePlaced", this.timePlaced);
	}
	
	public void readFromNBT(NBTTagCompound compound) {
		//load list of generated blocks
		Point p;
		NBTTagList list = compound.getTagList("Generated", NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound item = list.getCompoundTagAt(i);
			int x = item.getInteger("x");
			int y = item.getInteger("y");
			int z = item.getInteger("z");
			p = new Point(x, y, z);
		}
		
		//load timePlaced
		this.timePlaced = compound.getLong("TimePlaced");
	}
	
	public static void onPlaced(World world, int x, int y, int z) {
		//clog unless its the only none clogged generator
		if (!world.isRemote) {
			TileEntityFortressGenerator placedFortressGenerator = (TileEntityFortressGenerator) world.getTileEntity(x, y, z);
			GeneratorCore placedCore = placedFortressGenerator.generatorCore;
			
			//set timePlaced
			placedCore.timePlaced = System.currentTimeMillis();
			
			//clog unless its the only none clogged generator (because placed generator must be newer than any others)
			ArrayList<TileEntityFortressGenerator> fgs = placedCore.getConnectedGeneratorsNotClogged();
			if (fgs.size() > 0) {
				placedCore.clog();
			}
		}
	}
	
	public static void onBroken(World world, int x, int y, int z) {
		//if (oldestGenerator) clog the others
		if (!world.isRemote) {
			TileEntityFortressGenerator brokenFortressGenerator = (TileEntityFortressGenerator) world.getTileEntity(x, y, z);
			GeneratorCore brokenCore = brokenFortressGenerator.generatorCore;
			
			if (isOldestNotCloggedGeneratorConnectedTo(brokenCore)) {
				ArrayList<TileEntityFortressGenerator> fgs = brokenCore.getConnectedGeneratorsNotClogged();
				for (TileEntityFortressGenerator fg : fgs) {
					fg.generatorCore.clog();
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
	
	private void generateWall() {
		//if (!oldestGenerator) clog else generate walls
		
		//TODO: write this (see FortressWallUpdater)
		sendGlobalChat("generateWall()");
	}
	
	private void degenerateWall() {
		//degenerate the walls it generated

		//TODO: write this (see FortressWallUpdater)
		sendGlobalChat("degenerateWall()");
	}

	private static boolean isOldestNotCloggedGeneratorConnectedTo(GeneratorCore core) {
		ArrayList<TileEntityFortressGenerator> fgs = core.getConnectedGeneratorsNotClogged(); 
		boolean foundNewerGenerator = false;
		for (TileEntityFortressGenerator fg : fgs) {
			if (fg.generatorCore.timePlaced > core.timePlaced) {
				//found newer generator
				foundNewerGenerator = true;
				break;
			}
		}
		return !foundNewerGenerator;
	}
	
	private ArrayList<TileEntityFortressGenerator> getConnectedGeneratorsNotClogged() {
		//TODO: write this
		return new ArrayList<TileEntityFortressGenerator>();
	}

	private void clog() {
		//TODO: write this
		sendGlobalChat("clog()");
	}
	
	private static void sendGlobalChat(String msg) {
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(msg));
	}
}
