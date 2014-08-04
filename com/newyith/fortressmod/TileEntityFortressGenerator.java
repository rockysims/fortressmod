package com.newyith.fortressmod;

import akka.event.Logging.Debug;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityFortressGenerator extends TileEntity implements IInventory {
	private ItemStack[] inventory;

	/** The number of ticks that the current item (none in this case) has been cooking for */
	public int cookTime;

	/** The number of ticks that the fortress generator will keep burning */
	public int burnTime; //remaining burn time

	/** The number of ticks that a fresh copy of the currently-burning item would keep the furnace burning for */
	public int itemBurnTime;
	
	private boolean isActive;

	public TileEntityFortressGenerator() {
		this.inventory = new ItemStack[2];
		this.cookTime = 0;
		this.burnTime = 0;
		this.itemBurnTime = 0;
	}
	
	@Override
	public void updateEntity() {
		boolean wasBurning = this.burnTime > 0;
		boolean flag1 = false;
		
		if (this.burnTime > 0) {
			this.burnTime--;
		}
		if (!this.worldObj.isRemote) {
			//consider starting to burn another fuel item
			if (this.burnTime == 0) {
				this.itemBurnTime = getItemBurnTime(this.inventory[0]);
				this.burnTime = this.itemBurnTime;
				if (this.burnTime > 0) {
					flag1 = true;
					if (this.inventory[0] != null) {
						this.inventory[0].stackSize--;
						if (this.inventory[0].stackSize == 0) {
							Item var3 = this.inventory[0].getItem().getContainerItem(); //hope i got import for Item right
							this.inventory[0] = (var3 == null)?null:new ItemStack(var3);
						}
					}
				}
			}
			
			if (this.isBurning()) {
				this.cookTime++;

				if (this.cookTime == 200) {
					this.cookTime = 0;
					this.onCooked();
					flag1 = true;
				}
			} else {
				this.cookTime = 0;
			}
			
			if (wasBurning != this.burnTime > 0) {
				flag1 = true;
				FortressGenerator.updateBlockState(this.burnTime > 0, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
			}
		} // end if (!isRemote)
		
		if (flag1) {
			this.markDirty();
		}
	}
	
	private void onCooked() {
		//TODO: add chance of getting dark stone dust
	}

	private static int getItemBurnTime(ItemStack itemStack) {
		if (itemStack != null) {
			int itemId = Item.getIdFromItem(itemStack.getItem());

			if (itemId == Item.getIdFromItem(Items.glowstone_dust)) {
				return 100; //TODO: replace with "return 1000*60*60; //1 hour"
			} else {
				return TileEntityFurnace.getItemBurnTime(itemStack);
			}
		}
		
		return 0;
	}
	
	/*
	public static boolean isItemFuel(ItemStack itemStack)
	{
		 return getItemBurnTime(itemStack) > 0;
	}
	//*/
	
	@Override
	public int getSizeInventory() {
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return inventory[i];
	}

	@Override
	public ItemStack decrStackSize(int slot, int count) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if (stack.stackSize <= count) {
				setInventorySlotContents(slot, null);
			} else {
				stack = stack.splitStack(count);
				if (stack.stackSize == 0) {
					setInventorySlotContents(slot, null);
				}
			}
		}
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		ItemStack itemstack = getStackInSlot(slot);
		setInventorySlotContents(slot, null);
		return itemstack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack) {
		inventory[slot] = itemstack;

		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
			itemstack.stackSize = getInventoryStackLimit();
			//TODO: consider: it seems like setting itemstack.stackSize = 1 would mean the items that didn't fit would disappear
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		
		NBTTagList list = new NBTTagList();
		
		for (int i = 0; i < getSizeInventory(); i++) {
			ItemStack itemstack = getStackInSlot(i);
			
			if (itemstack != null) {
				NBTTagCompound item = new NBTTagCompound();
	
				item.setByte("SlotFortressGenerator", (byte) i);
				itemstack.writeToNBT(item);
				list.appendTag(item);
			}
		}
		
		compound.setTag("ItemsFortressGenerator", list);
		
		//compound.setInteger("FrontDirectionFortressGenerator", (int)front);
		compound.setInteger("BurnTimeFortressGenerator", burnTime);
		compound.setInteger("CookTimeFortressGenerator", cookTime);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		NBTTagList list = compound.getTagList("ItemsFortressGenerator", NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound item = list.getCompoundTagAt(i);
			int slot = item.getByte("SlotFortressGenerator");
			
			if (slot >= 0 && slot < getSizeInventory()) {
				setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
			}
		}
		
		//front = compound.getInteger("FrontDirectionFortressGenerator");
		this.burnTime = compound.getInteger("BurnTimeFortressGenerator");
		this.cookTime = compound.getInteger("CookTimeFortressGenerator");
		this.itemBurnTime = getItemBurnTime(this.inventory[0]);
	}
	
	@Override
	public String getInventoryName() {
		return "Fortress Generator";
	}

	@Override
	public boolean hasCustomInventoryName() {  //assuming this is isInvNameLocalized method from tutorial
		return true;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		if (worldObj.getTileEntity(xCoord, yCoord, zCoord) != this) {
			return false;
		}
		return player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		return true;
	}

	public boolean isBurning() {
		return this.burnTime > 0;
	}

	public int getBurnTimeRemainingScaled(int max) {
		if (this.itemBurnTime == 0) {
			this.itemBurnTime = 200;
		}
		return (this.burnTime * max) / this.itemBurnTime;
	}

	public int getCookProgressScaled(int max) {
		return (this.cookTime * max) / 200;
	}

	/*
	public boolean isActive() {
		return this.isActive;
	}
	//*/
}