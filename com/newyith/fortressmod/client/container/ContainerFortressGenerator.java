package com.newyith.fortressmod.client.container;

import com.newyith.fortressmod.Dbg;
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
	private int lastBurnTime;
	private int lastItemBurnTime;
	private boolean lastIsClogged;
	
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
		crafting.sendProgressBarUpdate(this, 0, this.fortressGenerator.burnTime);
		crafting.sendProgressBarUpdate(this, 1, this.fortressGenerator.itemBurnTime);
		crafting.sendProgressBarUpdate(this, 2, ((this.fortressGenerator.isClogged())?1:0) );
	}

	/** Looks for changes made in the container, sends them to every listener. */
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		for (int i = 0; i < this.crafters.size(); ++i) {
			ICrafting icrafting = (ICrafting)this.crafters.get(i);

			if (this.lastBurnTime != this.fortressGenerator.burnTime) {
				icrafting.sendProgressBarUpdate(this, 0, this.fortressGenerator.burnTime);
			}

			if (this.lastItemBurnTime != this.fortressGenerator.itemBurnTime) {
				icrafting.sendProgressBarUpdate(this, 1, this.fortressGenerator.itemBurnTime);
			}
			
			if (this.lastIsClogged != this.fortressGenerator.isClogged()) {
				icrafting.sendProgressBarUpdate(this, 2, ((this.fortressGenerator.isClogged())?1:0) );
			}
		}

		this.lastBurnTime = this.fortressGenerator.burnTime;
		this.lastItemBurnTime = this.fortressGenerator.itemBurnTime;
		this.lastIsClogged = this.fortressGenerator.isClogged();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int key, int value) {
		if (key == 0) this.fortressGenerator.burnTime = value;
		if (key == 1) this.fortressGenerator.itemBurnTime = value;
		if (key == 2) this.fortressGenerator.setIsClogged(value == 1);
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
