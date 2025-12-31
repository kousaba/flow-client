package net.flowclient.mixin.client;

import io.netty.channel.ChannelHandlerContext;
import net.flowclient.Flow;
import net.flowclient.event.impl.PacketEvent;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.security.auth.callback.Callback;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, CallbackInfo ci){
        PacketEvent.Send event = new PacketEvent.Send(packet);
        Flow.EVENT_BUS.post(event);
        if(event.isCancelled()) ci.cancel();
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void onReceive(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci){
        PacketEvent.Receive event = new PacketEvent.Receive(packet);
        Flow.EVENT_BUS.post(event);
        if(event.isCancelled()) ci.cancel();
    }
}
