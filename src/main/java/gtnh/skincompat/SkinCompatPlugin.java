package gtnh.skincompat;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

/** Coremod entry point: ensures this jar is on the launch classpath and its mixin config is registered. */
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class SkinCompatPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
