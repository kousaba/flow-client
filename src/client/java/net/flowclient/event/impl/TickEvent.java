package net.flowclient.event.impl;

import net.flowclient.event.Event;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

public class TickEvent extends Event {
    public enum Phase{
        START, END
    }

    private final Phase phase;
    private final MinecraftClient mc;

    public TickEvent(Phase phase){
        this.phase = phase;
        this.mc = MinecraftClient.getInstance();
    }

    public boolean isStart(){
        return phase == Phase.START;
    }

    public boolean isEnd(){
        return phase == Phase.END;
    }

    public ClientPlayerEntity getPlayer(){
        return mc.player;
    }

    public ClientWorld getWorld(){
        return mc.world;
    }

    public boolean inGame(){
        return mc.player != null && mc.world != null;
    }
}
