package net.flowclient.module.setting;

import com.google.gson.JsonElement;

public abstract class Setting<T> {
    private final String name; // 設定名
    protected T data;
    public Setting(String name, T defaultData){
        this.name = name;
        this.data = defaultData;
    }
    public String getName(){
        return name;
    }
    public T getData(){
        return data;
    };
    public void setData(T data){
        this.data = data;
    };
    // セーブ、ロード
    public abstract JsonElement save();
    public abstract void load(JsonElement element);
}
