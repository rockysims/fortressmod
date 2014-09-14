package com.newyith.fortressmod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.IconFlipped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class FortressDoor extends BlockDoor {
	//i think meta == (top/bottom)(open/closed)(?)(?)
	
	private Material origMaterial;

	protected FortressDoor(Material origMaterial) {
		super(Material.rock); //not Material.iron so that it will open close on right click and not wood so it can't burn
		setHardness(1.0F);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		disableStats();
		this.origMaterial = origMaterial;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getTextureName() {
		return ModInfo.MODID.toLowerCase() + ":" + "fortress_door";
	}

	/**
	 * Updates door open state based on isPowered.
	 */
	@Override
	public void func_150014_a(World world, int x, int y, int z, boolean isPowered) {
		//do nothing (ignore redstone power)
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
    	String playerName = player.getGameProfile().getName();
    	ArrayList<String> authorizedNames = getNamesAllowedToOpenDoor(world, x, y, z);

    	//convert authorizedNames to lower case
    	for (int i = 0; i < authorizedNames.size(); i++)
    		authorizedNames.set(i, authorizedNames.get(i).toLowerCase());
    	
    	if (authorizedNames.contains(playerName.toLowerCase())) {
			return super.onBlockActivated(world, x, y, z, player, par6, par7, par8, par9);
		} else {
			return false;
		}
	}
	private ArrayList<String> getNamesAllowedToOpenDoor(World world, int x, int y, int z) {
		ArrayList<String> names = new ArrayList<String>();
		
		//get blockAboveDoor and pointAboveDoor
		Point pointAboveDoor = new Point(x, y + 1, z);
		Block blockAboveDoor = world.getBlock(x, y + 1, z);
		if (blockAboveDoor == FortressMod.fortressWoodenDoor || blockAboveDoor == FortressMod.fortressIronDoor) {
			blockAboveDoor = world.getBlock(x, y + 2, z);
			pointAboveDoor = new Point(x, y + 2, z);
		}
		
		if (blockAboveDoor == Blocks.obsidian || blockAboveDoor == FortressMod.fortressObsidian) {
			ArrayList<Block> wallBlocks = new ArrayList<Block>();
			ArrayList<Block> returnBlocks = new ArrayList<Block>();
			wallBlocks.add(Blocks.obsidian);
			wallBlocks.add(FortressMod.fortressObsidian);
			returnBlocks.add(Blocks.wall_sign);
			//returnBlocks.add(Blocks.standing_sign);
			List<Point> signs = Wall.getPointsConnected(world, pointAboveDoor, wallBlocks, returnBlocks, Wall.ConnectedThreshold.FACES);
			
			for (Point p : signs) {
				names.addAll(getNamesFromSign(world, p));
			}
		}
		
		return names;
	}
	
	private ArrayList<String> getNamesFromSign(World world, Point signPoint) {
		ArrayList<String> names = new ArrayList<String>();
		
		TileEntitySign sign = (TileEntitySign)world.getTileEntity(signPoint.x, signPoint.y, signPoint.z);
		String s = "";
		s += sign.signText[0];
		s += "\n";
		s += sign.signText[1];
		s += "\n";
		s += sign.signText[2];
		s += "\n";
		s += sign.signText[3];
		s = s.replaceAll(" ", "");
		
		if (s.contains(",")) {
			s = s.replaceAll("\n", "");
			for (String name : s.split(",")) {
				names.add(name);
			}
		} else {
			for (String name : s.split("\n")) {
				names.add(name);
			}
		}
		
		//Dbg.print("getNamesFromSign returning " + names.toString());
		return names;
	}

	public Item getItemDropped(int par1, Random par2, int par3) {
		Item item = (this.origMaterial == Material.wood)?FortressMod.itemFortressWoodenDoor:FortressMod.itemFortressIronDoor;
		return (par1 & 8) != 0 ? null : item;
	}
}
