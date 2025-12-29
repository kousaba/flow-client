package net.flowclient.module.setting;

import com.google.gson.JsonElement;

public abstract class Setting<T> {
    private final String name; // 設定名
    protected T data;
    public Setting(String name, T defaultData){
        this.name = name;
        this.data = defaultData;
        this.priority = Integer.MAX_VALUE; // デフォルトはint_max
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

    private int priority = 0;
    public int getPriority() {return priority;}
    public Setting<T> setPriority(int p) {this.priority = p; return this;}
}
