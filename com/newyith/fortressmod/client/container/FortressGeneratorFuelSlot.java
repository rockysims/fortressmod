package com.newyith.fortressmod.client.container;

import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class FortressGeneratorFuelSlot extends Slot {

	public FortressGeneratorFuelSlot(IInventory par1, int par2, int par3, int par4) {
		super(par1, par2, par3, par4);
	}

	@Override
	public boolean isItemValid(ItemStack itemstack) {
		if (itemstack.getItem() == Items.glowstone_dust)
			return true;
        return false;
    }
	
}
