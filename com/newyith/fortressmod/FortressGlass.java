package com.newyith.fortressmod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
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

}
