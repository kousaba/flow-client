package net.flowclient.module.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.flowclient.module.setting.Setting;

public class NumberSetting extends Setting {
    private double data;
    private double min_value;
    private double max_value;
    public NumberSetting(String name, double defaultData, double min_value, double max_value){
        super(name);
        this.data = defaultData;
        this.min_value = min_value;
        this.max_value = max_value;
    }
    public double getData(){
        return data;
    }
    public void setData(double data){
        this.data = Math.clamp(data, this.min_value, this.max_value);
    }
    public double getMinValue() {return this.min_value;}
    public double getMaxValue() {return this.max_value;}
    @Override
    public JsonElement save(){
        // min, maxは入れなくても良い
        return new JsonPrimitive(this.data);
    }
    @Override
    public void load(JsonElement element){
        if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()){
            setData(element.getAsDouble());
        }
    }
}
