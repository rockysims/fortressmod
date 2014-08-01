package com.newyith.fortressmod.client.gui;

import com.newyith.fortressmod.ModInfo;
import com.newyith.fortressmod.TileEntityFortressGenerator;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

//TODO: to continue, search for "xSize = 176;" in http://www.minecraftforum.net/forums/mapping-and-modding/mapping-and-modding-tutorials/1571431-1-6-2-advanced-minecraft-forge-modding-tutorial-1

public class FortressGeneratorGuiContainer extends GuiContainer {

	public static final ResourceLocation texture = new ResourceLocation(ModInfo.MODID.toLowerCase(), "textures/gui/fortressGeneratorGui.png");

	public FortressGeneratorGuiContainer(InventoryPlayer invPlayer, TileEntityFortressGenerator entity) {
		super(new ContainerFortressGenerator(invPlayer, entity));

		xSize = 176;
		ySize = 165;
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float p_146976_1_,
			int p_146976_2_, int p_146976_3_) {
		// TODO Auto-generated method stub

	}
	

}
