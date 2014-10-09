package com.newyith.fortressmod.blocks;


import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.newyith.fortressmod.Wall;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class FortressBedrock extends Block {
	public FortressBedrock() {
		super(Material.rock);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setStepSound(soundTypePiston);
		disableStats();
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return Blocks.bedrock.getIcon(side, meta);
	}
	
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		//if we don't override this method, we get an exception
		//don't need to register any textures because we're re-using bedrock texture
	}
	
	/**
	 * A randomly called display update to be able to add particles or other items for display
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int xParam, int yParam, int zParam, Random random) {
		Wall.randomDisplayTick(world, xParam, yParam, zParam, random);
	}	
}
