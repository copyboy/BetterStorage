package net.mcft.copy.betterstorage.block;

import net.mcft.copy.betterstorage.block.tileentity.TileEntityCraftingStation;
import net.mcft.copy.betterstorage.misc.Constants;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCraftingStation extends BlockContainerBetterStorage {
	
	private Icon topIcon;
	private Icon bottomIcon;
	
	public BlockCraftingStation(int id) {
		super(id, Material.wood);
		
		setHardness(1.5f);
                setStepSound(soundWoodFootstep);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		blockIcon = iconRegister.registerIcon(Constants.modId + ":craftingStation");
		topIcon = iconRegister.registerIcon(Constants.modId + ":craftingStation_top");
		bottomIcon = iconRegister.registerIcon(Constants.modId + ":craftingStation_bottom");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta) {
		return ((side == 0) ? bottomIcon : ((side == 1) ? topIcon : blockIcon));
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityCraftingStation();
	}
	
}
