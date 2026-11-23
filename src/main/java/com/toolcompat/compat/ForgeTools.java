package com.toolcompat.compat;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;

/**
 * Layer 1: the Forge tool API.
 *
 * <p>GregTech, Railcraft, BuildCraft and many other 1.7.10 mods expose their tools through
 * {@link net.minecraft.item.Item#getToolClasses(ItemStack)} and
 * {@link net.minecraft.item.Item#getHarvestLevel(ItemStack, String)} instead of extending
 * {@link net.minecraft.item.ItemTool}.</p>
 */
public final class ForgeTools {

    private static final String[] HARVEST_CLASSES = { "pickaxe", "axe", "shovel", "hoe", "sword" };

    private ForgeTools() {}

    /**
     * @return "sword" for a melee weapon, "axe" for an axe, "tool" for any other Forge tool class,
     *         or null when the item does not expose a Forge tool class.
     */
    public static String primaryClass(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return null;
        Set<String> classes = null;
        try {
            classes = stack.getItem().getToolClasses(stack);
        } catch (Throwable ignored) {
            // A few badly behaved items throw here; fall through to getHarvestLevel.
        }
        if (classes != null && !classes.isEmpty()) {
            if (classes.contains("sword")) return "sword";
            if (classes.contains("axe")) return "axe";
            return "tool";
        }
        for (String toolClass : HARVEST_CLASSES) {
            try {
                if (stack.getItem().getHarvestLevel(stack, toolClass) >= 0) {
                    return "sword".equals(toolClass) ? "sword" : "axe".equals(toolClass) ? "axe" : "tool";
                }
            } catch (Throwable ignored) {
                // Try the next class.
            }
        }
        return null;
    }

    public static boolean isSword(ItemStack stack) {
        return "sword".equals(primaryClass(stack));
    }

    public static boolean isTool(ItemStack stack) {
        return primaryClass(stack) != null;
    }

    /**
     * Forge-style harvest check used when WailaHarvestability is absent or the layer is off.
     * Unknown harvest levels are treated as "can harvest", so modded tools are never marked red
     * merely because they do not implement Forge's level API.
     */
    public static boolean canHarvest(ItemStack stack, Block block, int meta) {
        if (stack == null || stack.getItem() == null || block == null) return true;
        Material material = block.getMaterial();
        if (material == null || material.isToolNotRequired()) return true;
        String toolClass = block.getHarvestTool(meta);
        if (toolClass == null) return true;
        int level;
        try {
            level = stack.getItem().getHarvestLevel(stack, toolClass);
        } catch (Throwable ignored) {
            return true;
        }
        if (level < 0) return true;
        return block.getHarvestLevel(meta) <= level;
    }
}
