package com.newyith.fortressmod;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;

import com.newyith.fortressmod.blocks.FortressBedrock;
import com.newyith.fortressmod.blocks.FortressDoor;
import com.newyith.fortressmod.blocks.FortressEmergencyKey;
import com.newyith.fortressmod.blocks.FortressGenerator;
import com.newyith.fortressmod.blocks.FortressGlass;
import com.newyith.fortressmod.blocks.FortressNetherBrick;
import com.newyith.fortressmod.client.GuiHandler;
import com.newyith.fortressmod.commands.StuckCommand;
import com.newyith.fortressmod.items.ItemFortressDoor;
import com.newyith.fortressmod.items.ItemFortressManual;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
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
	public static Block fortressNetherBrick;
	//fortress door
	public static Block fortressWoodenDoor;
	public static Block fortressIronDoor;
	public static Item itemFortressWoodenDoor;
	public static Item itemFortressIronDoor;
	public static Item itemFortressManual;
	
	//config
	public static int config_glowstoneBurnTimeMs;
	public static int config_stuckDelayMs;

	public static CreativeTabs tabName = new CreativeTabs("tabName") {
		public Item getTabIconItem() {
			return Items.arrow;
		}
	};

	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
	     MinecraftServer server = MinecraftServer.getServer();
	     
	     ICommandManager command = server.getCommandManager();
	     ServerCommandManager manager = (ServerCommandManager) command;
	     StuckCommand stuckCommand = new StuckCommand();
	     FMLCommonHandler.instance().bus().register(stuckCommand); //listen for ticks
	     manager.registerCommand(stuckCommand);
	}
	
	@EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // you will be able to find the config file in .minecraft/config/ and it will be named fortressmod.cfg
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        //load config_glowstoneBurnTimeMs
        int msPerHour = 1000*60*60;
        String name = "glowstone_burn_time_ms";
        String category = "fortressmod";
        int defaultValue = 1 * msPerHour;
        int minValue = 50; //0.05 seconds
        int maxValue = Integer.MAX_VALUE; //~1.6 years
        String comment = "How many milliseconds 1 glowstone dust will fuel a fortress generator. 3600000 is 1 hour.";
        FortressMod.config_glowstoneBurnTimeMs = config.getInt(name, category, defaultValue, minValue, maxValue, comment);
        
        //load config_stuckDelayMs
        name = "stuck_delay_ms";
        category = "fortressmod";
        defaultValue = 3 * 60 * 1000; //3 minutes
        minValue = 0;
        maxValue = Integer.MAX_VALUE; //~1.6 years
        comment = "How many milliseconds /stuck waits before teleport. 180000 is 3 minutes.";
        FortressMod.config_stuckDelayMs = config.getInt(name, category, defaultValue, minValue, maxValue, comment);
        
        config.save();
    }
	
	@EventHandler
	public void init(FMLInitializationEvent e) {
		//fortress wall (bedrock)
		fortressBedrock = new FortressBedrock().setBlockName("FortressBedrock").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressBedrock, "FortressBedrock");
		//fortress wall (glass)
		fortressGlass = new FortressGlass().setBlockName("FortressGlass").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressGlass, "FortressGlass");
		//fortress wall (nether brick)
		fortressNetherBrick = new FortressNetherBrick().setBlockName("FortressNetherBrick").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressNetherBrick, "FortressNetherBrick");

		//fortress wall (wooden door (block))
		fortressWoodenDoor = new FortressDoor(Material.wood).setBlockName("FortressWoodenDoor");
		GameRegistry.registerBlock(fortressWoodenDoor, "FortressWoodenDoor");
		//fortress wall (wooden door (item))
		itemFortressWoodenDoor = new ItemFortressDoor(Material.wood).setCreativeTab(tabName);
		GameRegistry.registerItem(itemFortressWoodenDoor, "ItemFortressWoodenDoor");

		//fortress wall (iron door (block))
		fortressIronDoor = new FortressDoor(Material.iron).setBlockName("FortressIronDoor");
		GameRegistry.registerBlock(fortressIronDoor, "FortressIronDoor");
		//fortress wall (iron door (item))
		itemFortressIronDoor = new ItemFortressDoor(Material.iron).setCreativeTab(tabName);
		GameRegistry.registerItem(itemFortressIronDoor, "ItemFortressIronDoor");
		
		//fortress generator (off)
        fortressGenerator = new FortressGenerator(FortressGeneratorState.OFF).setBlockName("FortressGenerator").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressGenerator, "FortressGenerator");
		//fortress generator (on)
		fortressGeneratorOn = new FortressGenerator(FortressGeneratorState.ACTIVE).setBlockName("FortressGeneratorActive");
		GameRegistry.registerBlock(fortressGeneratorOn, "FortressGeneratorActive");
		//fortress generator (paused)
		fortressGeneratorPaused = new FortressGenerator(FortressGeneratorState.PAUSED).setBlockName("FortressGeneratorOnAndPowered");
		GameRegistry.registerBlock(fortressGeneratorPaused, "FortressGeneratorOnAndPowered");
		//fortress generator (clogged)
        fortressGeneratorClogged = new FortressGenerator(FortressGeneratorState.CLOGGED).setBlockName("FortressGeneratorClogged"); //.setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressGeneratorClogged, "FortressGeneratorClogged");
		
		//emergency key
		fortressEmergencyKey = new FortressEmergencyKey().setBlockName("FortressEmergencyKey").setCreativeTab(tabName);
		GameRegistry.registerBlock(fortressEmergencyKey, "FortressEmergencyKey");

		//fortress manual
		itemFortressManual = new ItemFortressManual().setCreativeTab(tabName);
		GameRegistry.registerItem(itemFortressManual, "FortressManual");
		
		//Recipes
		
		//fortress generator
		ItemStack obsidianStack = new ItemStack(Blocks.obsidian, 1);
		ItemStack diamondStack = new ItemStack(Items.diamond, 1);
        ItemStack fortressGeneratorStack = new ItemStack(fortressGenerator, 1);
        GameRegistry.addRecipe(fortressGeneratorStack, "ooo", "odo", "ooo", 'o', obsidianStack, 'd', diamondStack);
		
        //emergency key
		ItemStack quartzStack = new ItemStack(Blocks.quartz_block, 1);
        ItemStack emergencyKeyStack = new ItemStack(fortressEmergencyKey, 1);
        ItemStack glowstoneBlockStack = new ItemStack(Blocks.glowstone, 1);
        GameRegistry.addRecipe(emergencyKeyStack, "qqq", "qgq", "qqq", 'q', quartzStack, 'g', glowstoneBlockStack);

        //fortress manual
        ItemStack fortressManualStack = new ItemStack(itemFortressManual, 1);
        /*
        GameRegistry.addShapelessRecipe(fortressManualStack, obsidianStack);
        /*/
        ItemStack bookStack = new ItemStack(Items.book, 1);
        GameRegistry.addShapelessRecipe(fortressManualStack, obsidianStack, bookStack);
        //*/
        
        /* debug recipes
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