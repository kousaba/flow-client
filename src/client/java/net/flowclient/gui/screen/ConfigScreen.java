package net.flowclient.gui.screen;

import net.flowclient.Flow;
import net.flowclient.module.HudModule;
import net.flowclient.module.Module;
import net.flowclient.module.ModuleManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ConfigScreen extends Screen {
    private double scrollOffset = 0;
    private HudModule draggingModule = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private final int windowWidth;
    private final int windowHeight;
    private final int windowX;
    private final int windowY;
    private final int itemHeight = 25;

    public ConfigScreen(int windowWidth, int windowHeight, int windowX, int windowY){
        super(Text.of("Ally Config"));
        this.windowWidth = (int) (this.width / 2.5);
        this.windowHeight = (int) (this.height / 2.5);
        this.windowX = (this.width - windowWidth) / 2;
        this.windowY = (this.height - windowHeight) / 2;
    }

    @Override
    protected void init(){
        super.init();
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
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    private void renderBackGrounds(DrawContext context){
        // 全体の背景
        context.fill(windowX, windowY, windowX + windowWidth, windowY + windowHeight, 0x80101010);
        // 上の少し濃い背景
        int barWidth = windowWidth / 10;
        int barHeight = windowHeight;
        context.fill(windowX, windowY, windowX + barWidth, windowY + barHeight, 0xAA101010);
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

    // マウスをクリックしたときの処理
    public boolean mouseClicked(Click click, boolean doubled){
        if(click.x() >= windowX && click.x() <= windowX + windowWidth && click.y() >= windowY && click.y() <= windowY + windowHeight){
            int startY = windowY + 40;
            int currentY = startY - (int)scrollOffset;
            for(Module module : Flow.INSTANCE.moduleManager.getModules()){
                if(currentY < windowY + 30 || currentY > windowY + windowHeight - 30){
                    currentY += itemHeight;
                    continue;
                }
                // ボタン設定

            }
        }
    }
}
