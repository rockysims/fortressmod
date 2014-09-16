package com.newyith.fortressmod;

import java.util.Random;

import javax.management.RuntimeErrorException;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
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
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class FortressGenerator extends BlockContainer {
	private Random rand = new Random();

    protected IIcon frontIcon;
    protected IIcon topIcon;
    protected IIcon frontIconClogged;
    protected IIcon frontIconPaused;

    private final FortressGeneratorState state;

	private static boolean ignoreBreakBlock = false;

	public FortressGenerator(FortressGeneratorState state) {
		super(Material.rock);
		setHardness(8.0F);
		setStepSound(Block.soundTypePiston);
		setResistance(17.5F);
		this.state = state;
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		
		TileEntityFortressGenerator fg = (TileEntityFortressGenerator)world.getTileEntity(x, y, z);
		fg.onNeighborBlockChange(world, x, y, z);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityFortressGenerator(this.state);
	}
	
	public Item getItemDropped(int par1, Random par2, int par3) {
        return Item.getItemFromBlock(FortressMod.fortressGenerator);
    }
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		setDefaultDirection(world, x, y, z);
	}

	private void setDefaultDirection(World world, int x, int y, int z) {
		if (!world.isRemote) {
			boolean zNegIsOpaque = world.getBlock(x, y, z - 1).isOpaqueCube();
			boolean zPosIsOpaque = world.getBlock(x, y, z + 1).isOpaqueCube();
			boolean xNegIsOpaque = world.getBlock(x - 1, y, z).isOpaqueCube();
			boolean xPosIsOpaque = world.getBlock(x + 1, y, z).isOpaqueCube();
			byte meta = 3;
			
			if (xNegIsOpaque && !xPosIsOpaque) meta = 5;
			if (xPosIsOpaque && !xNegIsOpaque) meta = 4;
			if (zNegIsOpaque && !zPosIsOpaque) meta = 3;
			if (zPosIsOpaque && !zNegIsOpaque) meta = 2;
			
			world.setBlockMetadataWithNotify(x, y, z, meta, 2);
		}
	}
	
	/** Called when the block is placed in the world. */
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		int d = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		
		if (d == 0) world.setBlockMetadataWithNotify(x, y, z, 2, 2);
		if (d == 1) world.setBlockMetadataWithNotify(x, y, z, 5, 2);
		if (d == 2) world.setBlockMetadataWithNotify(x, y, z, 3, 2);
		if (d == 3) world.setBlockMetadataWithNotify(x, y, z, 4, 2);
		
		//set placingPlayerName 
		String placingPlayerName = "notPlacedByPlayer";
		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;
			placingPlayerName = player.getGameProfile().getName();
		}
		
		GeneratorCore.onPlaced(world, x, y, z, placingPlayerName);
	}
	
	/** Called upon block activation (right click on the block.) */
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
    	
    	TileEntityFortressGenerator fg = (TileEntityFortressGenerator) world.getTileEntity(x, y, z);
    	
    	if(!world.isRemote) {
    		player.openGui(FortressMod.modInstance, 0, world, x, y, z);
    	}
    	return true;
    }
	
    @Override
	public void breakBlock(World world, int x, int y, int z, Block oldblock, int oldMetadata) {
		if (!ignoreBreakBlock) {
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
        	
           	GeneratorCore.onBroken(world, x, y, z); //must be before super.breakBlock()
    	}

    	super.breakBlock(world, x, y, z, oldblock, oldMetadata);
    }

    //synchronized for this.ignoreBreakBlock
	public static synchronized void updateBlockState(TileEntityFortressGenerator fg) {
		Dbg.print("updateBlockState(fg)");
		World world = fg.getWorldObj();
		int x = fg.xCoord;
		int y = fg.yCoord;
		int z = fg.zCoord;

	    int meta = world.getBlockMetadata(x, y, z);
	    TileEntity tileentity = world.getTileEntity(x, y, z);
	    ignoreBreakBlock = true;
	    if (fg.isClogged()) {
	    	world.setBlock(x, y, z, FortressMod.fortressGeneratorClogged);
			Dbg.print("updateBlockState(fg) clog");
	    } else if (fg.isPaused()) {
	    	world.setBlock(x, y, z, FortressMod.fortressGeneratorPaused);
			Dbg.print("updateBlockState(fg) pause");
	    } else if (fg.isActive()) {
	    	world.setBlock(x, y, z, FortressMod.fortressGeneratorOn);
			Dbg.print("updateBlockState(fg) on");
		} else {
	    	world.setBlock(x, y, z, FortressMod.fortressGenerator);
			Dbg.print("updateBlockState(fg) off");
		}
	    ignoreBreakBlock = false;
	    world.setBlockMetadataWithNotify(x, y, z, meta, 2);
	    if (tileentity != null) {
	    	tileentity.validate();
		    world.setTileEntity(x, y, z, tileentity);
	    }
		fg.markDirty();
	}
	
	/**
     * Gets the block's texture. Args: side, meta
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
    	if (side == 1) { //top
    		return this.topIcon;
    	} else {
    		if (side == meta) { //front
    			return this.frontIcon;
    		} else { //side or bottom
    			return this.blockIcon;
    		}
    	}
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister)
    {
    	String iconStr = ModInfo.MODID.toLowerCase() + ":" + "fortress_generator_block";
        this.blockIcon = iconRegister.registerIcon(iconStr);
        this.topIcon = iconRegister.registerIcon(iconStr + "_top");
        
        String frontSuffix;
        if (this.state == FortressGeneratorState.CLOGGED)
        	frontSuffix = "_front_clogged";
        else if (this.state == FortressGeneratorState.PAUSED)
        	frontSuffix = "_front_paused";
        else if (this.state == FortressGeneratorState.ACTIVE)
        	frontSuffix = "_front_on";
        else
        	frontSuffix = "_front_off";
        
    	this.frontIcon = iconRegister.registerIcon(iconStr + frontSuffix);
    }

    /**
     * A randomly called display update to be able to add particles or other items for display
     */
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int xParam, int yParam, int zParam, Random random) {
    	if (this.state == FortressGeneratorState.ACTIVE) {
        	if (random.nextFloat() * 100.0F < 6.0F) {
                int meta = world.getBlockMetadata(xParam, yParam, zParam);
                float x = (float)xParam + 0.5F;
                float y = (float)yParam + 0.5F + random.nextFloat() * 2.0F / 16.0F;
                float z = (float)zParam + 0.5F;
                float f3 = 0.52F;
                float f4 = random.nextFloat() * 0.2F - 0.1F;

                if (meta == 4) { //0100
                	spawnGeneratorSpark(world, (double)(x - f3), (double)y, (double)(z + f4), random);
                } else if (meta == 5) { //0101
                	spawnGeneratorSpark(world, (double)(x + f3), (double)y, (double)(z - f4), random);
                } else if (meta == 2) { //0010
                	spawnGeneratorSpark(world, (double)(x + f4), (double)y, (double)(z - f3), random);
                } else if (meta == 3) { //0011
                	spawnGeneratorSpark(world, (double)(x - f4), (double)y, (double)(z + f3), random);
                }
        	}
        }
    }
    private void spawnGeneratorSpark(World world, double x, double y, double z, Random random) {
    	world.spawnParticle("smoke", x, y, z, 0.0D, 0.0D, 0.0D);
    	if (random.nextFloat() * 100.0F < 50.0F) {
    		world.spawnParticle("flame", x, y, z, 0.0D, 0.0D, 0.0D);
    	}
    	if (random.nextFloat() * 100.0F < 10.0F) {
    		world.spawnParticle("dripLava", x + 0.1F, y - 0.1F, z, 0.0D, 0.0D, 0.0D);
    	}
    }
}
