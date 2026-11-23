package gtnh.skincompat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Runtime guards, kept free of Minecraft types so they can be unit tested.
 */
public final class SkinPortGuard {

    private static final Logger LOGGER = LogManager.getLogger("gtnhfixes");
    private static final boolean SKINPORT;
    private static boolean logged;

    static {
        // Resource lookup instead of Class.forName: the renderer class extends a vanilla
        // (obfuscated) class, so it cannot be resolved outside a running game.
        boolean present;
        try {
            present = SkinPortGuard.class.getResource("/lain/mods/skinport/impl/forge/SkinPortRenderPlayer.class") != null;
        } catch (Throwable t) {
            present = false;
        }
        SKINPORT = present;
    }

    private SkinPortGuard() {}

    public static boolean skinPortLoaded() {
        return SKINPORT;
    }

    /**
     * True when the given RenderLivingEvent carries a SkinPort renderer.
     * Uses reflection because the renderer field type is a vanilla (obfuscated) class.
     */
    public static boolean isSkinPortRenderer(Object event) {
        if (!SKINPORT || event == null) {
            return false;
        }
        try {
            Object renderer = event.getClass().getField("renderer").get(event);
            if (renderer == null) {
                return false;
            }
            if (renderer.getClass().getName().startsWith("lain.mods.skinport.")) {
                logOnce();
                return true;
            }
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    private static void logOnce() {
        if (logged) {
            return;
        }
        logged = true;
        LOGGER.info("[gtnhfixes] SkinPort renderer detected - SimpleSkinBackport arm handling is skipped for it");
    }
}
