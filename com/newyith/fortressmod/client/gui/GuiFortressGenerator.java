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
	private TileEntityFortressGenerator fortressGenerator;

	public GuiFortressGenerator(InventoryPlayer invPlayer, TileEntityFortressGenerator entity) {
		super(new ContainerFortressGenerator(invPlayer, entity));
		fortressGenerator = entity;

		xSize = 176;
		ySize = 165;
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int j, int i) {
		GL11.glColor4f(1F, 1F, 1F, 1F);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
	

	
	/*
	
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		 fontRenderer.drawString(StatCollector.translateToLocal("container.inventory"), 8, (ySize - 96) + 2, 0xffffff);
	}

	protected void drawGuiContainerBackgroundLayer_(float par1, int par2, int par3)
	{
		 int i = mc.renderEngine.getTexture("/Block/goldOvenGUI.png");
		 GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		 mc.renderEngine.bindTexture(i);
		 int j = (width - xSize) / 2;
		 int k = (height - ySize) / 2;
		 drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		 if (goldInventory.isBurning())
		 {
			 int burn = goldInventory.getBurnTimeRemainingScaled(14);
			 drawTexturedModalRect(j + 73, k+59, 176, 16, burn, 10);
		 }

		 int update = goldInventory.getCookProgressScaled(16);
		 drawTexturedModalRect(j+ 89, k+55, 191, 15,-update , -update);
	}
	//*/
	
	
}
