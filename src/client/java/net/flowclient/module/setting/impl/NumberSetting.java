package net.flowclient.module.setting.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.flowclient.module.setting.Setting;

public class NumberSetting extends Setting<Double> {
    private final Double min_value;
    private final Double max_value;
    public NumberSetting(String name, Double defaultData){
        this(name, defaultData, null, null);
    }
    public NumberSetting(String name, Double defaultData, Double min_value, Double max_value){
        super(name, defaultData);
        this.min_value = min_value;
        this.max_value = max_value;
    }
    @Override
    public void setData(Double data){
        if(min_value == null && max_value == null) {
            this.data = Math.clamp(data, this.min_value, this.max_value);
        }else{
            this.data = data;
        }
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
