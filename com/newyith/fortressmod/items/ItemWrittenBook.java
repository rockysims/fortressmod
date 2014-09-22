package com.newyith.fortressmod.items;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemWrittenBook extends ItemEditableBook {
	
	private List<String> pages;

	public void setPages(List<String> pages) {
		this.pages = pages;
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
		ItemStack book = new ItemStack(Items.written_book);
		
		//create pages
		NBTTagList pagesTag = new NBTTagList();
		for (String pageText : this.pages) {
			pagesTag.appendTag(new NBTTagString(pageText));
		}
		
		//save pages to book
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("pages", pagesTag);
		book.setTagCompound(compound);
		
		player.displayGUIBook(book);
		
		return itemStack;
    }
	
	@SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack p_77636_1_) {
        return false;
    }

	/*
	@Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer par2, List list, boolean par4)
    {
        String s = "the string s here";
    	list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("book.byAuthor", new Object[] {s}));
    }
	//*/
}
