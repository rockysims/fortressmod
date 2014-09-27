package com.newyith.fortressmod;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class ModWorldData extends WorldSavedData {
	final static String key = "fortressmod.world.data";

	// Fields containing your data here
	public String testStr = "testStr original value";

	public ModWorldData(String mapName) {
		super(mapName);
		Dbg.print("ModWorldData(String) constructor called. mapName == " + mapName);
	}
	
	public ModWorldData(World world) {
		super(key);
		Dbg.print("key: " + String.valueOf(key));
	}
	
	@Override
	public boolean isDirty() {
		//TODO: keep track of if its actually dirty?
		return true;
	}
	
	public static ModWorldData forWorld(World world) {
		// Retrieves the MyWorldData instance for the given world, creating it if necessary
		MapStorage storage = world.perWorldStorage;
		ModWorldData result = (ModWorldData)storage.loadData(ModWorldData.class, key);
		if (result == null) {
			result = new ModWorldData(world);
			Dbg.print("storage.setData()");
			storage.setData(key, result);
		}
		return result;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		// Get your data from the nbt here
		this.testStr = nbt.getString("testStr");
		Dbg.print("readFromNBT testStr == " + testStr);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		// Put your data in the nbt here
		Dbg.print("writeToNBT testStr == " + this.testStr);
		nbt.setString("testStr", this.testStr);
	}

}