package net.flowclient.module.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.flowclient.module.setting.Setting;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean defaultData){
        super(name, defaultData);
    }
    public void toggle(){
        this.data = !data;
    }
    @Override
    public JsonElement save(){
        return new JsonPrimitive(this.data);
    }
    @Override
    public void load(JsonElement element){
        if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()){
            setData(element.getAsBoolean());
        }
    }
}
