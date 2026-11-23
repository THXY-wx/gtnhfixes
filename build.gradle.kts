plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

// The skin compatibility part is a coremod + Mixin. GTNH ships UniMixins, so the Mixin
// dependency is compileOnly and supplied at runtime; no refmap is needed because the mixin
// targets a mod class with remap = false.
tasks.withType<org.gradle.api.tasks.bundling.Jar>().configureEach {
    manifest {
        attributes(
            "FMLCorePlugin" to "gtnh.skincompat.SkinCompatPlugin",
            "FMLCorePluginContainsFMLMod" to "true",
            "ForceLoadAsMod" to "true",
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "MixinConfigs" to "mixins.gtnhfixes.json"
        )
    }
}
