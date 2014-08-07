package com.newyith.fortressmod;

import net.minecraft.world.World;

public class GeneratorCore {
	private TileEntityFortressGenerator tileentity;

	public GeneratorCore(TileEntityFortressGenerator tileentity) {
		this.tileentity = tileentity;
	}

	public void generate() {
		//if (!oldestGenerator) clog else generate walls
		
	}
	
	public void degenerate() {
		//degenerate the walls it generated
		
	}
	
	public static void onPlaced(World world, int x, int y, int z) {
		//clog unless its the only none clogged generator
		
	}
	
	public static void onBroken(World world, int x, int y, int z) {
		//if (oldestGenerator) clog the others
		
	}
}
