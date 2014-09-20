package com.newyith.fortressmod.items;

import com.newyith.fortressmod.Dbg;
import com.newyith.fortressmod.ModInfo;

import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.world.World;

//TODO: delete next 2 lines
//ItemWritableBook
//ItemEditableBook
public class ItemFortressManual extends ItemEditableBook {
	
	public ItemFortressManual() {
		setUnlocalizedName("FortressManual");
		setTextureName(ModInfo.MODID.toLowerCase() + ":" + "fortress_manual");
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		Dbg.print("onManualRightClicked");
		player.displayGUIBook(new ItemStack(Items.written_book));
		/*
		public void displayGUIBook(ItemStack p_71048_1_)
	    {
	        Item item = p_71048_1_.getItem();

	        if (item == Items.written_book)
	        {
	            this.mc.displayGuiScreen(new GuiScreenBook(this, p_71048_1_, false));
	        }
	        else if (item == Items.writable_book)
	        {
	            this.mc.displayGuiScreen(new GuiScreenBook(this, p_71048_1_, true));
	        }
	    }
	    */
		return itemStack;
    }
	

}
