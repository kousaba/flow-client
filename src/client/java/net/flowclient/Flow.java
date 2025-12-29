package net.flowclient;

import net.fabricmc.api.ClientModInitializer;
import net.flowclient.module.ModuleManager;

import java.io.File;

public class Flow implements ClientModInitializer {
    public static Flow INSTANCE;
    public ModuleManager moduleManager;
    @Override
    public void onInitializeClient(){
        System.out.println("Flow Client Initializing...");
        INSTANCE = this;
        System.out.println("Searching Flow Config File..");
        File dir = new File("flow_config");
        if(!dir.exists()) dir.mkdir();
        System.out.println("ModuleManager Setting...");
        moduleManager = new ModuleManager(dir);
//        moduleManager.loadConfig();
        System.out.println("Flow Client Initialized!");
    }
}
