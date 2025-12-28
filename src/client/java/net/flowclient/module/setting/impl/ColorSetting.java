package net.flowclient.module.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.flowclient.module.setting.Setting;

public class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, int defaultColor){
        super(name, defaultColor);
    }
    @Override
    public JsonElement save(){
        return new JsonPrimitive(this.data);
    }
    @Override
    public void load(JsonElement element){
        if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()){
            this.data = element.getAsInt();
        }
    }
}
