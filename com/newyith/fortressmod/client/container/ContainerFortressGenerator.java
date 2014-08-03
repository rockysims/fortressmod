package com.newyith.fortressmod.client.container;

import com.newyith.fortressmod.TileEntityFortressGenerator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

public class ContainerFortressGenerator extends Container {

	private TileEntityFortressGenerator fortressGenerator;
	
	public ContainerFortressGenerator(InventoryPlayer invPlayer, TileEntityFortressGenerator entity) {
		this.fortressGenerator = entity;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return fortressGenerator.isUseableByPlayer(player);
	}

}
