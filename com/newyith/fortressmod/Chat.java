package com.newyith.fortressmod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class Chat {
	public static void sendGlobal(String msg) {
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(msg));
	}
	public static void sendMessageToPlayer(String msg, EntityPlayer player) {
		player.addChatMessage(new ChatComponentText(msg));
	}
}
