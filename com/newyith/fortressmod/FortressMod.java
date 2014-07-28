package com.newyith.fortressmod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = BasicInfo.MODID, name = BasicInfo.NAME, version = BasicInfo.VERSION)
public class FortressMod
{
	public static Block yourBlock;
	public static Block testBlock;
	public static Block fortressGenerator;

	public static CreativeTabs tabName = new CreativeTabs("tabName") {
		public Item getTabIconItem() {
			return Items.arrow;
		}
	};
	
	/*
	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {}
	//*/
	
	@EventHandler
	public void init(FMLInitializationEvent e)
	{
        fortressGenerator = new FortressGenerator().setBlockName("FortressGenerator").setHardness(3.5F).setStepSound(Block.soundTypePiston).setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressGenerator, "FortressGenerator");
		
		ItemStack obsidianStack = new ItemStack(Blocks.obsidian, 1);
        ItemStack fortressGeneratorStack = new ItemStack(fortressGenerator, 1);
        GameRegistry.addRecipe(fortressGeneratorStack, "ooo", "o o", "ooo", 'o', obsidianStack);
        
        //* debug recipes
        ItemStack dirtStack = new ItemStack(Blocks.dirt, 1);
        ItemStack obsidianStack64 = new ItemStack(Blocks.obsidian, 64);
        GameRegistry.addRecipe(obsidianStack64, "   ", " d ", "   ", 'd', dirtStack);

        //ItemStack obsidianStack = new ItemStack(Blocks.obsidian, 1);
        ItemStack dirtStack64 = new ItemStack(Blocks.dirt, 64);
        GameRegistry.addRecipe(dirtStack64, "   ", " o ", "   ", 'o', obsidianStack);
        
        ItemStack lightstoneDustStack64 = new ItemStack(Items.glowstone_dust, 64);
        GameRegistry.addRecipe(lightstoneDustStack64, "d  ", " o ", "   ", 'o', obsidianStack, 'd', dirtStack);
        //*/
	}
	
	/*
	@EventHandler
	public void postInit(FMLPostInitializationEvent e){}
	//*/
	
}