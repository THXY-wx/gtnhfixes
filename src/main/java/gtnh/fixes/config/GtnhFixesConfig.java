package gtnh.fixes.config;

import com.gtnewhorizon.gtnhlib.config.Config;

/**
 * All compatibility fixes are toggled here. GTNHLib reads this class and shows it in the
 * in-game Mods config screen; the comments below are written to the config file (Chinese included).
 */
@Config(modid = "gtnhfixes", category = "compat", configSubDirectory = "gtnhfixes", filename = "config")
public final class GtnhFixesConfig {

    @Config.Comment({
        "Layer 1: read Forge Item.getToolClasses / getHarvestLevel (GregTech, Railcraft, BuildCraft, ...).",
        "第 1 层：读取 Forge 的 Item.getToolClasses / getHarvestLevel（GregTech、Railcraft、BuildCraft 等）。"
    })
    @Config.LangKey("gtnhfixes.config.useModdedTools")
    public static boolean useModdedTools = true;

    @Config.Comment({
        "Layer 2: Tinkers' Construct Weapon / HarvestTool / AOEHarvestTool / IWindup.",
        "第 2 层：匠魂 Weapon / HarvestTool / AOEHarvestTool / IWindup。"
    })
    @Config.LangKey("gtnhfixes.config.useTinkersWeapons")
    public static boolean useTinkersWeapons = true;

    @Config.Comment({
        "Layer 3: WailaHarvestability ToolHelper.canToolHarvestBlock for the green/red dots.",
        "第 3 层：调用 WailaHarvestability 的 ToolHelper.canToolHarvestBlock 画绿点/红点。"
    })
    @Config.LangKey("gtnhfixes.config.useWailaHarvest")
    public static boolean useWailaHarvest = true;

    @Config.Comment({
        "Add An Extra Touch item sound categories for Tinkers' weapons and tools.",
        "为 An Extra Touch 补充匠魂武器/工具的音效分类。"
    })
    @Config.LangKey("gtnhfixes.config.fixItemSounds")
    public static boolean fixItemSounds = true;

    @Config.Comment({
        "Stop SimpleSkinBackport from touching SkinPort's player model (fixes the first-join NPE in ArmPair.of).",
        "阻止 SimpleSkinBackport 处理 SkinPort 的玩家模型（修复首次进世界时 ArmPair.of 的空指针崩溃）。"
    })
    @Config.LangKey("gtnhfixes.config.fixSkinPortSsb")
    public static boolean fixSkinPortSsb = true;

    public GtnhFixesConfig() {}
}
