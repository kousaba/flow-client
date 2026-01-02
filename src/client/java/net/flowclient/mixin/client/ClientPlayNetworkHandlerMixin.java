package net.flowclient.mixin.client;

import net.flowclient.Flow;
import net.flowclient.event.impl.ChatReceiveEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ProfilelessChatMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
//    @Inject(method = "onGameMessage", at = @At("HEAD"))
//    private void onGameMessage(GameMessageS2CPacket packet, CallbackInfo ci){
//        postChat(packet.content().getString());
//    }
//    @Inject(method = "onChatMessage", at = @At("HEAD"))
//    private void onChatMessage(ChatMessageS2CPacket packet, CallbackInfo ci){
//        postChat(packet.body().content());
//    }
//    @Inject(method = "onProfilelessChatMessage", at = @At("HEAD"))
//    private void onProfilelessChatMessage(ProfilelessChatMessageS2CPacket packet, CallbackInfo ci){
//        postChat(packet.message().getString());
//    }
//
//    private void postChat(String msg) {
//        if (Flow.INSTANCE != null) {
//            // 呼び出し元のメソッド名を取得して表示する
//            String caller = Thread.currentThread().getStackTrace()[2].getMethodName();
//            System.out.println("[Debug] Chat Event from: " + caller + " | Msg: " + msg);
//
//            Flow.EVENT_BUS.post(new ChatReceiveEvent(msg));
//        }
//    }
}
