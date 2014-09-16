package com.newyith.fortressmod;

import java.util.Arrays;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.newyith.fortressmod.client.GuiHandler;
import com.newyith.fortressmod.items.ItemFortressDoor;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = ModInfo.MODID, name = ModInfo.NAME, version = ModInfo.VERSION)
public class FortressMod
{
    @Instance(value = ModInfo.MODID)
    public static FortressMod modInstance;
	
	public static Block fortressGenerator;
	public static Block fortressGeneratorOn;
	public static Block fortressGeneratorPaused;
	public static Block fortressGeneratorClogged;
	public static Block fortressEmergencyKey;
	public static Block fortressBedrock;
	public static Block fortressGlass;
	public static Block fortressObsidian;
	//fortress door
	public static Block fortressWoodenDoor;
	public static Block fortressIronDoor;
	public static Item itemFortressWoodenDoor;
	public static Item itemFortressIronDoor;

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
	public void init(FMLInitializationEvent e) {
		
		//Fortress Wall Blocks
		
		fortressBedrock = new FortressBedrock().setBlockName("FortressBedrock").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressBedrock, "FortressBedrock");

		fortressGlass = new FortressGlass().setBlockName("FortressGlass").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressGlass, "FortressGlass");

		fortressObsidian = new FortressObsidian().setBlockName("FortressObsidian").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressObsidian, "FortressObsidian");

		//fortress wooden door block
		fortressWoodenDoor = new FortressDoor(Material.wood).setBlockName("FortressWoodenDoor");
		GameRegistry.registerBlock(fortressWoodenDoor, "FortressWoodenDoor");
		//fortress wooden door item
		itemFortressWoodenDoor = new ItemFortressDoor(Material.wood).setCreativeTab(tabName);
		GameRegistry.registerItem(itemFortressWoodenDoor, "ItemFortressWoodenDoor");

		//fortress iron door block
		fortressIronDoor = new FortressDoor(Material.iron).setBlockName("FortressIronDoor");
		GameRegistry.registerBlock(fortressIronDoor, "FortressIronDoor");
		//fortress iron door item
		itemFortressIronDoor = new ItemFortressDoor(Material.iron).setCreativeTab(tabName);
		GameRegistry.registerItem(itemFortressIronDoor, "ItemFortressIronDoor");
		
		//Fortress Generators

        fortressGenerator = new FortressGenerator(FortressGeneratorState.OFF).setBlockName("FortressGenerator").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressGenerator, "FortressGenerator");
		
		fortressGeneratorOn = new FortressGenerator(FortressGeneratorState.ACTIVE).setBlockName("FortressGeneratorActive");
		GameRegistry.registerBlock(fortressGeneratorOn, "FortressGeneratorActive");
		
		fortressGeneratorPaused = new FortressGenerator(FortressGeneratorState.PAUSED).setBlockName("FortressGeneratorOnAndPowered");
		GameRegistry.registerBlock(fortressGeneratorPaused, "FortressGeneratorOnAndPowered");
		
        fortressGeneratorClogged = new FortressGenerator(FortressGeneratorState.CLOGGED).setBlockName("FortressGeneratorClogged").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressGeneratorClogged, "FortressGeneratorClogged");
		
		//Fortress Generator Emergency Key
		fortressEmergencyKey = new FortressEmergencyKey().setBlockName("FortressEmergencyKey").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressEmergencyKey, "FortressEmergencyKey");
		
		//Recipes
		
		ItemStack obsidianStack = new ItemStack(Blocks.obsidian, 1);
		ItemStack diamondStack = new ItemStack(Items.diamond, 1);
        ItemStack fortressGeneratorStack = new ItemStack(fortressGenerator, 1);
        GameRegistry.addRecipe(fortressGeneratorStack, "ooo", "odo", "ooo", 'o', obsidianStack, 'd', diamondStack);
		
        //*
		ItemStack quartzStack = new ItemStack(Blocks.quartz_block, 1);
        ItemStack emergencyKeyStack = new ItemStack(fortressEmergencyKey, 1);
        GameRegistry.addRecipe(emergencyKeyStack, "qqq", "q q", "qqq", 'q', quartzStack);
        //*/
        
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

		//Fortress Generator GUI
        
		new GuiHandler();
		GameRegistry.registerTileEntity(TileEntityFortressGenerator.class, "FortressGenerator" + ModInfo.MODID);
		
		//Event Handlers
		
		//MinecraftForge.EVENT_BUS.register(new OnPlayerInteractsWithBlock_notUsed());
	}
	
	/*
	@EventHandler
	public void postInit(FMLPostInitializationEvent e){}
	//*/
	
}