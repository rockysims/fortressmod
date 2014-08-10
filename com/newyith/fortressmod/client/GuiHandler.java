package com.newyith.fortressmod.client;

import com.newyith.fortressmod.Dbg;
import com.newyith.fortressmod.FortressMod;
import com.newyith.fortressmod.TileEntityFortressGenerator;
import com.newyith.fortressmod.client.container.ContainerFortressGenerator;
import com.newyith.fortressmod.client.gui.GuiFortressGenerator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;

public class GuiHandler implements IGuiHandler{

	public GuiHandler() {
		NetworkRegistry.INSTANCE.registerGuiHandler(FortressMod.modInstance, this);
	}
	
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity entity = world.getTileEntity(x, y, z);

		switch(id) {
			case 0:
				if (entity != null && entity instanceof TileEntityFortressGenerator) {
					return new ContainerFortressGenerator(player.inventory, (TileEntityFortressGenerator) entity);
				} else {
					return null;
				}
			default:
				return null;
		}
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity entity = world.getTileEntity(x, y, z);

		switch(id) {
		case 0:
			if(entity != null && entity instanceof TileEntityFortressGenerator) {
				return new GuiFortressGenerator(player.inventory, (TileEntityFortressGenerator) entity);
			} else {
				return null;
			}
		default:
			return null;
		}
	}
	
}
