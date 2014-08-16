package com.newyith.fortressmod;

import java.util.Arrays;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.newyith.fortressmod.client.GuiHandler;
import com.newyith.fortressmod.items.ItemFortressDoor;
import com.newyith.fortressmod.renderers.FortressIronDoorRenderer;

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

//TODO: work on fortress generator gui. see http://www.minecraftforum.net/forums/mapping-and-modding/mapping-and-modding-tutorials/1571074-1-6-2-advanced-minecraft-forge-modding-tutorial-1
//TODO: work on FortressGeneratorTileEntity class

//TODO: make only the oldest (in terms of when placed) fortress generator effect wall on/off state
//TODO: on destroy oldest fortress generator make all other fortress generators touching the wall clog
//TODO: add FortressEmergencyKey (8 quarts to craft, emergency key only clogs fortress if generator generating it was placed by same person)
//TODO: think about issue: if you turn on fg to generate fortress next to second fortress, then connect the 2 fortresses, then stop generating, that would update the whole wall or leave behind permanent bedrock

@Mod(modid = ModInfo.MODID, name = ModInfo.NAME, version = ModInfo.VERSION)
public class FortressMod
{
    @Instance(value = ModInfo.MODID)
    public static FortressMod modInstance;
	
	public static Block fortressGenerator;
	public static Block fortressGeneratorOn;
	public static Block fortressGeneratorClogged;
	public static Block fortressEmergencyKey;
	public static Block fortressBedrock;
	public static Block fortressGlass;
	//fortress door
	public static Block fortressIronDoor;
	public static Item itemFortressDoor;
	public static int fortressIronDoorRenderId;

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
		//Fortress Wall Blocks
		
		fortressBedrock = new FortressBedrock().setBlockName("FortressBedrock").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressBedrock, "FortressBedrock");

		fortressGlass = new FortressGlass().setBlockName("FortressGlass").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressGlass, "FortressGlass");

		//fortress door block
		fortressIronDoor = new FortressIronDoor().setBlockName("FortressIronDoor").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressIronDoor, "FortressIronDoor");
		//GameRegistry.registerBlock(fortressIronDoor, ItemFortressDoor.class, "FortressIronDoor");
		//fortress door item
		itemFortressDoor = new ItemFortressDoor();
		GameRegistry.registerItem(itemFortressDoor, "ItemFortressDoor");
		//fortress door renderer
		fortressIronDoorRenderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(new FortressIronDoorRenderer(fortressIronDoorRenderId));
		
		//Fortress Generators

        fortressGenerator = new FortressGenerator(false).setBlockName("FortressGenerator").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressGenerator, "FortressGenerator");
		
        fortressGeneratorOn = new FortressGenerator(true).setBlockName("FortressGeneratorActive");
		GameRegistry.registerBlock(fortressGeneratorOn, "FortressGeneratorActive");
		
        fortressGeneratorClogged = new FortressGenerator(false, true).setBlockName("FortressGeneratorClogged").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressGeneratorClogged, "FortressGeneratorClogged");
		
		//Fortress Generator Emergency Key
		fortressEmergencyKey = new FortressEmergencyKey().setBlockName("FortressEmergencyKey").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressEmergencyKey, "FortressEmergencyKey");
		
		//Recipes
		
		ItemStack obsidianStack = new ItemStack(Blocks.obsidian, 1);
        ItemStack fortressGeneratorStack = new ItemStack(fortressGenerator, 1);
        GameRegistry.addRecipe(fortressGeneratorStack, "ooo", "o o", "ooo", 'o', obsidianStack);
		
		ItemStack quartzStack = new ItemStack(Blocks.quartz_block, 1);
        ItemStack emergencyKeyStack = new ItemStack(fortressEmergencyKey, 1);
        GameRegistry.addRecipe(emergencyKeyStack, "qqq", "q q", "qqq", 'q', quartzStack);
        
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