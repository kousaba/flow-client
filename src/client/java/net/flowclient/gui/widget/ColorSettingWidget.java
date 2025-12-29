package net.flowclient.gui.widget;

import net.flowclient.module.setting.impl.ColorSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Narratable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class ColorSettingWidget extends ClickableWidget {
    private final ColorSetting setting;
    private final TextFieldWidget textField;

    public ColorSettingWidget(int x, int y, int width, int height, ColorSetting setting){
        super(x,y,width,height, Text.of(setting.getName()));
        this.setting = setting;

        // 入力欄の幅調整
        int fieldWidth = width / 3;
        this.textField = new TextFieldWidget(
                MinecraftClient.getInstance().textRenderer,
                x + (width - fieldWidth - 30), y, fieldWidth, height, Text.empty()
        );

        // 現在の色を16進数文字列にして、初期値として入力
        this.textField.setText(String.format("%08X", setting.getData()));

        // テキストが書き換えられたときの処理
        this.textField.setChangedListener(text -> {
            try{
                long colorLong = Long.parseLong(text, 16);
                setting.setData((int) colorLong);
            } catch (NumberFormatException e){
                // 更新しない
            }
        });
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta){
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        // ラベル描画
        context.drawText(textRenderer, getMessage(), getX(), getY() + (height - 8) / 2, 0xFFFFFFFF, true);

        // テキスト入力欄を描画
        this.textField.render(context, mouseX, mouseY, delta);

        // 色プレビュー表示
        int previewX = getX() + width - 25;
        int previewY = getY() + 2;
        int previewSize = height - 4;

        context.fill(previewX - 1, previewY - 1, previewX + previewSize + 1, previewY + previewSize + 1, 0xFF000000);
        context.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, setting.getData());
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled){
        return this.textField.mouseClicked(click, doubled) || super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input){
        return this.textField.keyPressed(input) || super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input){
        return this.textField.charTyped(input);
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
