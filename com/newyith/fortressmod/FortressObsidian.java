package com.newyith.fortressmod;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class FortressObsidian extends BlockObsidian {
	public FortressObsidian() {
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
    public void registerBlockIcons(IIconRegister iconRegister)
    {
    	String iconStr = ModInfo.MODID.toLowerCase() + ":" + "fortress_obsidian";
        this.blockIcon = iconRegister.registerIcon(iconStr);
    }
}
