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
		this.fortressGenerator = entity;

		xSize = 176;
		ySize = 165;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		String name;
		if (this.fortressGenerator.isClogged()) {
			name = "Clogged Fortress Generator";
			fontRendererObj.drawString(StatCollector.translateToLocal(name), 17, 4, 0x404040);
		} else if (this.fortressGenerator.isPaused()) {
			name = "Paused Fortress Generator";
			fontRendererObj.drawString(StatCollector.translateToLocal(name), 18, 4, 0x404040);
		} else {
			name = "Fortress Generator";
			fontRendererObj.drawString(StatCollector.translateToLocal(name), 40, 4, 0x404040);
		}
		
		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, (ySize - 96) + 2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);

        if (this.fortressGenerator.isBurning())
        {
        	int xOffset = 24;
        	int yOffset = -12;
            int i1 = this.fortressGenerator.getBurnTimeRemainingScaled(13);
            this.drawTexturedModalRect(
            		xOffset + k + 56, yOffset + l + 36 + 12 - i1,
            		176, 12 - i1, 
            		14, i1 + 1);
            //i1 = this.fortressGenerator.getCookProgressScaled(24);
            //this.drawTexturedModalRect(k + 79, l + 34, 176, 14, i1 + 1, 16);
        }
	}
	
}
