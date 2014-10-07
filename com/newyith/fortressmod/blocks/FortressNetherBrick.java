package com.newyith.fortressmod.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import com.newyith.fortressmod.ModInfo;
import com.newyith.fortressmod.Wall;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class FortressNetherBrick extends Block {
	public FortressNetherBrick() {
		super(Material.rock);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setStepSound(soundTypePiston);
		disableStats();
	}

	/**
	 * Gets the block's texture. Args: side, meta
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		return this.blockIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		String iconStr = ModInfo.MODID.toLowerCase() + ":" + "fortress_nether_brick";
		this.blockIcon = iconRegister.registerIcon(iconStr);
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
