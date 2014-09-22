package com.newyith.fortressmod.commands;

import java.util.Random;

import com.newyith.fortressmod.Dbg;
import com.newyith.fortressmod.Point;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class StuckCommand extends CommandBase {
	Random random = new Random();
	
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
			
			int x = (int)player.posX;
			int y = (int)player.posY;
			int z = (int)player.posZ;
			
			Point p;
			boolean teleported = false;
			int attemptLimit = 20;
			while (attemptLimit > 0) {
				attemptLimit--;
				
				p = getRandomNearbyPoint(x, y, z);
				p = getValidTeleportDest(p);
				if (p != null) {
					player.setPositionAndUpdate(p.x, p.y, p.z);
					teleported = true;
				}
			}
			if (!teleported) {
				player.addChatMessage(new ChatComponentText("Failed to teleport you with /stuck. No suitable destination found."));
			}
		}
		
		Dbg.print("I'm /stuck!");
	}

	private Point getValidTeleportDest(Point p) {
		//TODO: write this
		return p;
	}

	private Point getRandomNearbyPoint(int x, int y, int z) {
		int quadrantSize = 2;
		int dist = quadrantSize + quadrantSize / 2 + (int)(random.nextFloat() * quadrantSize);
		
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