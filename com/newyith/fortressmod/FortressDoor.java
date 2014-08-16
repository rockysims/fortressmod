package com.newyith.fortressmod;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.IconFlipped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class FortressDoor extends BlockDoor {

	protected FortressDoor() {
		super(Material.rock); //not Material.iron so that it will open close on right click and not wood so it can't burn
		setHardness(1.0F);
		//setBlockUnbreakable();
		//setResistance(6000000.0F);
		//disableStats();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public String getTextureName() {
		return ModInfo.MODID.toLowerCase() + ":" + "fortress_door";
    }
	
	@Override
	public void onBlockClicked(World p_149699_1_, int p_149699_2_, int p_149699_3_, int p_149699_4_, EntityPlayer p_149699_5_) {
		Dbg.print("Fortress door clicked");
	}
	
	public Item getItemDropped(int par1, Random par2, int par3) {
		return (par1 & 8) != 0 ? null : FortressMod.itemFortressDoor;
    }
}
