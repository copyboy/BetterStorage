package net.mcft.copy.betterstorage.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.mcft.copy.betterstorage.Config;
import net.mcft.copy.betterstorage.block.TileEntityBackpack;
import net.mcft.copy.betterstorage.client.model.ModelBackpackArmor;
import net.mcft.copy.betterstorage.container.SlotArmorBackpack;
import net.mcft.copy.betterstorage.inventory.InventoryStacks;
import net.mcft.copy.betterstorage.misc.Constants;
import net.mcft.copy.betterstorage.misc.PropertiesBackpack;
import net.mcft.copy.betterstorage.utils.DirectionUtils;
import net.mcft.copy.betterstorage.utils.EntityUtils;
import net.mcft.copy.betterstorage.utils.StackUtils;
import net.mcft.copy.betterstorage.utils.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumHelper;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISpecialArmor;

public class ItemBackpack extends ItemArmor implements ISpecialArmor {
	
	public static final EnumArmorMaterial material = EnumHelper.addArmorMaterial(
			"backpack", 240, new int[]{ 0, 2, 0, 0 }, 15);
	static { material.customCraftingMaterial = Item.leather; }
	
	protected ItemBackpack(int id, EnumArmorMaterial material) {
		super(id - 256, material, 0, 1);
		setMaxDamage(240);
	}
	public ItemBackpack(int id) {
		this(id, EnumArmorMaterial.CLOTH);
	}
	
	public String getName() { return "container.backpack"; }
	
	/** Returns the number of columns this backpack has. */
	public int getColumns() { return 9; }
	/** Returns the number of rows this backpack has. */
	public int getRows() { return Config.backpackRows; }
	
	protected IInventory getBackpackItemsInternal(EntityLiving carrier, EntityPlayer player) {
		PropertiesBackpack backpackData = getBackpackData(carrier);
		if (backpackData.contents == null)
			backpackData.contents = new ItemStack[getColumns() * getRows()];
		return new InventoryStacks(getName(), backpackData.contents);
	}
	
	public boolean canUnequip(EntityPlayer player, ItemStack backpack) {
		// For compatibility with previous versions:
		// Do not allow backpacks that still have the items tag to be taken from the slot.
		PropertiesBackpack backpackData = getBackpackData(player);
		return (!StackUtils.has(backpack, "Items") &&
				((backpackData.contents == null) ||
		         StackUtils.isEmpty(backpackData.contents)));
	}
	
	// Item stuff
	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber() { return 0; }
	
