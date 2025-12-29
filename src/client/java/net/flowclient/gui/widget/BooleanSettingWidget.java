package net.flowclient.gui.widget;

import net.flowclient.module.setting.impl.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class BooleanSettingWidget extends ClickableWidget {
    private final BooleanSetting setting;

    public BooleanSettingWidget(int x, int y, int width, int height, BooleanSetting setting){
        super(x,y,width,height, Text.of(setting.getName()));
        this.setting = setting;
    }

    @Override
    public void onClick(Click click, boolean doubled){
        System.out.println("Click!!");
        setting.toggle();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta){
        // 背景描画
        int alpha = 0x80;
        int baseColor = isHovered() ? 0x555555 : 0x222222;
        int color = (alpha << 24) | baseColor;

        context.fill(getX(), getY(), getX() + width, getY() + height, color);

        // テキスト描画
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        boolean enabled = setting.getData();
        String label = setting.getName() + ": " + (enabled ? "ON" : "OFF");

        int statusColor = enabled ? 0xFF55FF55 : 0xFFFF5555;

        context.drawText(textRenderer, Text.literal(label), getX() + 5, getY() + (height - 8) / 2, statusColor, true);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    @Override
    public void setFocused(boolean focused){
        super.setFocused(focused);
    }
}
