package com.toolcompat.compat;

import net.minecraft.item.ItemStack;

/**
 * Layer 2: Tinkers' Construct classes, loaded by name so the mod compiles and runs without TiC.
 *
 * <p>TiC melee weapons extend {@code tconstruct.library.tools.Weapon}; pickaxes, hammers,
 * excavators and shovels extend {@code HarvestTool}/{@code AOEHarvestTool}; bows and crossbows
 * implement {@code tconstruct.library.weaponry.IWindup}.</p>
 */
public final class TinkersTools {

    private static final Class<?> WEAPON = classOrNull("tconstruct.library.tools.Weapon");
    private static final Class<?> HARVEST_TOOL = classOrNull("tconstruct.library.tools.HarvestTool");
    private static final Class<?> AOE_HARVEST_TOOL = classOrNull("tconstruct.library.tools.AOEHarvestTool");
    private static final Class<?> WINDUP = classOrNull("tconstruct.library.weaponry.IWindup");

    private TinkersTools() {}

    public static boolean isAvailable() {
        return WEAPON != null || HARVEST_TOOL != null || WINDUP != null;
    }

    public static boolean isMelee(ItemStack stack) {
        return instanceOf(WEAPON, stack);
    }

    public static boolean isHarvestTool(ItemStack stack) {
        return instanceOf(HARVEST_TOOL, stack) || instanceOf(AOE_HARVEST_TOOL, stack);
    }

    public static boolean isRanged(ItemStack stack) {
        return instanceOf(WINDUP, stack);
    }

    private static boolean instanceOf(Class<?> type, ItemStack stack) {
        if (type == null || stack == null || stack.getItem() == null) return false;
        try {
            return type.isInstance(stack.getItem());
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Class<?> classOrNull(String name) {
        try {
            return Class.forName(name, false, TinkersTools.class.getClassLoader());
        } catch (Throwable ignored) {
            return null;
        }
    }
}
