package net.flowclient.module.setting;

import com.google.gson.JsonElement;

public abstract class Setting {
    public String name; // 設定名
    public Setting(String name){
        this.name = name;
    }
    // セーブ、ロード
    public abstract JsonElement save();
    public abstract void load(JsonElement element);
}
