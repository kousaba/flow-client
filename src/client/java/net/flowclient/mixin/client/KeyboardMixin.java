package net.flowclient.mixin.client;


import net.flowclient.Flow;
import net.flowclient.event.impl.KeyEvent;
import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, @KeyInput.KeyAction int action, KeyInput input, CallbackInfo ci){
        if(action == 1 && Flow.INSTANCE != null){
            Flow.EVENT_BUS.post(new KeyEvent(input.key()));
        }
    }
}
