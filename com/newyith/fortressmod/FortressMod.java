package com.newyith.fortressmod;

import java.util.Arrays;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

//TODO: work on fortress generator gui. see http://www.minecraftforum.net/forums/mapping-and-modding/mapping-and-modding-tutorials/1571074-1-6-2-advanced-minecraft-forge-modding-tutorial-1
//TODO: work on FortressGeneratorTileEntity class


@Mod(modid = ModInfo.MODID, name = ModInfo.NAME, version = ModInfo.VERSION)
public class FortressMod
{
    @Instance(value = ModInfo.MODID)
    public static FortressMod modInstance;
	
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
        fortressGenerator = new FortressGenerator().setBlockName("FortressGenerator").setCreativeTab(tabName);
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