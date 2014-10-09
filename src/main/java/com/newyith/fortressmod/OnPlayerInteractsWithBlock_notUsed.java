package com.newyith.fortressmod;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class OnPlayerInteractsWithBlock_notUsed {
	
	@SubscribeEvent
	public void onPlayerInteractsWithBlock(PlayerInteractEvent event)
	{
		World world = event.world;
		String playerName = event.entityPlayer.getGameProfile().getName();

		String s = "";
		
		s+= playerName;
		
		if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
			s += " right clicked";
		}
		
		int x = event.x;
		int y = event.y;
		int z = event.z;

		Block clickedBlockType = world.getBlock(x, y, z);
		if (clickedBlockType == Blocks.wooden_door) {
			s += " wooden door";
		}
		if (clickedBlockType == Blocks.iron_door) {
			s += " iron door";
		}
		
		int meta = world.getBlockMetadata(x, y, z);
		s += "(" + String.valueOf(meta) + ")";
		
		if (event.world.isRemote) {
			s += " on client";
		} else {
			s += " on server";
		}

		//event.setCanceled(true);
		
		Chat.sendGlobal(s);
	}
}
