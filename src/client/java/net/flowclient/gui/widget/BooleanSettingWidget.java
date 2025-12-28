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
        if(doubled) return;
        setting.toggle();
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta){
        // 背景描画
        int color = isHovered() ? 0xFF555555 : 0xFF333333; // ホバー時は少し明るく
        context.fill(getX(), getY(), getX() + width, getY() + height, 0xFF000000); // 枠線
        context.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, color); // 内側

        // ステータス表示
        int statusColor = setting.getData() ? 0xFF55FF55 : 0xFFFF5555;
        // 内側に小さな四角形を表示
        int indicatorSize = height - 4;
        context.fill(getX() + width - indicatorSize - 2, getY() + 2, getX() + width - 2, getY() + height - 2, statusColor);

        // テキスト描画
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawText(textRenderer, getMessage(), getX() + 4, getY() + (height - 8) / 2, 0xFFFFFF, true);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
