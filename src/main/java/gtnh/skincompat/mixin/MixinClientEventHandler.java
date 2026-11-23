package gtnh.skincompat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gtnh.fixes.config.GtnhFixesConfig;
import gtnh.skincompat.SkinPortGuard;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import roadhog360.simpleskinbackport.client.ClientEventHandler;

/**
 * SimpleSkinBackport's ClientEventHandler unconditionally calls checkAndSetArmsState() on
 * whatever renderer RenderManager returns. When SkinPort has replaced the player renderer,
 * that renderer's model is SkinPort's own 64x64 ModelBiped which never went through SSB's
 * ssb$set64x() upgrade, so the armwear ArmPair is null and the first rendered frame throws:
 *
 *   NPE in ArmPair.of <- ModelBiped.ssb$createArmBoxes <- ssb$setSlim
 *
 * SkinPort already handles arms/slim itself, so SSB must simply stay away from it. Both
 * injections are gated by the GTNHLib config switch "fixSkinPortSsb".
 */
@Mixin(value = ClientEventHandler.class, remap = false)
public abstract class MixinClientEventHandler {

    @Inject(method = "onHandRender", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gtnhfixes$skipHandRender(RenderHandEvent event, CallbackInfo ci) {
        if (GtnhFixesConfig.fixSkinPortSsb && SkinPortGuard.skinPortLoaded()) {
            ci.cancel();
        }
    }

    @Inject(method = "onRenderEntity", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gtnhfixes$skipEntityRender(RenderLivingEvent.Pre event, CallbackInfo ci) {
        if (GtnhFixesConfig.fixSkinPortSsb && SkinPortGuard.isSkinPortRenderer(event)) {
            ci.cancel();
        }
    }
}
