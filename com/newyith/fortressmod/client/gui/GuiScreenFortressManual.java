package com.newyith.fortressmod.client.gui;

import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public class GuiScreenFortressManual extends PublicGuiScreenBook {
	public GuiScreenFortressManual(EntityPlayer player, ItemStack itemStack, boolean isUnsigned) {
		super(player, itemStack, isUnsigned);
		/* super(player, itemStack, isUnsigned):
	    this.editingPlayer = player;
	    this.bookObj = itemStack;
	    this.bookIsUnsigned = isUnsigned;

	    if (itemStack.hasTagCompound())
	    {
	        NBTTagCompound nbttagcompound = itemStack.getTagCompound();
	        this.bookPages = nbttagcompound.getTagList("pages", 8);

	        if (this.bookPages != null)
	        {
	            this.bookPages = (NBTTagList)this.bookPages.copy();
	            this.bookTotalPages = this.bookPages.tagCount();

	            if (this.bookTotalPages < 1)
	            {
	                this.bookTotalPages = 1;
	            }
	        }
	    }

	    if (this.bookPages == null && isUnsigned)
	    {
	        this.bookPages = new NBTTagList();
	        this.bookPages.appendTag(new NBTTagString(""));
	        this.bookTotalPages = 1;
	    }
	    //*/
		this.bookPages.appendTag(new NBTTagString("my new string here"));
		this.bookTotalPages++;
	}
}