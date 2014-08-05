package com.newyith.fortressmod;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;

public class FortressBedrock extends Block {
	protected FortressBedrock() {
		super(Material.rock);
		setHardness(-1);
	}

	@SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
		return Blocks.bedrock.getIcon(side, meta);
    }
}
