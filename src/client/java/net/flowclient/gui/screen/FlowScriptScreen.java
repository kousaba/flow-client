package net.flowclient.gui.screen;

import it.unimi.dsi.fastutil.chars.CharIntBiConsumer;
import net.flowclient.Flow;
import net.flowclient.gui.widget.editor.CodeEditorWidget;
import net.flowclient.module.impl.ScriptModule;
import net.flowclient.script.ScriptManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FlowScriptScreen extends Screen {
    private int sidebarWidth;
    private int consoleHeight;
    private String currentFileName = "main.flow";
    private List<String> fileList = new ArrayList<>();
    private int selectedFileIndex = -1;

    private static List<String> consoleLogs = new ArrayList<>();

    private final int itemHeight = 15;
    private final int listTopOffset = 30;
    private TextFieldWidget fileNameField;
    private ButtonWidget renameButton;

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

        int fieldY = 25;
        this.fileNameField = new TextFieldWidget(this.textRenderer, 5, fieldY, sidebarWidth - 35, 15, Text.of("File Name"));
        this.fileNameField.setMaxLength(32);
        this.fileNameField.setText(currentFileName);
        this.addDrawableChild(fileNameField);
        this.renameButton = ButtonWidget.builder(Text.of("R"), button -> {
            performRename();
        }).dimensions(sidebarWidth - 28, fieldY, 24, 15).build();
        this.addDrawableChild(renameButton);

        // エディタウィジェットの作成
        this.editorWidget = new CodeEditorWidget(editorX, editorY, editorWidth, editorHeight);
        this.addDrawableChild(editorWidget);

        refreshFileList();
        loadCurrentFile();

        int btnX = this.width - 60;
        int btnY = 5;
        this.addDrawableChild(ButtonWidget.builder(Text.of("Save"), button -> {
            saveCurrentFile();
        }).dimensions(btnX, btnY, 50, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.of("+ New"), button -> {
            createNewFile();
        }).dimensions(5, this.height - consoleHeight - 25, sidebarWidth - 10, 20).build());

        String content = ScriptManager.loadScript(currentFileName);
        if(!content.isEmpty()){
            editorWidget.setText(content);
        }

        // フォーカスをエディタに当てる
        this.setFocused(editorWidget);
    }

    private void performRename(){
        String newName = fileNameField.getText().trim();
        if(!newName.endsWith(".flow")) newName += ".flow";
        if(newName.equals(currentFileName) || newName.equals(".flow")) return;
        if(ScriptManager.renameScript(currentFileName, newName)){
            this.currentFileName = newName;
            refreshFileList();
            fileNameField.setText(newName);
        }else{
            fileNameField.setText(currentFileName);
        }
    }

    private void refreshFileList(){
        this.fileList = ScriptManager.getScriptFiles();
        // ファイルが見つからない場合はデフォルトのファイル(main.flow)を作成
        if(this.fileList.isEmpty()){
            ScriptManager.saveScript("main.flow", "// Hello FlowScript!\nfn on_tick() {\n    text = \"Hello\";\n}");
            this.fileList = ScriptManager.getScriptFiles();
        }
        this.selectedFileIndex = -1;
        for(int i = 0;i < fileList.size();i++){
            if(fileList.get(i).equals(currentFileName)){
                this.selectedFileIndex = i;
                break;
            }
        }
    }

    private void loadCurrentFile(){
        String content = ScriptManager.loadScript(currentFileName);
        if(editorWidget != null){
            editorWidget.setText(content);
        }
    }

    private void saveCurrentFile(){
        if(editorWidget != null){
            ScriptManager.saveScript(currentFileName, editorWidget.getText());
            ScriptModule module = Flow.INSTANCE.moduleManager.getModule(ScriptModule.class);
            if(module != null){
                module.reload();
            }
        }
    }

    private void createNewFile(){
        int count = 1;
        String newName = "script" + count + ".flow";
        while(fileList.contains(newName)){
            count++;
            newName = "script" + count + ".flow";
        }
        ScriptManager.saveScript(newName, "");
        this.currentFileName = newName;
        refreshFileList();
        loadCurrentFile();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){
        this.renderBackground(context, mouseX, mouseY, delta);

        // サイドバー描画
        context.fill(0,0,sidebarWidth,this.height, 0xFF202020);
        context.drawStrokedRectangle(0,0,sidebarWidth,this.height,0xFF404040);

        // ファイルリスト描画
        context.drawText(this.textRenderer, "Scripts", 10, 10, 0xFFFFFFFF, true);
        for(int i = 0;i < fileList.size();i++){
            String fileName = fileList.get(i);
            int itemY = listTopOffset + (i * itemHeight);
            if(i == selectedFileIndex) context.fill(2, itemY - 2, sidebarWidth - 2, itemY + itemHeight - 2, 0xFF404040);
            else if(mouseX >= 0 && mouseX <= sidebarWidth && mouseY >= itemY - 2 && mouseY <= itemY + itemHeight - 2) context.fill(2, itemY - 2, sidebarWidth - 2, itemY + itemHeight - 2, 0xFF303030);
            int color = i == selectedFileIndex ? 0xFF55FF55 : 0xFFAAAAAA;
            context.drawText(this.textRenderer, fileName, 15, itemY, color, true);
        }

        // コンソールエリア描画
        int consoleY = this.height - consoleHeight;
        context.fill(sidebarWidth, consoleY, this.width, this.height, 0xFF101015);
        context.drawStrokedRectangle(sidebarWidth, consoleY, this.width - sidebarWidth, consoleHeight, 0xFF404040);
        context.drawText(this.textRenderer, "Console / Output", sidebarWidth + 5, consoleY + 5, 0xFFAAAAAA, true);
        for(int i = 0;i < consoleLogs.size();i++){
            String text = consoleLogs.get(consoleLogs.size() - i - 1);
            context.drawText(this.textRenderer, text, sidebarWidth + 5, consoleY + 5 + (textRenderer.fontHeight + 2) * (i + 1), 0xFFAAAAAA, true);
        }


        // ウィジェット描画
        super.render(context, mouseX, mouseY, delta);
    }

    public static void addLog(String msg){
        consoleLogs.add(msg);
        if(consoleLogs.size() > 10) consoleLogs.removeFirst();
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
        if(fileNameField.isFocused()){
            if(input.getKeycode() == GLFW.GLFW_KEY_ENTER){
                performRename();
                return true;
            }
            return fileNameField.keyPressed(input);
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled){
        // サイドバーのクリック判定
        if(click.x() >= 0 && click.x() <= sidebarWidth){
            // どのインデックスの位置をクリックしたか計算
            int clickedIndex = ((int)click.y() - listTopOffset + 2) / itemHeight;
            // 有向な範囲か
            if(clickedIndex >= 0 && clickedIndex < fileList.size()){
                // ファイルをロード
                String clickedFile = fileList.get(clickedIndex);
                if(!clickedFile.equals(currentFileName)){
                    this.currentFileName = clickedFile;
                    this.selectedFileIndex = clickedIndex;
                    loadCurrentFile();
                    return true;
                }
            }
        }
        if(fileNameField.mouseClicked(click, doubled)){
            this.setFocused(fileNameField);
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean charTyped(CharInput input){
        if(fileNameField.isFocused()){
            return fileNameField.charTyped(input);
        }
        return super.charTyped(input);
    }

    @Override
    public void removed(){
        Flow.INSTANCE.moduleManager.reloadScripts();
        super.removed();
    }
}
