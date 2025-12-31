package net.flowclient.module;

import net.flowclient.module.HudModule;
import net.flowclient.module.setting.impl.BooleanSetting;
import net.flowclient.module.setting.impl.ColorSetting;
import net.flowclient.module.setting.impl.NumberSetting;
import net.flowclient.module.setting.impl.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.Map;

public abstract class TextHudModule extends HudModule {
    // 置き換えるテキスト($fpsなど)
    List<String> placeHolder;
    public TextHudModule(String name, int x, int y, String defaultFormat, List<String> placeHolder){
        super(name,x,y);
        addSetting(new ColorSetting("color", 0xFFFFFFFF));
        addSetting(new BooleanSetting("shadow", true));
        addSetting(new StringSetting("format", defaultFormat));
        this.placeHolder = placeHolder;
    }

    // placeHolderを置き換えるテキスト
    public abstract Map<String, String> getValue();

    public String getDisplayText() {
        String format = getSetting("format", StringSetting.class).getData();
        Map<String, String> replaceMap = getValue();
        if (replaceMap == null) return "";

        String result = format;
        for (String key : placeHolder) {
            if (replaceMap.containsKey(key)) {
                // "$(fps)" という文字列を "60" などに置換する
                result = result.replace("$(" + key + ")", replaceMap.get(key));
            }
        }
        return result;
    }

    public double getWidth(){
        String text = getDisplayText();
        return mc.textRenderer.getWidth(text);
    }

    public double getHeight(){
        String text = getDisplayText();
        return mc.advanceValidatingTextRenderer.fontHeight;
    }

    @Override
    public void render(DrawContext context){
        String text = getDisplayText();
        int color = getSetting("color", ColorSetting.class).getData();
        boolean shadow = getSetting("shadow", BooleanSetting.class).getData();
        double x = getSetting("x", NumberSetting.class).getData();
        double y = getSetting("y", NumberSetting.class).getData();
        context.drawText(mc.textRenderer, text, (int) x, (int) y, color, shadow);
    }
}
