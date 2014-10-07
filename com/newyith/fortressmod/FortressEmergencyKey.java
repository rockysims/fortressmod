package com.newyith.fortressmod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class FortressEmergencyKey extends BlockQuartz {
	
	public FortressEmergencyKey() {
		super();
		setHardness(3.5F);
	}
	
	/** Called when the block is placed in the world. */
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;
			activateKey(world, x, y, z, player);
		}
	}
	
	/** Called upon block activation (right click on the block.) */
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
    	activateKey(world, x, y, z, player);
    	return true;
    }
	
	private void activateKey(World world, int x, int y, int z, EntityPlayer player) {
		String keyPlacingPlayerName = player.getGameProfile().getName();
		
		int clogged = clogConnectedGeneratorsPlacedBy(world, x, y, z, keyPlacingPlayerName);
		if (clogged > 0) {
			world.setBlock(x, y, z, Blocks.air);
			String msg = "Emergency key clogged " + clogged + " fortress generator" + ((clogged > 1)?"s":"") + ".";
			msg = EnumChatFormatting.AQUA + msg;
			Chat.sendMessageToPlayer(msg, player);
		}
	}
	
	private int clogConnectedGeneratorsPlacedBy(World world, int x, int y, int z, String keyPlacingPlayerName) {
		Point emergencyKeyPoint = new Point(x, y, z);
		ArrayList<TileEntityFortressGenerator> fgs = this.getConnectedFortressGeneratorsNotClogged(world, emergencyKeyPoint); 
		
		int clogCount = 0;
		for (TileEntityFortressGenerator fg : fgs) {
			if (fg.getGeneratorCore().getPlacedByPlayerName().contentEquals(keyPlacingPlayerName)) {
				fg.getGeneratorCore().clog();
				clogCount++;
			}
		}
		
		return clogCount;
	}

	private ArrayList<TileEntityFortressGenerator> getConnectedFortressGeneratorsNotClogged(World world, Point origin) {
		ArrayList<TileEntityFortressGenerator> matches = new ArrayList<TileEntityFortressGenerator>();
		
		Set<Point> connectedFgPoints;
		connectedFgPoints = Wall.getPointsConnected(world, origin, Wall.getWallBlocks(), Wall.getNotCloggedGeneratorBlocks(), Wall.ConnectedThreshold.FACES);
		for (Point p : connectedFgPoints) {
			TileEntityFortressGenerator fg = (TileEntityFortressGenerator) world.getTileEntity(p.x, p.y, p.z);
			matches.add(fg);
		}

		return matches;
	}
	
	// ------------------
	
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
    	String iconStr = ModInfo.MODID.toLowerCase() + ":" + "fortress_emergency_key";
        this.blockIcon = iconRegister.registerIcon(iconStr);
    }
}
