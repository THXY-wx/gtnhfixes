package gtnh.fixes.client;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gtnh.fixes.config.GtnhFixesConfig;

@SideOnly(Side.CLIENT)
public class GtnhFixesConfigScreen extends SimpleGuiConfig {

    public GtnhFixesConfigScreen(GuiScreen parent) throws ConfigException {
        super(parent, "gtnhfixes", "GTNH Fixes", GtnhFixesConfig.class);
    }
}
