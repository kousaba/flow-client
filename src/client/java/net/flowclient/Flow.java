package net.flowclient;

import net.fabricmc.api.ClientModInitializer;
import net.flowclient.event.EventBus;
import net.flowclient.module.Module;
import net.flowclient.module.ModuleManager;
import net.flowclient.module.impl.ScriptModule;
import net.flowclient.script.ScriptText;
import net.flowclient.script.runtime.FlowScriptInterpreter;
import net.flowclient.script.runtime.FlowScriptLib;
import net.flowclient.util.FlowLogger;

import java.io.File;

public class Flow implements ClientModInitializer {
    public static final EventBus EVENT_BUS = new EventBus();
    public static Flow INSTANCE;
    public ModuleManager moduleManager;
    @Override
    public void onInitializeClient(){
        FlowLogger.info("Flow Client Initializing...");
        INSTANCE = this;
        FlowLogger.debug("Looking for config file...");
        File dir = new File("flow_config");
        if(!dir.exists()) dir.mkdir();
        FlowLogger.debug("Initializing modules...");
        moduleManager = new ModuleManager(dir);
        FlowLogger.debug("Loading module configuration...");
        moduleManager.loadConfig();
        FlowLogger.debug("Registering FlowScriptLibs...");
        moduleManager.registerScriptLibs();
        FlowLogger.debug("Registering Eventbus...");
        for(Module m : moduleManager.getModules()){
            EVENT_BUS.register(m);
        }
        EVENT_BUS.register(moduleManager);
        FlowLogger.info("Flow Client Initialized!!");
    }
}
