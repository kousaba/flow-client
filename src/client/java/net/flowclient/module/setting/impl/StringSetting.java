package net.flowclient.module.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.flowclient.module.setting.Setting;

public class StringSetting extends Setting {
    private String data;
    public StringSetting(String name, String defaultData){
        super(name);
        this.data = defaultData;
    }
    public String getData(){
        return data;
    }
    @Override
    public JsonElement save(){
        return new JsonPrimitive(this.data);
    }
    @Override
    public void load(JsonElement element){
        if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()){
            this.data = element.getAsString();
        }
    }
}
