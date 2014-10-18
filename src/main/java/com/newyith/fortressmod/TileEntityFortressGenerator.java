package com.newyith.fortressmod;

import java.util.Date;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import com.newyith.fortressmod.blocks.FortressGenerator;

public class TileEntityFortressGenerator extends TileEntity implements IInventory {
	private static Random rand = new Random();
	private ItemStack[] inventory;

	/** The number of ticks (50ms each) that the fortress generator will keep burning */
	public int burnTicksRemaining;

	private static final int glowstoneDustBurnTicks = (FortressMod.config_glowstoneBurnTimeMs)/50; //50 ms per updateEntity() call (tick)
	
	private FortressGeneratorState state;
	private boolean updateBlockStateFlag;
	
	private GeneratorCore generatorCore = null; //public so it's static methods can get the generatorCore instance via fortress generator's tile entity

	//debug
	public int uniqueId;
	private long lastUpdateEntity = 0;
	private static int nextUniqueId = 0;
	
	//-----------------------
	
	//must have a no arguments constructor (or we get runtime exceptions)
	public TileEntityFortressGenerator() {
		this.uniqueId = this.nextUniqueId++;
		//Dbg.print("new TileEntityFortressGenerator() uniqueId: " + this.uniqueId);
		
		this.inventory = new ItemStack[1];
		this.burnTicksRemaining = 0;
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
		boolean wasBurning = this.burnTicksRemaining > 0;
		
		long now = new Date().getTime();
		long duration = now - this.lastUpdateEntity;
		this.lastUpdateEntity = now;
		//Dbg.print("burnTime--; " + String.valueOf(this.uniqueId) + " duration: " + String.valueOf(duration), this.worldObj.isRemote);
		
		if (this.burnTicksRemaining > 0 && !this.isPaused()) {
			this.burnTicksRemaining--;
		}
		
		if (!this.isClogged()) {
			boolean needToMarkAsDirty = false;
			
			if (!this.worldObj.isRemote) {
				//consider starting to burn another fuel item
				if (this.burnTicksRemaining == 0) {
					this.burnTicksRemaining = getItemBurnTicks(this.inventory[0]);
					if (this.burnTicksRemaining > 0) {
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
					
					//just started burning so check if it should be paused
					if (this.isBurning()) {
						int x = this.xCoord;
						int y = this.yCoord;
						int z = this.zCoord;
						if (this.worldObj.isBlockIndirectlyGettingPowered(x, y, z)) {
							this.setState(FortressGeneratorState.PAUSED);
						}
					}
					
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
	
	private static int getItemBurnTicks(ItemStack itemStack) {
		if (itemStack != null) {
			int itemId = Item.getIdFromItem(itemStack.getItem());

			if (itemId == Item.getIdFromItem(Items.glowstone_dust)) {
				return glowstoneDustBurnTicks;
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
		compound.setString("state", this.state.name());
		compound.setInteger("burnTicksRemaining", burnTicksRemaining);
		
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
		
		String stateStr = compound.getString("state");
		this.state = FortressGeneratorState.valueOf(stateStr);
		this.burnTicksRemaining = compound.getInteger("burnTicksRemaining");
		
		//in case config_glowstoneBurnTimeMs has changed, make sure remainingTicks <= maxTicks
		if (this.burnTicksRemaining > this.glowstoneDustBurnTicks) {
			this.burnTicksRemaining = this.glowstoneDustBurnTicks;
		}
		
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

	public boolean isBurning() {
		return this.burnTicksRemaining > 0;
	}

	public int getBurnTimeRemainingScaled(int max) {
		//(remaining * max) / total
		long remaining = this.burnTicksRemaining; //TODO: fix: burnTicksRemaining can be as high as 6243 when total is 720 (on server)
		long scaleMax = max;
		long total = this.glowstoneDustBurnTicks;
		Dbg.print("remaining: " + String.valueOf(remaining));
		Dbg.print("total: " + String.valueOf(total));
		return (int) (remaining * scaleMax / total);
		//return (this.burnTicksRemaining * max) / this.glowstoneDustBurnTicks;
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
			//Dbg.print("tile.setState() to " + state.name(), this.getWorldObj().isRemote);
			
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
			
			//TODO: finish or comment out this sounds stuff
			/*
			Dbg.print("play sound");
			this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "fortressmod:test_sound", 1.0F, 0.6F);
			this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "test_sound", 1.0F, 0.6F);
			//*/
			
			this.setState(FortressGeneratorState.PAUSED);
		}
		
		//if (paused and !powered) unpause
		if (this.state == FortressGeneratorState.PAUSED && !nowPowered) {
			if (this.isBurning())
				this.setState(FortressGeneratorState.ACTIVE);
			else
				this.setState(FortressGeneratorState.OFF);
		}
		
		this.generatorCore.onPoweredMightHaveChanged();
	}
	
	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot.
	 */
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
		return getItemBurnTicks(itemStack) > 0;
	}

	/**
	 * Returns an array containing the indices of the slots that can be accessed by automation on the given side of this
	 * block.
	 */
	public int[] getAccessibleSlotsFromSide(int side) {
		return new int[] {0};
	}

	/**
	 * Returns true if automation can insert the given item in the given slot from the given side. Args: Slot, item,
	 * side
	 */
	public boolean canInsertItem(int slot, ItemStack itemStack, int side) {
		return this.isItemValidForSlot(slot, itemStack);
	}

	/**
	 * Returns true if automation can extract the given item in the given slot from the given side. Args: Slot, item,
	 * side
	 */
	public boolean canExtractItem(int slot, ItemStack itemStack, int side) {
		return side == 1; //can only extract from the bottom
	}
}