package net.flowclient.mixin.client;

import net.flowclient.script.runtime.FlowScriptLib;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"
            )
    )
    private Object onGetGamma(SimpleOption<Float> instance) {
        // スクリプトで設定された値があれば、それを優先して返す
        if (FlowScriptLib.overrideGamma != null) {
            return FlowScriptLib.overrideGamma;
        }
        // なければ通常通り設定画面の値を返す
        return instance.getValue();
    }
}

