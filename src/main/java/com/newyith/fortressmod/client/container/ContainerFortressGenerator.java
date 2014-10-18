package com.newyith.fortressmod.client.container;

import java.nio.ByteBuffer;
import java.util.Date;

import com.newyith.fortressmod.Dbg;
import com.newyith.fortressmod.FortressGeneratorState;
import com.newyith.fortressmod.FortressMod;
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
	private FortressGeneratorState lastState = FortressGeneratorState.OFF;
	private int lastBurnTicksRemaining = 0;
	private int lastGlowstoneBurnTimeMs = 0;
	private Short burnTicksRemainingShort1;
	private Short burnTicksRemainingShort2;
	private Short glowstoneBurnTimeMsShort1;
	private Short glowstoneBurnTimeMsShort2;
	
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
	
	private static short[] int2shorts(int i) {  
	    ByteBuffer buf = ByteBuffer.allocate(4);  
	    buf.putInt(i);  
	   
	    buf.rewind();  
	    return new short[] {buf.getShort(), buf.getShort()};  
	}
	
	private static int shorts2int(short[] shorts) {
		 ByteBuffer buf = ByteBuffer.allocate(4);  
		 buf.putShort(shorts[0]);
		 buf.putShort(shorts[1]);
		 
		 buf.rewind();
		 return buf.getInt();
	}
	
	@Override
	public void addCraftingToCrafters(ICrafting crafting) {
		super.addCraftingToCrafters(crafting);
		crafting.sendProgressBarUpdate(this, 0, this.fortressGenerator.getState().ordinal());
		
		short[] shorts = int2shorts(this.fortressGenerator.burnTicksRemaining);
		crafting.sendProgressBarUpdate(this, 1, shorts[0]);
		crafting.sendProgressBarUpdate(this, 2, shorts[1]);
		
		//hack to prevent disagreement between client and server about config_glowstoneBurnTimeMs
		shorts = int2shorts(FortressMod.config_glowstoneBurnTimeMs);
		crafting.sendProgressBarUpdate(this, 3, shorts[0]);
		crafting.sendProgressBarUpdate(this, 4, shorts[1]);
	}

	/** Looks for changes made in the container, sends them to every listener. */
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

		for (int i = 0; i < this.crafters.size(); ++i) {
			ICrafting icrafting = (ICrafting)this.crafters.get(i);

			if (this.lastState != this.fortressGenerator.getState()) {
				icrafting.sendProgressBarUpdate(this, 0, this.fortressGenerator.getState().ordinal());
			}

			if (this.lastBurnTicksRemaining != this.fortressGenerator.burnTicksRemaining) {
				short[] shorts = int2shorts(this.fortressGenerator.burnTicksRemaining);
				icrafting.sendProgressBarUpdate(this, 1, shorts[0]);
				icrafting.sendProgressBarUpdate(this, 2, shorts[1]);
			}

			//hack to prevent disagreement between client and server about config_glowstoneBurnTimeMs
			if (this.lastGlowstoneBurnTimeMs != FortressMod.config_glowstoneBurnTimeMs) {
				short[] shorts = int2shorts(FortressMod.config_glowstoneBurnTimeMs);
				icrafting.sendProgressBarUpdate(this, 3, shorts[0]);
				icrafting.sendProgressBarUpdate(this, 4, shorts[1]);
			}
		}

		this.lastState = this.fortressGenerator.getState();
		this.lastBurnTicksRemaining = this.fortressGenerator.burnTicksRemaining;
		this.lastGlowstoneBurnTimeMs = FortressMod.config_glowstoneBurnTimeMs;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int key, int value) {
		if (key == 0) {
			this.fortressGenerator.setState(FortressGeneratorState.values()[value]);
		}

		//listen for both parts of burnTicksRemaining then set it
		if (key == 1 || key == 2) {
			if (key == 1)
				this.burnTicksRemainingShort1 = (short) value;
			else
				this.burnTicksRemainingShort2 = (short) value;
			
			if (this.burnTicksRemainingShort1 != null && this.burnTicksRemainingShort2 != null) {
				this.fortressGenerator.burnTicksRemaining = shorts2int(new short[] {this.burnTicksRemainingShort1, this.burnTicksRemainingShort2});
				
				this.burnTicksRemainingShort1 = null;
				this.burnTicksRemainingShort2 = null;
			}
		}

		//hack to prevent disagreement between client and server about config_glowstoneBurnTimeMs
		//listen for both parts of config_glowstoneBurnTimeMs then set it
		if (key == 3 || key == 4) {
			if (key == 3)
				this.glowstoneBurnTimeMsShort1 = (short) value;
			else
				this.glowstoneBurnTimeMsShort2 = (short) value;
			
			if (this.glowstoneBurnTimeMsShort1 != null && this.glowstoneBurnTimeMsShort2 != null) {
				FortressMod.config_glowstoneBurnTimeMs = shorts2int(new short[] {this.glowstoneBurnTimeMsShort1, this.glowstoneBurnTimeMsShort2});
				
				this.glowstoneBurnTimeMsShort1 = null;
				this.glowstoneBurnTimeMsShort2 = null;
			}
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
