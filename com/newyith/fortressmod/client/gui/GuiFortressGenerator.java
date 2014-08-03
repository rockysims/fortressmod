package com.newyith.fortressmod.client.gui;

import org.lwjgl.opengl.GL11;

import com.newyith.fortressmod.ModInfo;
import com.newyith.fortressmod.TileEntityFortressGenerator;
import com.newyith.fortressmod.client.container.ContainerFortressGenerator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiFortressGenerator extends GuiContainer {

	public static final ResourceLocation texture = new ResourceLocation(ModInfo.MODID.toLowerCase(), "textures/gui/fortressGeneratorGui.png");

	public GuiFortressGenerator(InventoryPlayer invPlayer, TileEntityFortressGenerator entity) {
		super(new ContainerFortressGenerator(invPlayer, entity));

		xSize = 176;
		ySize = 165;
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int j, int i) {
		GL11.glColor4f(1F, 1F, 1F, 1F);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
	

}
