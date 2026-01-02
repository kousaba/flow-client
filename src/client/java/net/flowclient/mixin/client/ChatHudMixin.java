package net.flowclient.mixin.client;

import net.flowclient.Flow;
import net.flowclient.event.impl.ChatReceiveEvent;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.text.Text;

@Mixin(ChatHud.class)
class ChatHudMixin {
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    private void onAddMessage(Text message, CallbackInfo ci){
        System.out.println("add message");
        if(Flow.INSTANCE != null){
            String msg = message.toString();
            Flow.EVENT_BUS.post(new ChatReceiveEvent(msg));
        }
    }
}
