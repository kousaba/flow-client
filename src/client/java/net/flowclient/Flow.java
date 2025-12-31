package net.flowclient;

import net.fabricmc.api.ClientModInitializer;
import net.flowclient.event.EventBus;
import net.flowclient.module.Module;
import net.flowclient.module.ModuleManager;
import net.flowclient.script.ScriptText;

import java.io.File;

public class Flow implements ClientModInitializer {
    public static final EventBus EVENT_BUS = new EventBus();
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
        moduleManager.loadConfig();
        System.out.println("Flow Client Initialized!");
        for(Module m : moduleManager.getModules()){
            EVENT_BUS.register(m);
        }
        EVENT_BUS.register(moduleManager);
        String code = """
    fn on_tick() {
        let fps = 60;
        if (fps > 30) {
            color = #00FF00;
        }
        text = "FPS: " + fps;
    }
    """;
        ScriptText.testParser(code);
    }
}
