package com.newyith.fortressmod.items;

import com.newyith.fortressmod.Dbg;
import com.newyith.fortressmod.FortressMod;
import com.newyith.fortressmod.ModInfo;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ItemFortressDoor extends ItemDoor {
	
	private Material origMaterial;

	public ItemFortressDoor(Material origMaterial) {
		super(Material.rock);
		setUnlocalizedName("FortressDoor");
		setTextureName(ModInfo.MODID.toLowerCase() + ":" + "fortress_door");
		this.origMaterial = origMaterial;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
		Dbg.print("ItemFortressDoor onItemUse");
		
		if (p_77648_7_ != 1)
        {
            return false;
        }
        else
        {
            ++y;
            Block block;
            if (this.origMaterial == Material.wood) {
            	block = FortressMod.fortressWoodenDoor;
            } else {
            	block = FortressMod.fortressIronDoor;
            }

            if (player.canPlayerEdit(x, y, z, p_77648_7_, stack) && player.canPlayerEdit(x, y + 1, z, p_77648_7_, stack))
            {
                if (!block.canPlaceBlockAt(world, x, y, z))
                {
                    return false;
                }
                else
                {
                    int i1 = MathHelper.floor_double((double)((player.rotationYaw + 180.0F) * 4.0F / 360.0F) - 0.5D) & 3;
                    placeDoorBlock(world, x, y, z, i1, block);
                    --stack.stackSize;
                    return true;
                }
            }
            else
            {
                return false;
            }
        }
	}
}
