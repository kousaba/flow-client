package net.flowclient.gui.screen;

import net.flowclient.Flow;
import net.flowclient.gui.widget.BooleanSettingWidget;
import net.flowclient.gui.widget.ColorSettingWidget;
import net.flowclient.gui.widget.NumberSettingWidget;
import net.flowclient.gui.widget.StringSettingWidget;
import net.flowclient.module.HudModule;
import net.flowclient.module.Module;
import net.flowclient.module.ModuleManager;
import net.flowclient.module.setting.Setting;
import net.flowclient.module.setting.impl.BooleanSetting;
import net.flowclient.module.setting.impl.ColorSetting;
import net.flowclient.module.setting.impl.NumberSetting;
import net.flowclient.module.setting.impl.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

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
            int settingX = windowX + sidebarWidth + 10;
            int settingY = windowY + 40;
            int widgetWidth = windowWidth - sidebarWidth - 40;
            for(Setting<?> setting : selectedModule.getAllSettings()){
                if(setting instanceof BooleanSetting bool) this.addDrawableChild(new BooleanSettingWidget(settingX, settingY, widgetWidth, 20, bool));
                else if(setting instanceof NumberSetting num) this.addDrawableChild(new NumberSettingWidget(settingX, settingY, widgetWidth, 20, num));
                else if(setting instanceof StringSetting str) this.addDrawableChild(new StringSettingWidget(settingX, settingY, widgetWidth, 20, str));
                else if(setting instanceof ColorSetting clr) this.addDrawableChild(new ColorSettingWidget(settingX, settingY, widgetWidth, 20, clr));
                else{
                    throw new UnsupportedOperationException("Unsupported setting type: " + setting.getClass().getName());
                }
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
            }
        }else{
            // ドラッグでモジュール移動
            for(Module module : Flow.INSTANCE.moduleManager.getModules()){
                if(module instanceof HudModule hud){
                    if(hud.isEnabled() && hud.isHovered(click.x(), click.y())){
                        this.draggingModule = hud;
                        this.dragOffsetX = (int) (click.x() - hud.getSetting("x", NumberSetting.class).getData());
                        this.dragOffsetY = (int) (click.y() - hud.getSetting("y", NumberSetting.class).getData());
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= verticalAmount * 20;
        if (scrollOffset < 0) scrollOffset = 0;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
