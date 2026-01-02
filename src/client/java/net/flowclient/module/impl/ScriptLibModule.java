package net.flowclient.module.impl;

import net.flowclient.module.Module;
import net.flowclient.module.setting.impl.NumberSetting;

public class ScriptLibModule extends Module {
    public ScriptLibModule(){
        super("ScriptSettings");
        addSetting(new NumberSetting("FPS_Interval", 0.5, 0.0, 5.0));
        addSetting(new NumberSetting("Ping_Interval", 1.0, 0.0, 10.0));
        addSetting(new NumberSetting("Pos_Interval", 0.1, 0.0, 2.0));
    }
}
