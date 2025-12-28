package net.flowclient.module.impl;

import net.flowclient.module.Module;
import net.flowclient.module.setting.impl.NumberSetting;
import net.minecraft.client.gui.DrawContext;

public abstract class HudModule extends Module {
    public HudModule(String name){
        super(name);
        // 表示する座標
        addSetting(new NumberSetting("x", 0.0));
        addSetting(new NumberSetting("y", 0.0));
    }

    public abstract void render(DrawContext context);

    // マウス上に存在するか
    public boolean isHovered(double mouseX, double mouseY){
        NumberSetting xSetting = getSetting("x", NumberSetting.class);
        NumberSetting ySetting = getSetting("y", NumberSetting.class);
        NumberSetting heightSetting = getSetting("height", NumberSetting.class);
        NumberSetting widthSetting = getSetting("width", NumberSetting.class);
        double x = xSetting.getData(), y = ySetting.getData(), h = heightSetting.getData(), w = widthSetting.getData();
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
