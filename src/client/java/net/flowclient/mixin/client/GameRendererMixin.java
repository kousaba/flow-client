package net.flowclient.mixin.client;

import net.flowclient.script.runtime.FlowScriptLib;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir){
        float originalFov = cir.getReturnValue();
        if(FlowScriptLib.fovModifier != 1.0){
            cir.setReturnValue((float) (originalFov * FlowScriptLib.fovModifier));
        }
    }
}
