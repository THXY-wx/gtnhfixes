package com.toolcompat.compat;

import java.lang.reflect.Method;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

/**
 * Layer 3: WailaHarvestability. Its {@code ToolHelper.canToolHarvestBlock} is the exact predicate
 * Waila uses for the green/red harvest indicator, so the crosshair dots agree with Waila.
 */
public final class WailaTools {

    private static boolean resolved;
    private static Method method;

    private WailaTools() {}

    /**
     * @return TRUE/FALSE when WailaHarvestability is installed and answered, null otherwise.
     */
    public static Boolean canHarvest(ItemStack stack, Block block, int meta) {
        resolve();
        if (method == null || stack == null || block == null) return null;
        try {
            Object result;
            if (method.getParameterTypes().length == 3) {
                result = method.invoke(null, stack, block, meta);
            } else {
                result = method.invoke(null, stack, block);
            }
            return result instanceof Boolean ? (Boolean) result : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        String[] classes = {
            "squeek.wailaharvestability.helpers.ToolHelper",
            "squeek.wailaharvestability.ToolHelper"
        };
        for (String className : classes) {
            try {
                Class<?> helper = Class.forName(className, false, WailaTools.class.getClassLoader());
                try {
                    method = helper.getMethod("canToolHarvestBlock", ItemStack.class, Block.class, int.class);
                } catch (NoSuchMethodException ignored) {
                    method = helper.getMethod("canToolHarvestBlock", ItemStack.class, Block.class);
                }
                return;
            } catch (Throwable ignored) {
                method = null;
            }
        }
    }
}
