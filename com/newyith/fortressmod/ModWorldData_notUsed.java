package com.newyith.fortressmod;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.util.Constants.NBT;

public class ModWorldData_notUsed extends WorldSavedData {
	final static String key = "fortressmod.world.data";

	//data to store
	private Set<Point> allGeneratorCorePoints = new HashSet<Point>();

	public ModWorldData_notUsed(String mapName) {
		super(mapName);
	}
	
	public Set<Point> getGeneratorCorePoints() {
		return this.allGeneratorCorePoints;
	}
	
	public void addGeneratorCorePoint(Point p) {
		this.allGeneratorCorePoints.add(p);
		this.setDirty(true);
	}
	
	public void removeGeneratorCorePoint(Point p) {
		this.allGeneratorCorePoints.remove(p);
		this.setDirty(true);
	}
	
	public static ModWorldData_notUsed forWorld(World world) {
		// Retrieves the MyWorldData instance for the given world, creating it if necessary
		MapStorage storage = world.perWorldStorage;
		ModWorldData_notUsed result = (ModWorldData_notUsed)storage.loadData(ModWorldData_notUsed.class, key);
		if (result == null) {
			result = new ModWorldData_notUsed(key);
			storage.setData(key, result);
		}
		return result;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		//read allGeneratorCorePoints
		NBTTagList list = compound.getTagList("allGeneratorCorePoints", NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound item = list.getCompoundTagAt(i);
			int x = item.getInteger("x");
			int y = item.getInteger("y");
			int z = item.getInteger("z");
			this.allGeneratorCorePoints.add(new Point(x, y, z));
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		//write allGeneratorCorePoints
		NBTTagList list = new NBTTagList();
		for (Point p : this.allGeneratorCorePoints) {
			NBTTagCompound item = new NBTTagCompound();
			item.setInteger("x", p.x);
			item.setInteger("y", p.y);
			item.setInteger("z", p.z);
			list.appendTag(item);
		}
		compound.setTag("allGeneratorCorePoints", list);
	}

}