package net.flowclient.gui.widget;

import net.flowclient.module.setting.impl.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

public class NumberSettingWidget extends SettingWidget<Double, NumberSetting> {
    // スライダー用
    private boolean isSliding = false;
    // テキスト入力用
    private TextFieldWidget textField = null;

    public NumberSettingWidget(int x, int y, int width, int height, NumberSetting setting){
        super(x,y,width,height, setting);
        if(!setting.hasBounds()){
            this.textField = new TextFieldWidget(
                    MinecraftClient.getInstance().textRenderer,
                    x + width / 2, y, width / 2, height, Text.empty()
            );
            this.textField.setMaxLength(10);
            this.textField.setText(String.valueOf(setting.getData()));
            this.textField.setChangedListener(this::onTextChanged);
        }
    }

    private void onTextChanged(String text){
        try{
            double val = Double.parseDouble(text);
            setting.setData(val);
        } catch (NumberFormatException e){
            // 数値ではない場合は無視
            setting.setData(0.0);
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta){
        if(setting.hasBounds()){
            renderSlider(context, mouseX, mouseY);
        }else{
            renderTextField(context, mouseX, mouseY, delta);
        }
    }

    private void renderSlider(DrawContext context, int mouseX, int mouseY){
        Double minVal = setting.getMinValue();
        Double maxVal = setting.getMaxValue();
        Double currentVal = setting.getData();
        if(minVal == null || maxVal == null || currentVal == null){
            throw new NullPointerException("minVal, maxVal, data must not be null.");
        }

        // 現在の値の割合
        double min = minVal;
        double max = maxVal;
        double current = currentVal;
        double renderRatio = (current - min) / (max - min);

        context.fill(getX(), getY(), getX() + width, getY() + height, 0xFF222222);

        // スライダーのバー
        int sliderWidth = (int) (width * renderRatio);
        context.fill(getX(), getY(), getX() + sliderWidth, getY() + height, 0xFF5555FF);

        // テキスト表示
        String displayValue = String.format("%.2f", current);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawText(textRenderer, getMessage().getString() + ": " + displayValue, getX() + 4, getY() + (height - 8) / 2, 0xFFFFFFFF, true);
    }
    private void renderTextField(DrawContext context, int mouseX, int mouseY, float delta){
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawText(textRenderer, getMessage(), getX(), getY() + (height - 8) / 2, 0xFFFFFFFF, true);
        this.textField.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled){
        if(textField != null){
            boolean clicked = this.textField.mouseClicked(click, doubled);
            if(clicked){
                this.textField.setFocused(true);
                return true;
            }
        }
        if(setting.hasBounds()){
            this.isSliding = true;
            updateSliderValue(click.x());
            return true;
        }
        if(textField != null){
            return textField.mouseClicked(click, doubled);
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click){
        this.isSliding = false;
        return super.mouseReleased(click);
    }

    @Override
    public void onDrag(Click click, double offsetX, double offsetY){
        if(this.isSliding){
            updateSliderValue(click.x());
        }
    }

    private void updateSliderValue(double mouseX){
        Double minVal = setting.getMinValue();
        Double maxVal = setting.getMaxValue();
        if (minVal == null || maxVal == null) {
            throw new NullPointerException("minVal and maxVal must not be null");
        }
        double min = minVal;
        double max = maxVal;

        // マウス位置から割合を計算(0~1)
        double diff = Math.min(Math.max((mouseX - getX()) / (double) width, 0.0), 1.0);

        // 割合を値に変換
        double newValue = min + (diff * (max - min));
        setting.setData(newValue);
    }

    @Override
    public boolean keyPressed(KeyInput input){
        if(textField != null) return textField.keyPressed(input);
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input){
        if(textField != null) return textField.charTyped(input);
        return super.charTyped(input);
    }

    @Override
    public void setFocused(boolean focused){
        super.setFocused(focused);
        if(this.textField != null){
            this.textField.setFocused(focused);
        }
    }

    // 設定をもう一度読み込んで値を更新
    @Override
    public void updateValue(){
        if(this.textField != null && !this.textField.isFocused()){
            String formatted = String.format("%.2f", setting.getData());
            this.textField.setText(formatted);
        }
    }
}
