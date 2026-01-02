package net.flowclient.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.flowclient.Flow;
import net.flowclient.event.Subscribe;
import net.flowclient.event.impl.Render2DEvent;
import net.flowclient.module.impl.FPSModule;
import net.flowclient.module.impl.ScriptLibModule;
import net.flowclient.module.impl.ScriptModule;
import net.flowclient.script.runtime.FlowScriptLib;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.apache.logging.log4j.core.script.Script;

import java.io.*;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModuleManager {
    private final Map<Class<? extends Module>, Module> modules = new LinkedHashMap<>();
    private final File configFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ModuleManager(File dataDir){
        this.configFile = new File(dataDir, "modules.json");
        addModule(new FPSModule());
        addModule(new ScriptLibModule());
        addModule(new ScriptModule());
    }

    private void addModule(Module module){
        modules.put(module.getClass(), module);
        Flow.EVENT_BUS.register(module);
    }

    // モジュール取得
    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz){
        return (T) modules.get(clazz);
    }

    // すべてのモジュール取得
    public Collection<Module> getModules(){
        return modules.values();
    }

    public void saveConfig(){
        JsonObject json = new JsonObject();
        for(Module module : modules.values()){
            json.add(module.name, module.saveAll());
        }
        try(Writer writer = new FileWriter(configFile)){
            gson.toJson(json, writer);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void loadConfig(){
        if(!configFile.exists()) return;
        try(Reader reader = new FileReader(configFile)){
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            for(Module module : modules.values()){
                if(json.has(module.name)){
                    module.loadAll(json.getAsJsonObject(module.name));
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

//    @Subscribe
//    public void onRender(Render2DEvent event){
//        this.render(event.getContext());
//    }

//    public void render(DrawContext context){
//        for(Module module : modules.values()){
//            if(!module.isEnabled()) continue;
//            if(module instanceof HudModule hud){
//                hud.render(context);
//            }
//        }
//    }

    public void reloadScripts(){
        for(Module module : modules.values()){
            if(!module.isEnabled()) continue;
            if(module instanceof ScriptModule script){
                script.reload();
            }
        }
    }

    public void registerScriptLibs(){
        for(Module module : modules.values()){
            if(module instanceof ScriptModule script){
                FlowScriptLib.registerAll(((ScriptModule) module).getInterpreter());
            }
        }
    }
}
