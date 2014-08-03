package com.newyith.fortressmod;

import java.util.Random;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class FortressGenerator extends BlockContainer {
	private Random rand = new Random();

    protected IIcon frontIcon;
    protected IIcon topIcon;

	private static boolean keepInventory = false;

	protected FortressGenerator() {
		super(Material.rock);
		setHardness(3.5F);
		setStepSound(Block.soundTypePiston);
		setResistance(17.5F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityFortressGenerator();
	}
	
	/**
     * Called upon block activation (right click on the block.)
     */
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
    	if(!world.isRemote) {
    		player.openGui(FortressMod.modInstance, 0, world, x, y, z);
    	}
    	return true;
    }
	
    @Override
	public void breakBlock(World world, int x, int y, int z, Block oldblock, int oldMetadata) {
		if (!keepInventory) {
        	TileEntityFortressGenerator fgTile = (TileEntityFortressGenerator) world.getTileEntity(x, y, z);
        	if (fgTile != null) {
            	for (int i = 0; i < fgTile.getSizeInventory(); i++) {
                	ItemStack itemstack = fgTile.getStackInSlot(i);
                	if(itemstack != null) {
    					float f = this.rand.nextFloat() * 0.8F + 0.1F;
    					float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
    					float f2 = this.rand.nextFloat() * 0.8F + 0.1F;

    					while (itemstack.stackSize > 0) {
    						int j = this.rand.nextInt(21) + 10;

    						if (j > itemstack.stackSize) {
    							j = itemstack.stackSize;
    						}

    						itemstack.stackSize -= j;

    						EntityItem item = new EntityItem(world, (double)((float)x + f), (double)((float)y + f1), (double)((float)z + f2), new ItemStack(itemstack.getItem(), j, itemstack.getItemDamage()));

    						if (itemstack.hasTagCompound()) {
    							item.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
    						}

    						world.spawnEntityInWorld(item);
    					}
    				}
    			}

    			world.func_147453_f(x, y, z, oldblock);
        	}
    	}

    	super.breakBlock(world, x, y, z, oldblock, oldMetadata);
    }
	
	/**
     * Gets the block's texture. Args: side, meta
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
    	if (side == 1 || side == 0) { //top or bottom
    		return this.topIcon;
    	} else {
    		if (side == meta) { //front
    			return this.frontIcon;
    		} else { //side
    			return this.blockIcon;
    		}
    	}
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister)
    {
    	String iconStr = ModInfo.MODID + ":" + "fortress_generator_block";
        this.blockIcon = iconRegister.registerIcon(iconStr);
        //this.frontIcon = p_149651_1_.registerIcon(this.field_149932_b (isBurning?) ? "furnace_front_on" : "furnace_front_off");
        this.frontIcon = iconRegister.registerIcon(iconStr);
        this.topIcon = iconRegister.registerIcon(iconStr);
    }

	public static void updateBlockState(boolean isBurning, World world, int x, int y, int z) {
		    int meta = world.getBlockMetadata(x, y, z);
		    TileEntity tileentity = world.getTileEntity(x, y, z);
		    keepInventory = true;
		    if (isBurning)
		    {
		    	world.setBlock(x, y, z, FortressMod.fortressGenerator); //TODO: change this to FortressMod.fortressGeneratorOn
		    }
		    else
		    {
		    	world.setBlock(x, y, z, FortressMod.fortressGenerator);
		    }
		    keepInventory = false;
		    world.setBlockMetadataWithNotify(x, y, z, meta, 2);
		    if (tileentity != null)
		    {
			    tileentity.validate();
			    world.setTileEntity(x, y, z, tileentity);
		    }
	}
    
}
