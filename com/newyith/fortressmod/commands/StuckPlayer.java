package com.newyith.fortressmod.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.newyith.fortressmod.Dbg;
import com.newyith.fortressmod.Point;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class StuckPlayer {
	private EntityPlayer player;
	private float initialHealth;
	private long startTimestamp;

	private Map<Integer, String> messages;

	Random random = new Random();
	private int quadrantSize = 64;
	private final int stuckDelayMs = 20*1000; //TODO: change back to 5*60*1000

	public StuckPlayer(EntityPlayer player) {
		this.player = player;
		this.startTimestamp = new Date().getTime();
		this.initialHealth = player.getHealth();
		
		this.messages = new HashMap<Integer, String>();
		int ms;
		ms = 1*1000;
		this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/1000) + " seconds");
		ms = 2*1000;
		this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/1000) + " seconds");
		ms = 3*1000;
		this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/1000) + " seconds");
		ms = 5*1000;
		this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/1000) + " seconds");
		ms = 10*1000;
		this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/1000) + " seconds");
		ms = 15*1000;
		this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/1000) + " seconds");
		ms = 30*1000;
		this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/1000) + " seconds");
		ms = 1*60*1000;
		this.messages.put(ms, "/stuck teleport in 1 minute");
		for (int i = 2; i*60*1000 < this.stuckDelayMs; i++) {
			ms = i*60*1000;
			this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/(1000*60)) + " minutes");
		}
		
		//remove messages that would be shown earlier than stuckDelayMs
		List<Integer> displayTimes = new ArrayList<Integer>(this.messages.keySet());
		for (int displayTime : displayTimes) {
			if (displayTime > this.stuckDelayMs) {
				this.messages.remove(displayTime);
			}
		}
	}
	
	public void considerSendingMessage() {
		Dbg.print("considerSendingMessage()");
		
		int elapsed = this.getElapsedMs();
		int remaining = this.stuckDelayMs - elapsed;
		
		List<Integer> displayTimes = new ArrayList<Integer>(this.messages.keySet());
		Collections.sort(displayTimes);
		Collections.reverse(displayTimes);
		for (int displayTime : displayTimes) {
			if (remaining <= displayTime) {
				//time to display the message
				String msg = this.messages.get(displayTime);
				this.messages.remove(displayTime);
				player.addChatMessage(new ChatComponentText(msg));
				break;
			}
		}
	}

	public boolean isDoneWaiting() {
		return this.getElapsedMs() > this.stuckDelayMs;
	}
	
	private int getElapsedMs() {
		long now = new Date().getTime();
		int elapsed = (int) (now - this.startTimestamp);
		return elapsed;
	}

	public void stuckTeleport() {
		int x = (int)player.posX;
		int y = (int)player.posY;
		int z = (int)player.posZ;

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
