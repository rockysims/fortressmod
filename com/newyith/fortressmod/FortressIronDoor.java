package com.newyith.fortressmod;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.IconFlipped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class FortressIronDoor extends BlockDoor {

	private IIcon[] upperDoorIcon;
	private IIcon[] lowerDoorIcon;
	private IIcon[] field_150017_a;
	private IIcon[] field_150016_b;

	protected FortressIronDoor() {
		super(Material.wood); //not Material.iron so that it will open close on right click and not wood so it can't burn
		//setBlockUnbreakable();
		//setResistance(6000000.0F);
		//disableStats();
	}
	
	public Item getItemDropped(int par1, Random par2, int par3) {
		return FortressMod.itemFortressDoor;
    }
	
	
	/**
     * Gets the block's texture. Args: side, meta
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int p_149691_1_, int p_149691_2_)
    {
        return this.field_150016_b[0];
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess p_149673_1_, int p_149673_2_, int p_149673_3_, int p_149673_4_, int p_149673_5_)
    {
        if (p_149673_5_ != 1 && p_149673_5_ != 0)
        {
            int i1 = this.func_150012_g(p_149673_1_, p_149673_2_, p_149673_3_, p_149673_4_);
            int j1 = i1 & 3;
            boolean flag = (i1 & 4) != 0;
            boolean flag1 = false;
            boolean flag2 = (i1 & 8) != 0;

            if (flag)
            {
                if (j1 == 0 && p_149673_5_ == 2)
                {
                    flag1 = !flag1;
                }
                else if (j1 == 1 && p_149673_5_ == 5)
                {
                    flag1 = !flag1;
                }
                else if (j1 == 2 && p_149673_5_ == 3)
                {
                    flag1 = !flag1;
                }
                else if (j1 == 3 && p_149673_5_ == 4)
                {
                    flag1 = !flag1;
                }
            }
            else
            {
                if (j1 == 0 && p_149673_5_ == 5)
                {
                    flag1 = !flag1;
                }
                else if (j1 == 1 && p_149673_5_ == 3)
                {
                    flag1 = !flag1;
                }
                else if (j1 == 2 && p_149673_5_ == 4)
                {
                    flag1 = !flag1;
                }
                else if (j1 == 3 && p_149673_5_ == 2)
                {
                    flag1 = !flag1;
                }

                if ((i1 & 16) != 0)
                {
                    flag1 = !flag1;
                }
            }

            return flag2 ? this.field_150017_a[flag1?1:0] : this.field_150016_b[flag1?1:0];
        }
        else
        {
            return this.field_150016_b[0];
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister p_149651_1_)
    {
        this.field_150017_a = new IIcon[2];
        this.field_150016_b = new IIcon[2];
        this.field_150017_a[0] = p_149651_1_.registerIcon(this.getTextureName() + "_upper");
        this.field_150016_b[0] = p_149651_1_.registerIcon(this.getTextureName() + "_lower");
        this.field_150017_a[1] = new IconFlipped(this.field_150017_a[0], true, false);
        this.field_150016_b[1] = new IconFlipped(this.field_150016_b[0], true, false);
    }
	
	
	
	
	


//    /**
//     * Gets the block's texture. Args: side, meta
//     */
//    @SideOnly(Side.CLIENT)
//    public IIcon getIcon(int p_149691_1_, int p_149691_2_)
//    {
//    	Dbg.print("getIcon(2 params)");
//        return this.lowerDoorIcon[0];
//    }
//
//    @SideOnly(Side.CLIENT)
//    public IIcon getIcon(IBlockAccess p_149673_1_, int p_149673_2_, int p_149673_3_, int p_149673_4_, int p_149673_5_)
//    {
//    	Dbg.print("getIcon(5 params)");
//        if (p_149673_5_ != 1 && p_149673_5_ != 0)
//        {
//            int i1 = this.func_150012_g(p_149673_1_, p_149673_2_, p_149673_3_, p_149673_4_);
//            int j1 = i1 & 3;
//            boolean flag = (i1 & 4) != 0;
//            boolean flag1 = false;
//            boolean flag2 = (i1 & 8) != 0;
//
//            if (flag)
//            {
//                if (j1 == 0 && p_149673_5_ == 2)
//                {
//                    flag1 = !flag1;
//                }
//                else if (j1 == 1 && p_149673_5_ == 5)
//                {
//                    flag1 = !flag1;
//                }
//                else if (j1 == 2 && p_149673_5_ == 3)
//                {
//                    flag1 = !flag1;
//                }
//                else if (j1 == 3 && p_149673_5_ == 4)
//                {
//                    flag1 = !flag1;
//                }
//            }
//            else
//            {
//                if (j1 == 0 && p_149673_5_ == 5)
//                {
//                    flag1 = !flag1;
//                }
//                else if (j1 == 1 && p_149673_5_ == 3)
//                {
//                    flag1 = !flag1;
//                }
//                else if (j1 == 2 && p_149673_5_ == 4)
//                {
//                    flag1 = !flag1;
//                }
//                else if (j1 == 3 && p_149673_5_ == 2)
//                {
//                    flag1 = !flag1;
//                }
//
//                if ((i1 & 16) != 0)
//                {
//                    flag1 = !flag1;
//                }
//            }
//
//            return flag2 ? this.upperDoorIcon[flag1?1:0] : this.lowerDoorIcon[flag1?1:0];
//        }
//        else
//        {
//            return this.lowerDoorIcon[0];
//        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    public void registerBlockIcons(IIconRegister iconRegister)
//    {
//    	String iconStr = ModInfo.MODID.toLowerCase() + ":" + "fortress_door";
//        
//        this.upperDoorIcon = new IIcon[2]; //index 0 means front and 1 means back?
//        this.lowerDoorIcon = new IIcon[2]; //index 0 means front and 1 means back?
//        this.upperDoorIcon[0] = iconRegister.registerIcon(iconStr + "_upper");
//        this.lowerDoorIcon[0] = iconRegister.registerIcon(iconStr + "_lower");
//        this.upperDoorIcon[1] = new IconFlipped(this.upperDoorIcon[0], true, false);
//        this.lowerDoorIcon[1] = new IconFlipped(this.lowerDoorIcon[0], true, false);
//    }
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    
//    public AdobeDoor(int par1, Material par2Material) {
//		super(par1, par2Material);
//		float f = 0.5F;
//		float f1 = 1.0F;
//		this.setLightOpacity(0);
//		this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f1, 0.5F + f);
//	}
//
//	public Icon getBlockTexture(IBlockAccess par1IBlockAccess, int par2,
//			int par3, int par4, int par5) {
//		if (par5 == 1 || par5 == 0) {
//			return this.blockIcon;
//		}
//		int meta = getFullMetadata(par1IBlockAccess, par2, par3, par4);
//		boolean flag = (meta & 4) != 0;
//		int halfMeta = meta & 3;
//		boolean flipped = false;
//
//		if (flag) {
//			if (halfMeta == 0 && par5 == 2)
//				flipped = !flipped;
//			else if (halfMeta == 1 && par5 == 5)
//				flipped = !flipped;
//			else if (halfMeta == 2 && par5 == 3)
//				flipped = !flipped;
//			else if (halfMeta == 3 && par5 == 4)
//				flipped = !flipped;
//		} else {
//			if (halfMeta == 0 && par5 == 5)
//				flipped = !flipped;
//			else if (halfMeta == 1 && par5 == 3)
//				flipped = !flipped;
//			else if (halfMeta == 2 && par5 == 4)
//				flipped = !flipped;
//			else if (halfMeta == 3 && par5 == 2)
//				flipped = !flipped;
//			if ((meta & 16) != 0)
//				flipped = !flipped;
//		}
//
//		if (flipped)
//			return flippedIcons[(meta & 8) != 0 ? 1 : 0];
//		else
//			return (meta & 8) != 0 ? this.topDoorIcon : this.blockIcon;
//	}
//
//	public Icon getIcon(int par1, int par2) {
//		return this.blockIcon;
//	}
//
//	@Override
//	public void registerIcons(IconRegister iconRegister) {
//		this.blockIcon = iconRegister.registerIcon(AdobeInfo.NAME.toLowerCase()
//				+ ":adobe_door_lower");
//		this.topDoorIcon = iconRegister.registerIcon(AdobeInfo.NAME
//				.toLowerCase() + ":adobe_door_upper");
//		this.flippedIcons[0] = new IconFlipped(blockIcon, true, false);
//		this.flippedIcons[1] = new IconFlipped(topDoorIcon, true, false);
//	}
//    
    
    
    
    
    
    
    
    
}
