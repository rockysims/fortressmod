package com.newyith.fortressmod.client.gui;

import org.lwjgl.opengl.GL11;

import com.newyith.fortressmod.ModInfo;
import com.newyith.fortressmod.TileEntityFortressGenerator;
import com.newyith.fortressmod.client.container.ContainerFortressGenerator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

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
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		fontRendererObj.drawString(StatCollector.translateToLocal("Fortress Generator"), 40, 4, 0x404040);
		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		GL11.glColor4f(1F, 1F, 1F, 1F);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		if (fortressGenerator.isBurning()) {
			int burn = fortressGenerator.getBurnTimeRemainingScaled(14);
			drawTexturedModalRect(j+73, k+59, 176, 16, burn, 10);
			System.out.println("burn: " + burn); //TODO: delete this line
		}
	}
	
}
