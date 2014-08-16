package com.newyith.fortressmod.renderers;

import com.newyith.fortressmod.Dbg;
import com.newyith.fortressmod.FortressMod;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class FortressIronDoorRenderer implements ISimpleBlockRenderingHandler {
	private int renderId;
	
	public FortressIronDoorRenderer(int fortressIronDoorRenderId) {
		renderId = fortressIronDoorRenderId;
	}
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		Dbg.print("renderWorldBlock");

		boolean flag;
		//renderer.setRenderBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		flag = renderer.renderBlockDoor(Blocks.wooden_door, x, y, z);
		//renderer.renderBlockAllFaces(Blocks.wooden_door, x, y, z);
		//renderer.renderBlockAllFaces(Blocks.bookshelf, x, y, z);
		
		Dbg.print("flag: " + String.valueOf(flag));

		return true;
		//RenderBlocks rb = new RenderBlocks(world);
		//rb.setRenderBounds(0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F);
		//return rb.renderBlockDoor(FortressMod.fortressIronDoor, x, y, z);
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return false;
	}

	@Override
	public int getRenderId() {
		return this.renderId;
	}

}
