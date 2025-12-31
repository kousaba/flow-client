package net.flowclient.mixin.client;

import net.flowclient.Flow;
import net.flowclient.event.impl.TickEvent;
import net.flowclient.gui.screen.ConfigScreen;
import net.flowclient.gui.screen.FlowScriptScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    private boolean isConfigScreenOpenKeyPressed = false;
    private boolean wasLeftButtonPressed = false;
    private boolean wasRightButtonPressed = false;
    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onHandleInputEvents(CallbackInfo ci){
        MinecraftClient client = (MinecraftClient)(Object)this;
        if(InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT)){
            if(!isConfigScreenOpenKeyPressed){
                if(client.currentScreen == null){
                    client.setScreen(new ConfigScreen());
                }else if(client.currentScreen instanceof ConfigScreen){
                    client.setScreen(null);
                }
            }
            isConfigScreenOpenKeyPressed = true;
        }else{
            isConfigScreenOpenKeyPressed = false;
            if(InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_F12)){
                client.setScreen(new FlowScriptScreen());
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickHead(CallbackInfo ci){
        if(Flow.INSTANCE != null){
            Flow.EVENT_BUS.post(new TickEvent(TickEvent.Phase.START));
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci){
        if(Flow.INSTANCE != null){
            Flow.EVENT_BUS.post(new TickEvent(TickEvent.Phase.END));
        }
    }
}
