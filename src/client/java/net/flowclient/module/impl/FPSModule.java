package net.flowclient.module.impl;

import net.flowclient.module.TextHudModule;
import net.minecraft.client.MinecraftClient;

import java.util.List;
import java.util.Map;

public class FPSModule extends TextHudModule {
    public FPSModule(){
        super("FPS Module", 2, 2, "FPS: $(fps)", List.of("fps"));
    }

    @Override
    public Map<String, String> getValue(){
        int fps = (MinecraftClient.getInstance().getCurrentFps());
        return Map.of("fps", String.valueOf(fps));
    }
}
