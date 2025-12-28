package net.flowclient;

import net.fabricmc.api.ClientModInitializer;
import net.flowclient.module.ModuleManager;

import java.io.File;

public class Flow implements ClientModInitializer {
    public static final Flow INSTANCE = new Flow();
    public ModuleManager moduleManager;
    @Override
    public void onInitializeClient(){
        System.out.println("Flow Client Initializing...");
        File dir = new File("flow_config");
        if(!dir.exists()) dir.mkdir();
        moduleManager = new ModuleManager(dir);
        moduleManager.loadConfig();
    }
}
