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
import com.newyith.fortressmod.FortressMod;
import com.newyith.fortressmod.Point;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class StuckPlayer {
	private EntityPlayer player;
	private Point startPoint;
	private float maxHealthOnRecord;
	private long startTimestamp;

	private Map<Integer, String> messages;

	Random random = new Random();
	private int quadrantSize = 32;
	private final int stuckDelayMs = FortressMod.config_stuckDelayMs;

	public StuckPlayer(EntityPlayer player) {
		this.player = player;
		this.startPoint = new Point(player.getPlayerCoordinates().posX, player.getPlayerCoordinates().posY, player.getPlayerCoordinates().posZ);
		this.maxHealthOnRecord = player.getHealth();
		this.startTimestamp = new Date().getTime();
		
		this.messages = new HashMap<Integer, String>();
		int ms;
		ms = 1*1000;
		this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/1000) + " seconds.");
		ms = 2*1000;
		this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/1000) + " seconds.");
		ms = 3*1000;
		this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/1000) + " seconds.");
		ms = 4*1000;
		this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/1000) + " seconds.");
		ms = 5*1000;
		this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/1000) + " seconds.");
		ms = 10*1000;
		this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/1000) + " seconds.");
		ms = 15*1000;
		this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/1000) + " seconds.");
		ms = 30*1000;
		this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/1000) + " seconds.");
		ms = 1*60*1000;
		this.messages.put(ms, "/stuck teleport in 1 minute.");
		for (int i = 2; i*60*1000 < this.stuckDelayMs; i++) {
			ms = i*60*1000;
			this.messages.put(ms, "/stuck teleport in " + String.valueOf(ms/(1000*60)) + " minutes.");
		}
		
		//remove messages that would be shown later than or at stuckDelayMs
		List<Integer> displayTimes = new ArrayList<Integer>(this.messages.keySet());
		for (int displayTime : displayTimes) {
			if (displayTime >= this.stuckDelayMs) {
				this.messages.remove(displayTime);
			}
		}
	}
	
	public void considerSendingMessage() {
		int remaining = this.getRemainingMs();
		
		List<Integer> displayTimes = new ArrayList<Integer>(this.messages.keySet());
		Collections.sort(displayTimes);
		Collections.reverse(displayTimes);
		for (int displayTime : displayTimes) {
			if (remaining <= displayTime) {
				//time to display the message
				String msg = this.messages.get(displayTime);
				this.messages.remove(displayTime);
				sendMessage(msg);
				break;
			}
		}
	}
	
	public boolean considerCancelling() {
		boolean cancel = false;
		
		int x = this.player.getPlayerCoordinates().posX;
		int y = this.player.getPlayerCoordinates().posY;
		int z = this.player.getPlayerCoordinates().posZ;
		int changeInX = Math.abs(x - this.startPoint.x);
		int changeInY = Math.abs(y - this.startPoint.y);
		int changeInZ = Math.abs(z - this.startPoint.z);
		
		int maxChange = 8;
		if (changeInX > maxChange || changeInY > maxChange || changeInZ > maxChange) {
			//player moved too far away
			cancel = true;
			String msg = "/stuck cancelled because you moved too far away.";
			sendMessage(msg);
		}
		
		float health = this.player.getHealth();
		if (health < this.maxHealthOnRecord) {
			//player took damage
			cancel = true;
			String msg = "/stuck cancelled because you took damage.";
			sendMessage(msg);
		} else if (health > this.maxHealthOnRecord) {
			//player healed
			this.maxHealthOnRecord = this.player.getHealth();
		}
		
		return cancel;
	}

	public void sendStartMessage() {
		String msgLine1 = "/stuck will cancel if you move 8+ blocks away or take damage.";
		
		String msgLine2 = "";
		int ms = this.stuckDelayMs;
		if (ms <= 5*1000) {
			//first natural message will be soon enough
		} else if (ms < 60*1000) { //less than a minute delay
			msgLine2 = "/stuck teleport in " + String.valueOf(ms/1000) + " seconds.";
		} else {
			msgLine2 = "/stuck teleport in " + String.valueOf(ms/(1000*60)) + " minutes.";
		}
		
		this.sendMessage(msgLine1);
		if (msgLine2.length() > 0) {
			this.sendMessage(msgLine2);
		}
	}

	public void sendBePatientMessage() {
		int remainingSeconds = this.getRemainingMs() / 1000;
		String msg = "/stuck teleport in " + String.valueOf(remainingSeconds) + " seconds... be patient.";
		this.sendMessage(msg);
	}
	
	private void sendMessage(String msg) {
		msg = EnumChatFormatting.AQUA + msg;
		this.player.addChatMessage(new ChatComponentText(msg));
	}

	public boolean isPlayer(EntityPlayer otherPlayer) {
		return this.player.getGameProfile().getName() == otherPlayer.getGameProfile().getName();
	}

	public boolean isDoneWaiting() {
		return this.getElapsedMs() > this.stuckDelayMs;
	}
	
	private int getElapsedMs() {
		long now = new Date().getTime();
		int elapsed = (int) (now - this.startTimestamp);
		return elapsed;
	}
	
	private int getRemainingMs() {
		return this.stuckDelayMs - this.getElapsedMs();
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
			this.sendMessage("/stuck failed because no suitable destination was found.");
		}
	}

	private Point getValidTeleportDest(Point p, World world) {
		Point validDest = null;
		
		int maxHeight = world.getActualHeight();
		for (int y = maxHeight-2; y >= 0; y--) {
			Block b = world.getBlock(p.x, y, p.z);
			if (!b.isAir(world, p.x, y, p.z)) {
				//first non air block
				
				//check if valid teleport destination
				if (b.isSideSolid(world, p.x, y, p.z, ForgeDirection.UP)) {
					validDest = new Point(p.x, y+1, p.z);
				}
				
				break;
			}
		}
		
		return validDest;
	}

	private Point getRandomNearbyPoint(int x, int y, int z) {
		int dist = quadrantSize  + quadrantSize / 2 + (int)(random.nextFloat() * quadrantSize);
		
		//pick a quadrant
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
