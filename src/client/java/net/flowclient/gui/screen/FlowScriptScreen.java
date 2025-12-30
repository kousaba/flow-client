package net.flowclient.gui.screen;

import net.flowclient.gui.widget.editor.CodeEditorWidget;
import net.flowclient.script.ScriptManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class FlowScriptScreen extends Screen {
    private int sidebarWidth;
    private int consoleHeight;
    private String currentFileName = "test.flow";

    private CodeEditorWidget editorWidget;

    public FlowScriptScreen(){
        super(Text.of("FlowScript Editor"));
    }

    @Override
    protected void init(){
        // 左のリスト幅
        this.sidebarWidth = this.width / 4;
        // 下のコンソールの高さ
        this.consoleHeight = this.height / 4;
        int editorX = sidebarWidth;
        int editorY = 0;
        int editorWidth = this.width - sidebarWidth;
        int editorHeight = this.height - consoleHeight;

        // エディタウィジェットの作成
        this.editorWidget = new CodeEditorWidget(editorX, editorY, editorWidth, editorHeight);
        this.addDrawableChild(editorWidget);

        int btnX = this.width - 60;
        int btnY = 5;
        this.addDrawableChild(ButtonWidget.builder(Text.of("Save"), button -> {
            saveCurrentFile();
        }).dimensions(btnX, btnY, 50, 20).build());

        String content = ScriptManager.loadScript(currentFileName);
        if(!content.isEmpty()){
            editorWidget.setText(content);
        }

        // フォーカスをエディタに当てる
        this.setFocused(editorWidget);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){
        this.renderBackground(context, mouseX, mouseY, delta);

        // サイドバー描画
        context.fill(0,0,sidebarWidth,this.height, 0xFF202020);
        context.drawStrokedRectangle(0,0,sidebarWidth,this.height,0xFF404040);

        // コンソールエリア描画
        int consoleY = this.height - consoleHeight;
        context.fill(sidebarWidth, consoleY, this.width, this.height, 0xFF101015);
        context.drawStrokedRectangle(sidebarWidth, consoleY, this.width - sidebarWidth, consoleHeight, 0xFF404040);

        context.drawText(this.textRenderer, "Console / Output", sidebarWidth + 5, consoleY + 5, 0xFFAAAAAA, true);

        // ウィジェット描画
        super.render(context, mouseX, mouseY, delta);

        // タイトルやヘッダーの描画
        context.drawText(this.textRenderer, "Scripts", 10, 10, 0xFFFFFFFF, true);
        context.drawText(this.textRenderer, "> HUD_FPS.flow", 15, 30, 0xFF55FF55, true);
        context.drawText(this.textRenderer, "  AutoGG.flow", 15, 45, 0xFFAAAAAA, true);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta){
        context.fill(0,0,this.width,this.height, 0xFF111111);
    }

    @Override
    public boolean keyPressed(KeyInput input){
        if(input.getKeycode() == GLFW.GLFW_KEY_ESCAPE){
            this.close();
            return true;
        } else if(input.getKeycode() == GLFW.GLFW_KEY_S && (input.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0){
            saveCurrentFile();
            return true;
        }
        return super.keyPressed(input);
    }

    private void saveCurrentFile(){
        if(editorWidget != null){
            ScriptManager.saveScript(currentFileName, editorWidget.getText());
        }
    }
}
