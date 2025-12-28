package net.flowclient.module;

import com.google.gson.JsonObject;
import net.flowclient.module.setting.Setting;
import net.flowclient.module.setting.impl.BooleanSetting;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Module {
    public String name;
    private final Map<String, Setting<?>> settings = new LinkedHashMap<>();
    public Module(String name){
        this.name = name;
        this.addSetting(new BooleanSetting("enabled", false));
    }
    public void toggle(){
        BooleanSetting isEnabled = this.getSetting("enabled", BooleanSetting.class);
        isEnabled.toggle();
        if(isEnabled.getData()) onEnable();
        else onDisable();
    }
    protected void addSetting(Setting<?> setting){
        this.settings.put(setting.getName(), setting);
    }
    protected void onEnable(){}
    protected void onDisable(){}
    public JsonObject saveAll(){
        JsonObject json = new JsonObject();
        for(Setting<?> s : settings.values()){
            json.add(s.getName(), s.save());
        }
        return json;
    }
    public void loadAll(JsonObject object){
        for(Setting<?> s : settings.values()){
            String settingName = s.getName();
            if(object.has(settingName)){
                s.load(object.get(settingName));
            }
        }
    }
    @SuppressWarnings("unchecked")
    public <T extends Setting<?>> T getSetting(String name, Class<T> clazz){
        return (T) settings.get(name);
    }
}
