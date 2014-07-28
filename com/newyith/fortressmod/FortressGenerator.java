package com.newyith.fortressmod;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class FortressGenerator extends BlockContainer {

    protected IIcon frontIcon;
    protected IIcon topIcon;

	protected FortressGenerator() {
		super(Material.rock);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
     * Gets the block's texture. Args: side, meta
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
    	if (side == 1 || side == 0) { //top or bottom
    		return this.topIcon;
    	} else {
    		if (side == meta) { //front
    			return this.frontIcon;
    		} else { //side
    			return this.blockIcon;
    		}
    	}
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister)
    {
    	String iconStr = BasicInfo.MODID + ":" + "fortress_generator_block";
        this.blockIcon = iconRegister.registerIcon(iconStr);
        //this.frontIcon = p_149651_1_.registerIcon(this.field_149932_b (isBurning?) ? "furnace_front_on" : "furnace_front_off");
        this.frontIcon = iconRegister.registerIcon(iconStr);
        this.topIcon = iconRegister.registerIcon(iconStr);
    }
    
}