	@Override
	public boolean isValidArmor(ItemStack stack, int armorType) { return false; }
	
	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLiving entity, ItemStack stack, int slot) {
		return ModelBackpackArmor.instance;
	}
	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer) {
		return String.format(Constants.backpackTexture, layer - 1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advancedTooltips) {
		if ((getBackpack(player) != stack) || canUnequip(player, stack)) return;
		list.add("Contains items. Hold shift and right click");
		list.add("ground with empty hand to unequip.");
	}
	
	@Override
	public void onArmorTickUpdate(World world, EntityPlayer player, ItemStack itemStack) {
		
		// Replace the armor slot with a custom one, so the player
		// can't unequip the backpack when there's items inside.
		
		int index = 5 + armorType;
		Slot slotBefore = player.inventoryContainer.getSlot(index);
		if (slotBefore instanceof SlotArmorBackpack) return;
		int slotIndex = player.inventory.getSizeInventory() - getChestSlotOffset(player) - armorType;
		SlotArmorBackpack slot = new SlotArmorBackpack(player.inventory, slotIndex, 8, 8 + armorType * 18);
		slot.slotNumber = index;
		player.inventoryContainer.inventorySlots.set(index, slot);
		
	}
	
	private int getChestSlotOffset(EntityPlayer player)
	{
		return isExact(player.inventory, "micdoodle8.mods.galacticraft.core.inventory.GCCoreInventoryPlayer") ? 6 : 1;
	}
	
	private static boolean isExact(Object paramObject, String paramString)
	{
		try
		{
			return paramObject.getClass().getName().equals(paramString); 
		} 
		catch (Exception localException) 
		{
			return false;
		}
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) { return stack; }
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player,
	                         World world, int x, int y, int z, int side,
	                         float hitX, float hitY, float hitZ) {
		
		if (stack.stackSize == 0) return false;
		
		Block blockBackpack = Block.blocksList[itemID];
		Block blockClicked = Block.blocksList[world.getBlockId(x, y, z)];
		
		ForgeDirection orientation = DirectionUtils.getOrientation(player).getOpposite();
		
		// If a replacable block was clicked, move on.
		// Otherwise, check if the top side was clicked and adjust the position.
		if ((blockClicked != null) &&
		    (blockClicked != Block.snow) &&
		    (blockClicked != Block.vine) &&
		    (blockClicked != Block.tallGrass) &&
		    (blockClicked != Block.deadBush) &&
		    !blockClicked.isBlockReplaceable(world, x, y, z)) {
			if (side != 1) return false;
			y++;
		}
		
		// Return false if there's block is too low or too high.
		if ((y <= 0) || (y >= world.getHeight() - 1)) return false;
		
		// Return false if not placed on top of a solid block.	
		Block blockBelow = Block.blocksList[world.getBlockId(x, y - 1, z)];
		if ((blockBelow == null) || !blockBelow.isBlockSolidOnSide(world, x, y, z, ForgeDirection.UP)) return false;
		
		// Return false if the player can't edit the block.
		if (!player.canPlayerEdit(x, y, z, side, stack)) return false;
		
		// Return false if there's an entity blocking the placement.
		if (!world.canPlaceEntityOnSide(blockBackpack.blockID, x, y, z, false, side, player, stack)) return false;
		
		// Actually place the block in the world,
		// play place sound and decrease stack size if successful.
		if (!world.setBlock(x, y, z, blockBackpack.blockID, orientation.ordinal(), 3))
			return false;
		
		if (world.getBlockId(x, y, z) != blockBackpack.blockID)
			return false;
		
		blockBackpack.onBlockPlacedBy(world, x, y, z, player, stack);
		blockBackpack.onPostBlockPlaced(world, x, y, z, orientation.ordinal());
		
		TileEntityBackpack backpack = WorldUtils.get(world, x, y, z, TileEntityBackpack.class);
		backpack.stack = stack.copy();
		if (ItemBackpack.getBackpack(player) == stack)
			backpack.unequip(player);
		
		String sound = blockBackpack.stepSound.getPlaceSound();
		float volume = (blockBackpack.stepSound.getVolume() + 1.0F) / 2.0F;
		float pitch = blockBackpack.stepSound.getPitch() * 0.8F;
		world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5F, sound, volume, pitch);
		stack.stackSize--;
		
		return true;
		
	}
	
	// ISpecialArmor implementation
	
	@Override
	public ArmorProperties getProperties(EntityLiving player, ItemStack armor,
	                                     DamageSource source, double damage, int slot) {
		return new ArmorProperties(0, 2 / 25.0, armor.getMaxDamage() + 1 - armor.getItemDamage());
	}
	
	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) { return 2; }
	
	@Override
	public void damageArmor(EntityLiving entity, ItemStack stack,
	                        DamageSource source, int damage, int slot) {
		stack.damageItem(damage, entity);
		if (stack.stackSize > 0) return;
		PropertiesBackpack backpackData = ItemBackpack.getBackpackData(entity);
		if (backpackData.contents != null)
			for (ItemStack s : backpackData.contents)
				WorldUtils.dropStackFromEntity(entity, s);
		entity.renderBrokenItemStack(stack);
	}
	
	// Helper functions
	
	public static ItemStack getBackpack(EntityLiving entity) {
		ItemStack backpack = entity.getCurrentArmor(2);
		if ((backpack != null) &&
		    (backpack.getItem() instanceof ItemBackpack)) return backpack;
		else return null;
	}
	public static void setBackpack(EntityLiving entity, ItemStack backpack, ItemStack[] contents) {
		entity.setCurrentItemOrArmor(3, backpack);
		if (!entity.worldObj.isRemote)
			getBackpackData(entity).contents = contents;
	}
	public static void removeBackpack(EntityLiving entity) {
		if (!entity.worldObj.isRemote)
			getBackpackData(entity).contents = null;
		entity.setCurrentItemOrArmor(3, null);
	}
	
	public static IInventory getBackpackItems(EntityLiving carrier, EntityPlayer player) {
		ItemStack backpack = getBackpack(carrier);
		if (backpack == null) return null;
		return ((ItemBackpack)backpack.getItem()).getBackpackItemsInternal(carrier, player);
	}
	public static IInventory getBackpackItems(EntityLiving carrier) {
		return getBackpackItems(carrier, null);
	}
	
	public static PropertiesBackpack getBackpackData(EntityLiving entity) {
		PropertiesBackpack backpackData = EntityUtils.getProperties(entity, PropertiesBackpack.class);
		if (backpackData == null) {
			backpackData = EntityUtils.createProperties(entity, PropertiesBackpack.class);
			ItemBackpack.initBackpackOpen(entity);
		}
		return backpackData;
	}
	
	public static void initBackpackOpen(EntityLiving entity) {
		entity.getDataWatcher().addObject(Config.backpackOpenDataWatcherId, (byte)0);
	}
	public static void setBackpackOpen(EntityLiving entity, boolean isOpen) {
		entity.getDataWatcher().updateObject(Config.backpackOpenDataWatcherId, (byte)(isOpen ? 1 : 0));
	}
	public static boolean isBackpackOpen(EntityLiving entity) {
		return (entity.getDataWatcher().getWatchableObjectByte(Config.backpackOpenDataWatcherId) != 0);
	}
	
}
