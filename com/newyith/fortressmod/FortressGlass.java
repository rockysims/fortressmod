package com.newyith.fortressmod;

import java.util.Random;

import net.minecraft.block.BlockGlass;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class FortressGlass extends BlockGlass {
	protected FortressGlass() {
		super(Material.rock, false);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setStepSound(soundTypeGlass);
		disableStats();
	}


	/**
     * Gets the block's texture. Args: side, meta
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
    	return this.blockIcon;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister)
    {
    	String iconStr = ModInfo.MODID.toLowerCase() + ":" + "fortress_glass";
        this.blockIcon = iconRegister.registerIcon(iconStr);
    }

	/**
	 * A randomly called display update to be able to add particles or other items for display
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int xParam, int yParam, int zParam, Random random) {
		if (random.nextFloat() * 100 < 10) {
			Wall.randomDisplayTick(world, xParam, yParam, zParam, random);
		}
	}	
}
