package com.newyith.fortressmod.client.container;

import java.util.Date;

import com.newyith.fortressmod.Dbg;
import com.newyith.fortressmod.FortressGeneratorState;
import com.newyith.fortressmod.TileEntityFortressGenerator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;

public class ContainerFortressGenerator extends Container {

	private TileEntityFortressGenerator fortressGenerator;
	private int lastBurnTicksRemaining;
	private FortressGeneratorState lastState = FortressGeneratorState.OFF;
	private boolean lastIsPaused;
	
	public ContainerFortressGenerator(InventoryPlayer invPlayer, TileEntityFortressGenerator entity) {
		this.fortressGenerator = entity;
		
		//player inventory and hot bar must be added first for shit-clicking to work properly
		//player inventory
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				this.addSlotToContainer(new Slot(invPlayer, 9 + x + y * 9, 8 + x * 18, 84 + y * 18));
			}
		}
		//player hot bar
		for (int x = 0; x < 9; x++) {
			this.addSlotToContainer(new Slot(invPlayer, x, 8 + x * 18, 142));
		}

		//custom slots
		addSlotToContainer(new FortressGeneratorFuelSlot(entity, 0, 80, 41)); //fuel slot
	}
	
	@Override
	public void addCraftingToCrafters(ICrafting crafting) {
		super.addCraftingToCrafters(crafting);
		crafting.sendProgressBarUpdate(this, 0, this.fortressGenerator.burnTicksRemaining);
		crafting.sendProgressBarUpdate(this, 1, this.fortressGenerator.getState().ordinal());
	}

	/** Looks for changes made in the container, sends them to every listener. */
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		for (int i = 0; i < this.crafters.size(); ++i) {
			ICrafting icrafting = (ICrafting)this.crafters.get(i);

			if (this.lastBurnTicksRemaining != this.fortressGenerator.burnTicksRemaining) {
				icrafting.sendProgressBarUpdate(this, 0, this.fortressGenerator.burnTicksRemaining);
			}

			if (this.lastState != this.fortressGenerator.getState()) {
				icrafting.sendProgressBarUpdate(this, 1, this.fortressGenerator.getState().ordinal());
			}
		}

		this.lastBurnTicksRemaining = this.fortressGenerator.burnTicksRemaining;
		this.lastState = this.fortressGenerator.getState();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int key, int value) {
		if (key == 0) this.fortressGenerator.burnTicksRemaining = value;
		if (key == 1) {
			Dbg.print("updateProgressBar(): setState to " + (FortressGeneratorState.values()[value]).name());
			this.fortressGenerator.setState(FortressGeneratorState.values()[value]);
		}
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return fortressGenerator.isUseableByPlayer(player);
	}
	
	/**
	 * Allows player to shift-click into the inventory of the fortress generator
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int i) {
		Slot slot = getSlot(i);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack = slot.getStack();
			ItemStack result = itemstack.copy();

			if (i >= 36) {
				if (!mergeItemStack(itemstack, 0, 36, false)) {
					return null;
				}
			} else if (!mergeItemStack(itemstack, 36, 36 + fortressGenerator.getSizeInventory(), false)) {
				return null;
			}

			if (itemstack.stackSize == 0) {
				slot.putStack(null);
			} else {
				slot.onSlotChanged();
			}
			slot.onPickupFromSlot(player, itemstack); 
			return result;
		}
		return null;
	}

}
