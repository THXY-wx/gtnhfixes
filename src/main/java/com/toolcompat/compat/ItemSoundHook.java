package com.toolcompat.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;

import com.toolcompat.ToolCompat;
import gtnh.fixes.config.GtnhFixesConfig;

import cpw.mods.fml.common.Loader;

/**
 * Adds Tinkers' Construct (and other non-vanilla) item categories to An Extra Touch's sound
 * registry through its public {@code ItemSoundRegistry.reload(String[])} API.
 *
 * <p>An Extra Touch already reads {@code Item.getToolClasses}; the missing case is TiC, whose
 * weapons extend {@code tconstruct.library.tools.Weapon} instead of {@code ItemSword}. Because the
 * overrides are keyed by item id, only non-subtyped items are added: a wildcard override for a
 * GregTech meta-tool would otherwise make every GT tool share one category.</p>
 */
public final class ItemSoundHook {

    private static final String SENTINEL = "toolcompat:toolfix_sentinel=utility";

    private static boolean initialized;
    private static Field overridesField;
    private static Method reloadMethod;
    private static List<String> lastGenerated = new ArrayList<String>();
    private static int ticks;

    private ItemSoundHook() {}

    public static void init() {
        if (initialized || !Loader.isModLoaded("anextratouch")) return;
        initialized = true;
        try {
            Class<?> registry = Class.forName("industries.angelic.anextratouch.handlers.client.ItemSoundRegistry");
            Class<?> config = Class.forName("industries.angelic.anextratouch.Config");
            overridesField = config.getField("itemSoundOverrides");
            reloadMethod = registry.getMethod("reload", String[].class);
            apply();
        } catch (Throwable t) {
            ToolCompat.LOG.warn("An Extra Touch item sound API not usable: {}", t.toString());
        }
    }

    public static void tick() {
        if (!initialized) return;
        if (++ticks % 100 != 0) return;
        apply();
    }

    private static void apply() {
        try {
            List<String> generated = collectOverrides();
            String[] current = (String[]) overridesField.get(null);
            boolean applied = current != null && current.length > 0 && SENTINEL.equals(current[0]);

            if (!GtnhFixesConfig.fixItemSounds) {
                if (!applied) return;
            } else if (!applied && generated.isEmpty()) {
                return;
            } else if (applied && generated.equals(lastGenerated)) {
                return;
            }

            List<String> remaining = new ArrayList<String>();
            if (current != null) {
                for (String entry : current) {
                    if (SENTINEL.equals(entry)) continue;
                    if (lastGenerated.contains(entry)) continue;
                    remaining.add(entry);
                }
            }

            List<String> combined = new ArrayList<String>();
            if (GtnhFixesConfig.fixItemSounds && !generated.isEmpty()) {
                combined.add(SENTINEL);
                combined.addAll(generated);
            }
            combined.addAll(remaining);

            String[] array = combined.toArray(new String[combined.size()]);
            overridesField.set(null, array);
            reloadMethod.invoke(null, (Object) array);
            lastGenerated = generated;
        } catch (Throwable ignored) {
            // The target mod may not be ready yet; the next tick tries again.
        }
    }

    private static List<String> collectOverrides() {
        List<String> out = new ArrayList<String>();
        if (!GtnhFixesConfig.fixItemSounds) return out;
        for (Object object : Item.itemRegistry) {
            if (!(object instanceof Item)) continue;
            Item item = (Item) object;
            String category = category(item);
            if (category == null) continue;
            Object name = Item.itemRegistry.getNameForObject(item);
            if (name == null) continue;
            out.add(name.toString() + "=" + category);
        }
        return out;
    }

    private static String category(Item item) {
        ItemStack stack = new ItemStack(item);
        if (GtnhFixesConfig.useTinkersWeapons) {
            if (TinkersTools.isMelee(stack)) return "sword";
            if (TinkersTools.isHarvestTool(stack)) return "tool";
            if (TinkersTools.isRanged(stack)) return "crossbow";
        }
        if (GtnhFixesConfig.useModdedTools && !item.getHasSubtypes()) {
            // An Extra Touch already handles vanilla ItemSword/ItemTool/ItemAxe/ItemHoe.
            if (item instanceof ItemSword || item instanceof ItemTool
                || item instanceof ItemHoe || item instanceof ItemShears) {
                return null;
            }
            String toolClass = ForgeTools.primaryClass(stack);
            if (toolClass != null) {
                return "sword".equals(toolClass) ? "sword" : "axe".equals(toolClass) ? "axe" : "tool";
            }
        }
        return null;
    }
}
