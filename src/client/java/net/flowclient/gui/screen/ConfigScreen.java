package net.flowclient.gui.screen;

import net.flowclient.Flow;
import net.flowclient.gui.widget.WidgetFactory;
import net.flowclient.gui.widget.setting.*;
import net.flowclient.module.HudModule;
import net.flowclient.module.Module;
import net.flowclient.module.setting.Setting;
import net.flowclient.module.setting.impl.BooleanSetting;
import net.flowclient.module.setting.impl.ColorSetting;
import net.flowclient.module.setting.impl.NumberSetting;
import net.flowclient.module.setting.impl.StringSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ConfigScreen extends Screen {
    private double scrollOffset = 0;
    private HudModule draggingModule = null;
    private Module selectedModule = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private int sidebarWidth;
    private int windowWidth;
    private int windowHeight;
    private int windowX;
    private int windowY;
    private final int itemHeight = 25;

    public ConfigScreen() {
        super(Text.of("Ally Config"));
    }

    @Override
    protected void init(){
        this.windowWidth = (int) (this.width / 2.5);
        this.windowHeight = (int) (this.height / 2.5);
        this.windowX = (this.width - this.windowWidth) / 2;
        this.windowY = (this.height - this.windowHeight) / 2;
        this.sidebarWidth = this.windowWidth / 3;
        this.refreshSettings();
    }

    // ウィジェットをクリアして再構築する
    public void refreshSettings(){
        this.clearChildren();
        if (selectedModule != null) {
            List<Setting<?>> settings = new ArrayList<>(selectedModule.getAllSettings());
            settings.sort(Comparator.comparingInt(Setting::getPriority));
            int settingX = windowX + sidebarWidth + 10;
            int settingY = windowY + 40;
            int widgetWidth = windowWidth - sidebarWidth - 40;
            for(Setting<?> setting : settings){
                SettingWidget<?, ?> widget = WidgetFactory.create(settingX, settingY, widgetWidth, 20, setting);
                this.addDrawableChild(widget);
                settingY += 25;
            }
        }
    }

    @Override
    public void removed(){
        draggingModule = null;
        super.removed();
        // モジュールの設定をセーブ
        Flow.INSTANCE.moduleManager.saveConfig();
    }

    // ゲームを一時停止しない
    @Override
    public boolean shouldPause(){
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks){
        renderBackGrounds(context);
        // モジュール内のリスト描画
        TextRenderer textRenderer = this.client.textRenderer;
        int currentY = windowY + 40;

        for(Module m : Flow.INSTANCE.moduleManager.getModules()){
            int color = (m == selectedModule) ? 0xFF55FFFF : 0xFFFFFFFF;
            context.drawText(textRenderer, m.name, windowX + 10, currentY, color, true);
            currentY += itemHeight;
        }

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    private void renderBackGrounds(DrawContext context){
        // 全体の背景
        context.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, 0x80101010);
        context.fill(windowX, windowY, windowX + sidebarWidth, windowY + windowHeight, 0xAA101010);
    }

    // キー入力(ESCで閉じたりする処理)
    @Override
    public boolean keyPressed(KeyInput input){
        int keyCode = input.key();
        if(keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()){
            this.close();
            return true;
        }
        return super.keyPressed(input);
    }
    @Override
    public boolean charTyped(CharInput input){
        return super.charTyped(input);
    }

    // マウスをクリックしたときの処理
    @Override
    public boolean mouseClicked(Click click, boolean doubled){
        if(click.x() >= windowX && click.x() <= windowX + sidebarWidth && click.y() >= windowY && click.y() <= windowY + windowHeight){
            // 左のバーにあるモジュールをクリックしたらselectedModuleを切り替える。
            int startY = windowY + 40;
            int currentY = startY - (int)scrollOffset;
            for(Module module : Flow.INSTANCE.moduleManager.getModules()){
                if(click.y() >= currentY && click.y() <= currentY + itemHeight){
                    this.selectedModule = module;
                    this.refreshSettings();
                    System.out.println("Clicked bar!");
                    return true;
                }
                currentY += itemHeight;
            }
        }else{
            // ドラッグでモジュール移動
            for(Module module : Flow.INSTANCE.moduleManager.getModules()){
                if(module instanceof HudModule hud && hud.isEnabled()){
                    if(hud.isHovered(click.x(), click.y())){
                        this.draggingModule = hud;
                        this.dragOffsetX = (int) (click.x() - hud.getSetting("x", NumberSetting.class).getData());
                        this.dragOffsetY = (int) (click.y() - hud.getSetting("y", NumberSetting.class).getData());
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click){
        this.draggingModule = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY){
        if(this.draggingModule != null){
            // マウスの現在位置から、オフセットを引いて座標計算
            double newX = click.x() - dragOffsetX;
            double newY = click.y() - dragOffsetY;

            // 設定値を更新
            draggingModule.getSetting("x", NumberSetting.class).setData(newX);
            draggingModule.getSetting("y", NumberSetting.class).setData(newY);

            syncWidgetValues();

            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= verticalAmount * 20;
        if (scrollOffset < 0) scrollOffset = 0;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // 画面全体を 40% くらいの黒で塗りつぶす
        context.fill(0, 0, this.width, this.height, 0x60000000);
    }

    // 値を同期する
    private void syncWidgetValues(){
        for(var element : this.children()){
            if(element instanceof SettingWidget<?,?> settingWidget){
                settingWidget.updateValue();
            }
        }
    }
}
