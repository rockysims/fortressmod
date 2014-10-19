package com.newyith.fortressmod.blocks;

import com.newyith.fortressmod.FortressMod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;

public class FortressStairs extends BlockStairs {
	public FortressStairs(Block b) {
		super(b, 0);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		disableStats();
	}	
}