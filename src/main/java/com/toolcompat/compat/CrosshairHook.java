package com.toolcompat.compat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;

import com.toolcompat.ToolCompat;
import gtnh.fixes.config.GtnhFixesConfig;

import cpw.mods.fml.common.Loader;

/**
 * Registers a Dynamic Crosshair {@code IInteractionProvider} through the port's public API.
 *
 * <p>Everything is reflective so this mod compiles without the crosshair jar. When a layer is
 * disabled the provider returns {@code null}, which makes the port fall back to its own logic.</p>
 */
public final class CrosshairHook {

    private static boolean initialized;
    private static Class<?> contextClass;
    private static Field heldField;
    private static Field withBlockField;
    private static Field blockField;
    private static Field metaField;
    private static final Map<String, Object> TYPES = new HashMap<>();

    private CrosshairHook() {}

    public static void init() {
        if (initialized || !Loader.isModLoaded("dynamiccrosshair")) return;
        initialized = true;
        try {
            Class<?> api = Class.forName("mod.crend.dynamiccrosshair.api.DynamicCrosshairApi");
            Class<?> provider = Class.forName("mod.crend.dynamiccrosshair.api.IInteractionProvider");
            contextClass = Class.forName("mod.crend.dynamiccrosshair.crosshair.CrosshairContext");
            Class<?> interactionType = Class.forName("mod.crend.dynamiccrosshair.api.InteractionType");

            heldField = field(contextClass, "held");
            withBlockField = field(contextClass, "withBlock");
            blockField = field(contextClass, "block");
            metaField = field(contextClass, "meta");

            for (Object constant : interactionType.getEnumConstants()) {
                TYPES.put(((Enum<?>) constant).name(), constant);
            }

            Object proxy = Proxy.newProxyInstance(
                provider.getClassLoader(),
                new Class<?>[] { provider },
                new Handler());
            api.getMethod("register", provider).invoke(null, proxy);
            ToolCompat.LOG.info("Registered Dynamic Crosshair interaction provider.");
        } catch (Throwable t) {
            ToolCompat.LOG.warn("Dynamic Crosshair API not usable: {}", t.toString());
        }
    }

    private static Field field(Class<?> type, String name) throws NoSuchFieldException {
        Field field = type.getField(name);
        field.setAccessible(true);
        return field;
    }

    private static Object type(String name) {
        return TYPES.get(name);
    }

    private static Object classify(Object context) {
        ItemStack held;
        try {
            held = (ItemStack) heldField.get(context);
        } catch (Throwable ignored) {
            return null;
        }
        if (held == null || held.getItem() == null) return null;

        if (GtnhFixesConfig.useTinkersWeapons) {
            if (TinkersTools.isMelee(held)) return type("MELEE_WEAPON");
            if (TinkersTools.isHarvestTool(held)) return tool(context, held);
            if (TinkersTools.isRanged(held)) return type("RANGED_WEAPON");
        }
        if (GtnhFixesConfig.useModdedTools) {
            String toolClass = ForgeTools.primaryClass(held);
            if ("sword".equals(toolClass)) return type("MELEE_WEAPON");
            if (toolClass != null) return tool(context, held);
        }
        return null;
    }

    private static Object tool(Object context, ItemStack held) {
        try {
            if (!withBlockField.getBoolean(context)) return type("TOOL");
            Block block = (Block) blockField.get(context);
            int meta = metaField.getInt(context);
            if (block == null) return type("TOOL");
            Material material = block.getMaterial();
            if (material == null || material.isToolNotRequired()) return type("TOOL");
            if (block.getHarvestTool(meta) == null) return type("TOOL");

            if (GtnhFixesConfig.useWailaHarvest) {
                Boolean waila = WailaTools.canHarvest(held, block, meta);
                if (waila != null) return type(waila ? "CORRECT_TOOL" : "INCORRECT_TOOL");
            }
            return type(ForgeTools.canHarvest(held, block, meta) ? "CORRECT_TOOL" : "INCORRECT_TOOL");
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static final class Handler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if ("getModId".equals(name)) return "toolcompat";
            if ("getItemInteraction".equals(name) && args != null && args.length == 1) {
                try {
                    return classify(args[0]);
                } catch (Throwable ignored) {
                    return null;
                }
            }
            if ("equals".equals(name)) return proxy == args[0];
            if ("hashCode".equals(name)) return System.identityHashCode(proxy);
            if ("toString".equals(name)) return "ToolCompatInteractionProvider";
            return null;
        }
    }
}
