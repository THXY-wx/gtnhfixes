# GTNH Fixes

GTNH 2.9.0 / Minecraft 1.7.10 Forge 客户端修复合集，合并了两个原本独立的补丁：

1. **Tool Compatibility** —— 让依赖“武器/工具识别”的模组正确识别 GregTech、Railcraft、BuildCraft、匠魂等模组的工具与武器。
2. **SkinCompat** —— 让 SimpleSkinBackport 不再处理 SkinPort 的玩家模型，修复首次进世界时的 `ArmPair.of` 空指针崩溃。

两个目标模组都只通过公开 API / Mixin 接入，不修改它们的代码；未安装时对应功能自动跳过。

## 修复内容

### SkinPort x SimpleSkinBackport

SimpleSkinBackport 的 `ClientEventHandler` 会在 `onHandRender` 和 `onRenderEntity` 里无条件调用
`checkAndSetArmsState(...)`。当 SkinPort 已经替换玩家渲染器时，模型是 SkinPort 自己 new 的
64×64 `ModelPlayer`，没有经过 SSB 的 `ssb$set64x` 升级，`ssb$bipedLeftArmwear` 为 null，
首帧渲染手时在 `ArmPair.of` 抛出空指针。

本模组用 Mixin 在这两个入口取消 SSB 的手臂处理，玩家渲染完全交给 SkinPort；头颅、骷髅等
“复制玩家皮肤”的模型仍由 SSB 处理。日志中会出现一次：

```
[gtnhfixes] SkinPort renderer detected - SimpleSkinBackport arm handling is skipped for it
```

开关：`compat.fixSkinPortSsb`（默认开）。关掉后恢复 SSB 原始行为（可能再次崩溃）。

### 模组工具 / 武器识别

三层判断，任一层关闭就回退到下一层，最后回退到目标模组自己的逻辑：

1. **通用模组工具层** —— `compat.useModdedTools`
   - `Item.getToolClasses(stack)`
   - `getHarvestLevel(stack, "pickaxe" / "axe" / "shovel" / "hoe" / "sword")`
   - `toolClasses` 含 `sword` → 近战准星 / 剑音效；其余 → 工具准星 / 工具音效。
2. **匠魂专用层** —— `compat.useTinkersWeapons`
   - `tconstruct.library.tools.Weapon` → 近战；
   - `HarvestTool` / `AOEHarvestTool` → 工具；
   - `tconstruct.library.weaponry.IWindup` → 远程。
3. **WailaHarvestability** —— `compat.useWailaHarvest`
   - 绿点/红点直接调用 `ToolHelper.canToolHarvestBlock(stack, block, meta)`；
   - 未安装或关闭时退回 Forge harvest level 判断。

两个使用方：

- **An Extra Touch 音效**：通过 `ItemSoundRegistry.reload(String[])` 追加匠魂物品分类（`fixItemSounds`）。
- **Dynamic Crosshair 1.7.10**：通过 `DynamicCrosshairApi.register(IInteractionProvider)` 注册三层判定。

> An Extra Touch 本身没有准星/绿点；准星部分对接的是 1.7.10 DynamicCrosshair 移植版。只装
> An Extra Touch 时，准星适配会自动跳过。

## 配置

使用 **GTNHLib** 配置系统（GTNH 自带，游戏内 Mods 列表 → GTNH Fixes → Config）：

- 文件：`config/gtnhfixes/config.cfg`
- 分类：`compat`
- 所有开关（默认全部开启）：

```
compat {
    B:useModdedTools=true
    B:useTinkersWeapons=true
    B:useWailaHarvest=true
    B:fixItemSounds=true
    B:fixSkinPortSsb=true
}
```

配置文件注释为英中双语；游戏内 GUI 在中文语言下显示中文标签（`zh_CN.lang`）。

## 安装

1. 安装 GTNH 2.9.0（自带 GTNHLib、UniMixins；本模组要求 `required-after:gtnhlib`）。
2. 把 `GTNHFixes-1.7.10-2.0.0.jar` 放进 `mods/`。
3. 可选：安装 SkinPort + SimpleSkinBackport（皮肤修复）、An Extra Touch（音效分类）、
   DynamicCrosshair 1.7.10（准星识别）；未安装的部分自动跳过。
4. 重启游戏。

## 构建

```sh
./gradlew build --offline
```

产物：`build/libs/GTNHFixes-1.7.10-2.0.0.jar`。
