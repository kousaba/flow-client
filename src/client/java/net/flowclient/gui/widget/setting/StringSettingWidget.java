package net.flowclient.gui.widget.setting;

import net.flowclient.module.setting.impl.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class StringSettingWidget extends SettingWidget<String, StringSetting> {
    private final TextFieldWidget textField; // 内部にマイクラ標準の入力欄を持つ

    public StringSettingWidget(int x, int y, int width, int height, StringSetting setting) {
        super(x, y, width, height, setting);

        // 右側に 2/3 の幅で入力欄を作る
        int fieldWidth = (int)(width * 0.6);
        this.textField = new TextFieldWidget(
                MinecraftClient.getInstance().textRenderer,
                x + (width - fieldWidth), y, fieldWidth, height, Text.empty()
        );
        this.textField.setText(setting.getData());
        this.textField.setChangedListener(setting::setData);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. ラベル（名前）を描画
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        context.drawText(tr, getMessage(), getX(), getY() + (height - 8) / 2, 0xFFFFFFFF, true);

        // 2. 内部のテキストフィールドを描画
        this.textField.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    // マウス・キーボードの入力を内部のテキストフィールドに伝える
    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if(textField != null){
            boolean clicked = this.textField.mouseClicked(click, doubled);
            this.textField.setFocused(clicked);
            if(clicked) return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        return this.textField.keyPressed(input) || super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        return this.textField.charTyped(input);
    }

    @Override
    public void setFocused(boolean focused){
        super.setFocused(focused);
        if(this.textField != null){
            this.textField.setFocused(focused);
        }
    }

    @Override
    public void updateValue(){
        if(this.textField != null && !this.textField.isFocused()){
            this.textField.setText(setting.getData());
        }
    }
}