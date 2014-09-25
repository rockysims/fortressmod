package com.newyith.fortressmod.commands;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.newyith.fortressmod.Dbg;
import com.newyith.fortressmod.FortressMod;
import com.newyith.fortressmod.Point;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class StuckCommand extends CommandBase {
	Random random = new Random();
	private int quadrantSize = 64;
	private Map<EntityPlayer, Integer> stuckList = new HashMap<EntityPlayer, Integer>();
	private int stuckDelayMs = 5*60/60*1000; //TODO: change back to 5*60*1000

	
	
	//Called when the server ticks. Usually 20 ticks a second.
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			for (EntityPlayer player : stuckList.keySet()) {
				int waitTicks = stuckList.get(player);
				
				if (waitTicks > 0) {
					if (waitTicks % (60*1000/50) == 0) { //every 60 seconds
						int minutesRemaining = (waitTicks * 50) / (1000*60);
						player.addChatMessage(new ChatComponentText("/stuck teleport in " + String.valueOf(minutesRemaining) + " minutes"));
					}
					
					waitTicks--;
					stuckList.put(player, waitTicks);
				} else {
					stuckList.remove(player);
					stuckTeleport(player);
				}
			}
		}
	}
	
	@Override
	public String getCommandName() {
		// The name of the command, the string the user has to type following a "/" to call the command
		return "stuck";
	}
	
	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		// The string to show when the user types "/help X", X being the string from "getCommandName"
		return "/stuck";
	}
	
	// Method called when the command is typed in
	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		if (icommandsender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) icommandsender;
			stuckList.put(player, (int) ((this.stuckDelayMs )/50));
			//stuckTeleport(player);
		}
	}
	
	private void stuckTeleport(EntityPlayer player) {
		int x = (int)player.posX;
		int y = (int)player.posY;
		int z = (int)player.posZ;

		//TODO: make message on next line true
		player.addChatMessage(new ChatComponentText("Moving 8 blocks from here or taking damage will cancel /stuck."));

		Point p;
		boolean teleported = false;
		int attemptLimit = 20;
		while (!teleported && attemptLimit > 0) {
			attemptLimit--;
			
			p = getRandomNearbyPoint(x, y, z);
			p = getValidTeleportDest(p, player.worldObj);
			if (p != null) {
				player.setPositionAndUpdate(p.x + 0.5F, p.y, p.z + 0.5F);
				teleported = true;
			}
		}
		if (!teleported) {
			player.addChatMessage(new ChatComponentText("/stuck failed because no suitable destination was found."));
		}
	}

	private Point getValidTeleportDest(Point p, World world) {
		Point validDest = null;
		
		int maxHeight = world.getActualHeight();
		for (int y = maxHeight-2; y >= 0; y--) {
			Block b = world.getBlock(p.x, y, p.z);
			if (b != Blocks.air) {
				//first non air block
				
				//check if valid teleport destination
				Block b1 = world.getBlock(p.x, y+1, p.z);
				Block b2 = world.getBlock(p.x, y+2, p.z);
				if (b.isSideSolid(world, p.x, y, p.z, ForgeDirection.UP)) {
					boolean canSpawn1 = (b1 == Blocks.air) || b1 instanceof BlockBush;
					boolean canSpawn2 = (b2 == Blocks.air) || b2 instanceof BlockBush;
					if (canSpawn1 && canSpawn2) {
						validDest = new Point(p.x, y+1, p.z);
					}
					break;
				}
			}
		}
		
		return validDest;
	}

	private Point getRandomNearbyPoint(int x, int y, int z) {
		int dist = quadrantSize  + quadrantSize / 2 + (int)(random.nextFloat() * quadrantSize);
		
		//move left, right, forward, or backward by dist
		float f = random.nextFloat() * 100;
		if (f < 25) {
			x += dist;
		} else if (f < 50) {
			x -= dist;
		} else if (f < 75) {
			z += dist;
		} else {
			z -= dist;
		}
		
		//move left or right OR forward or backward by quadrantSize
		if (f < 50) { //x changed
			if (random.nextFloat() * 100 < 50) {
				z += quadrantSize;
			} else {
				z -= quadrantSize;
			}
		} else {// z changed
			if (random.nextFloat() * 100 < 50) {
				x += quadrantSize;
			} else {
				x -= quadrantSize;
			}
		}
		
		//move to random point in quadrant
		x += (int)(random.nextFloat() * quadrantSize) - (quadrantSize/2);
		z += (int)(random.nextFloat() * quadrantSize) - (quadrantSize/2);
		
		return new Point(x, y, z);
	}

}