package net.flowclient.gui.widget.setting;

import net.flowclient.module.setting.Setting;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public abstract class SettingWidget<T, S extends Setting<T>> extends ClickableWidget {
    protected final S setting;

    public SettingWidget(int x, int y, int width, int height, S setting){
        super(x,y,width,height, Text.literal(setting.getName()));
        this.setting = setting;
    }

    // 設定値から値を更新
    public abstract void updateValue();

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
