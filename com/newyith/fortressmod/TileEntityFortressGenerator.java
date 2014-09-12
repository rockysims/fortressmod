package com.newyith.fortressmod;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Random;

import akka.event.Logging.Debug;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityFortressGenerator extends TileEntity implements IInventory {
	private static Random rand = new Random();
	private ItemStack[] inventory;

	/** The number of ticks that the fortress generator will keep burning */
	public int burnTime; //remaining burn time

	/** The number of ticks that a fresh copy of the currently-burning item would keep the furnace burning for */
	public int itemBurnTime;
	private static final int burnPeriod = 100; //TODO: replace with "(1000*60*60)/50; //1 hour"
	
	private FortressGeneratorState state;
	private boolean updateBlockStateFlag;
	
	private GeneratorCore generatorCore = null; //public so it's static methods can get the generatorCore instance via fortress generator's tile entity

	//debug
	public int uniqueId;
	private long lastUpdateEntity = 0;
	private static int nextUniqueId = 0;
	
	//-----------------------
	
	public TileEntityFortressGenerator() {
		this.uniqueId = this.nextUniqueId++;
		//Dbg.print("new TileEntityFortressGenerator() uniqueId: " + this.uniqueId);
		
		this.inventory = new ItemStack[1];
		this.burnTime = 0;
		this.itemBurnTime = 0;
		this.state = FortressGeneratorState.OFF;
		this.generatorCore = new GeneratorCore(this);
	}
	
	public TileEntityFortressGenerator(FortressGeneratorState state) {
		this();
		this.state = state;
	}

	@Override
	public void setWorldObj(World world) {
		super.setWorldObj(world);
		this.generatorCore.setWorldObj(world);
	}
	
	public GeneratorCore getGeneratorCore() {
		return this.generatorCore;
	}
	
	@Override
	public void updateEntity() {
		boolean wasBurning = this.burnTime > 0;
		
		long now = new Date().getTime();
		long duration = now - this.lastUpdateEntity;
		this.lastUpdateEntity = now;
		//Dbg.print("burnTime--; " + String.valueOf(this.uniqueId) + " duration: " + String.valueOf(duration), this.worldObj.isRemote);
		
		if (this.burnTime > 0 && !this.isPaused()) {
			this.burnTime--;
		}
		
		if (!this.isClogged()) {
			boolean needToMarkAsDirty = false;
			
			if (!this.worldObj.isRemote) {
				//consider starting to burn another fuel item
				if (this.burnTime == 0) {
					this.itemBurnTime = getItemBurnTime(this.inventory[0]);
					this.burnTime = this.itemBurnTime;
					if (this.burnTime > 0) {
						needToMarkAsDirty = true;
						if (this.inventory[0] != null) {
							this.inventory[0].stackSize--;
							if (this.inventory[0].stackSize == 0) {
								Item var3 = this.inventory[0].getItem().getContainerItem();
								this.inventory[0] = (var3 == null)?null:new ItemStack(var3);
							}
						}
					}
				}
				
				if (wasBurning != this.isBurning()) {
					needToMarkAsDirty = true;
					this.updateBlockStateFlag = true;
					if (!this.isPaused() && !this.isClogged()) {
						if (this.isBurning())
							this.setState(FortressGeneratorState.ACTIVE);
						else
							this.setState(FortressGeneratorState.OFF);
					}
					generatorCore.onBurnStateChanged();
				}
			} // end if (!isRemote)
			
			if (needToMarkAsDirty) {
				this.markDirty();
			}
		} // end if (!isClogged)
		
		if (this.updateBlockStateFlag) {
			this.updateBlockStateFlag = false;
			FortressGenerator.updateBlockState(this);
		}
		
		this.generatorCore.updateEntity();
	}
	
	private static int getItemBurnTime(ItemStack itemStack) {
		if (itemStack != null) {
			int itemId = Item.getIdFromItem(itemStack.getItem());

			if (itemId == Item.getIdFromItem(Items.glowstone_dust)) {
				return burnPeriod;
			} else {
				return TileEntityFurnace.getItemBurnTime(itemStack);
			}
		}
		
		return 0;
	}
	
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
		compound.setString("stateFortressGenerator", this.state.name());
		compound.setInteger("BurnTimeFortressGenerator", burnTime);
		
		this.generatorCore.writeToNBT(compound);
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
		
		String stateStr = compound.getString("stateFortressGenerator");
		this.state = FortressGeneratorState.valueOf(stateStr);
		this.burnTime = compound.getInteger("BurnTimeFortressGenerator");
		this.itemBurnTime = getItemBurnTime(this.inventory[0]);
		
		this.generatorCore.readFromNBT(compound);
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
			this.itemBurnTime = burnPeriod;
		}
		return (this.burnTime * max) / this.itemBurnTime;
	}

	public boolean isClogged() {
		return this.state == FortressGeneratorState.CLOGGED;
	}
	
	public boolean isPaused() {
		return this.state == FortressGeneratorState.PAUSED;
	}
	
	public boolean isActive() {
		return this.state == FortressGeneratorState.ACTIVE;
	}
	
	public void setState(FortressGeneratorState state) {
		if (this.state != state) {
			this.state = state;
			Dbg.print("tile.setState() to " + state.name(), this.getWorldObj().isRemote);
			
			this.updateBlockStateFlag = true; //tells updateEntity() to call FortressGenerator.updateBlockState() when next executed
		}
	}
	
	public FortressGeneratorState getState() {
		return this.state;
	}

	public void onNeighborBlockChange(World world, int x, int y, int z) {
		boolean nowPowered = world.isBlockIndirectlyGettingPowered(x, y, z);

		//if (running and powered) pause
		if (this.state == FortressGeneratorState.ACTIVE && nowPowered) {
			this.setState(FortressGeneratorState.PAUSED);
		}
		
		//if (paused and !powered) unpause
		if (this.state == FortressGeneratorState.PAUSED && !nowPowered) {
			if (this.isBurning())
				this.setState(FortressGeneratorState.ACTIVE);
			else
				this.setState(FortressGeneratorState.OFF);
		}
		
		this.getGeneratorCore().onPoweredMightHaveChanged();
	}
}