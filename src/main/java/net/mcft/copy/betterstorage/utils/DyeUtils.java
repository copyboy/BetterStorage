package net.mcft.copy.betterstorage.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.BlockColored;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class DyeUtils {
	
	private static final Map<String, Integer> dyes = new HashMap<String, Integer>();
	static {
		addColorFromTable("dyeBlack");
		addColorFromTable("dyeRed");
		addColorFromTable("dyeGreen");
		addColorFromTable("dyeBrown");
		addColorFromTable("dyeBlue");
		addColorFromTable("dyePurple");
		addColorFromTable("dyeCyan");
		addColorFromTable("dyeLightGray");
		addColorFromTable("dyeGray");
		addColorFromTable("dyePink");
		addColorFromTable("dyeLime");
		addColorFromTable("dyeYellow");
		addColorFromTable("dyeLightBlue");
		addColorFromTable("dyeMagenta");
		addColorFromTable("dyeOrange");
		addColorFromTable("dyeWhite");
	};
	
	private DyeUtils() {  }
	
	/** Gets the dye color of the item stack. <br>
	 *  If it's not a dye, it will return -1. */
	public static int getDyeColor(ItemStack stack) {
		if (stack == null) return -1;
		int[] oreIds = OreDictionary.getOreIDs(stack);
		for (int ore : oreIds) {
			String name = OreDictionary.getOreName(ore);
			Integer color = dyes.get(name);
			if (color != null) return color;
		}
		return -1;
	}
	
	/** Returns if the item stack is a dye. */
	public static boolean isDye(ItemStack stack) {
		return (getDyeColor(stack) >= 0);
	}
	
	/** Returns the combined color of all the dyes and the base color. */
	public static int getColorFromDyes(int color, Collection<ItemStack> dyes) {
		int number = dyes.size();
		if (number < 1) return -1;
		int r = 0;
		int g = 0;
		int b = 0;
		if (color >= 0) {
			r = (color >> 16);
			g = ((color >> 8) & 0xFF);
			b = (color & 0xFF);
			number++;
		}
		for (ItemStack dye : dyes) {
			color = getDyeColor(dye);
			if (color < 0) continue;
			r += (color >> 16);
			g += ((color >> 8) & 0xFF);
			b += (color & 0xFF);
		}
		r /= number;
		g /= number;
		b /= number;
		return ((r << 16) | (g << 8) | b);
	}
	/** Returns the combined color of all the dyes. */
	public static int getColorFromDyes(Collection<ItemStack> dyes) {
		return getColorFromDyes(-1, dyes);
	}
	
	private static void addColorFromTable(String name) {
		int dye = BlockColored.func_150031_c(dyes.size());
		float[] values = EntitySheep.fleeceColorTable[dye];
		int r = (int)(values[0] * 255);
		int g = (int)(values[1] * 255);
		int b = (int)(values[2] * 255);
		int color = ((r << 16) | (g << 8) | b);
		dyes.put(name, color);
	}
	
}
