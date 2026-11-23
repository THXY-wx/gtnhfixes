package com.toolcompat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.toolcompat.compat.CrosshairHook;
import com.toolcompat.compat.ItemSoundHook;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import gtnh.fixes.config.GtnhFixesConfig;

/**
 * GTNH Fixes: tool/weapon recognition plus SkinPort x SimpleSkinBackport compatibility.
 *
 * <p>All compatibility fixes are toggled from a GTNHLib config (shown in the in-game Mods
 * config screen). The skin fix is a Mixin into SimpleSkinBackport; the tool fixes use only the
 * public APIs of An Extra Touch and the Dynamic Crosshair 1.7.10 port.</p>
 */
@Mod(
    modid = Reference.MODID,
    name = Reference.NAME,
    version = Reference.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    acceptableRemoteVersions = "*",
    dependencies = "required-after:gtnhlib",
    guiFactory = "gtnh.fixes.client.ConfigGuiFactory")
public class ToolCompat {

    public static final Logger LOG = LogManager.getLogger(Reference.MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        try {
            ConfigurationManager.registerConfig(GtnhFixesConfig.class);
        } catch (ConfigException e) {
            LOG.error("Failed to register the GTNH Fixes config", e);
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (!isClient()) return;
        FMLCommonHandler.instance().bus().register(this);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (!isClient()) return;
        CrosshairHook.init();
        ItemSoundHook.init();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ItemSoundHook.tick();
    }

    private static boolean isClient() {
        try {
            return FMLCommonHandler.instance().getSide().isClient();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
