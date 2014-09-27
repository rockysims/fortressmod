package com.newyith.fortressmod.commands;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
	private List<StuckPlayer> stuckList = new ArrayList<StuckPlayer>();
	private int ticksPerCheck = 1*1000/50; //1 second
	private int ticksCounter = ticksPerCheck;
	
	//Called when the server ticks. Usually 20 ticks a second.
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			this.ticksCounter++;
			if (this.ticksCounter > this.ticksPerCheck) {
				this.ticksCounter = 0;
				//check if any stuck players need message or teleport
				
				List<StuckPlayer> stuckListCopy = new ArrayList<StuckPlayer>(stuckList);
				for (StuckPlayer player : stuckListCopy) {
					if (player.isDoneWaiting()) {
						player.stuckTeleport();
						stuckList.remove(player);
					} else {
						boolean cancelled = player.considerCancelling();
						if (cancelled) {
							stuckList.remove(player);
						} else {
							player.considerSendingMessage();
						}
					}
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
			
			StuckPlayer alreadyStuckPlayer = null;
			for (StuckPlayer stuckPlayer : stuckList) {
				if (stuckPlayer.isPlayer(player)) {
					alreadyStuckPlayer = stuckPlayer;
				}
			}
			
			if (alreadyStuckPlayer == null) {
				StuckPlayer stuckPlayer = new StuckPlayer(player);
				stuckPlayer.sendStartMessage();
				stuckList.add(stuckPlayer);
			} else {
				alreadyStuckPlayer.sendBePatientMessage();
			}
		}
	}
}