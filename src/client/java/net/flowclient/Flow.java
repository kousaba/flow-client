package net.flowclient;

import net.fabricmc.api.ClientModInitializer;

public class Flow implements ClientModInitializer {
    public static final Flow INSTANCE = new Flow();
    @Override
    public void onInitializeClient(){
        System.out.println("Flow Client Initializing...");
    }
}
