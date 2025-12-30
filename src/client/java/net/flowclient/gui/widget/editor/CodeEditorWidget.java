package net.flowclient.gui.widget.editor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CodeEditorWidget extends ClickableWidget {
    // 行ごとに管理するリスト
    private final List<StringBuilder> lines = new ArrayList<>();

    private int cursorRow = 0;
    private int cursorCol = 0;

    private int scrollY = 0;

    public CodeEditorWidget(int x,int y,int width,int height){
        super(x,y,width,height, Text.empty());
        lines.add(new StringBuilder());
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta){
        context.fill(getX(), getY(), getX() + width, getY() + height, 0xFF1E1E1E);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int lineHeight = textRenderer.fontHeight + 2; // 行間を空ける

        // 各行の描画
        for(int i = 0;i < lines.size();i++) {
            String lineContext = lines.get(i).toString();
            int drawY = getY() + 5 + (i * lineHeight) - scrollY;
            if (drawY + lineHeight < getY() || drawY > getY() + height) continue; // 画面外なら描画しない
            context.drawText(textRenderer, String.valueOf(i + 1), getX() + 5, drawY, 0xFF606060, false);

            // コードの描画
            context.drawText(textRenderer, lineContext, getX() + 30, drawY, 0xFFD4D4D4, true);
        }

        if(this.isFocused()){
            int cursorDrawY = getY() + 5 + (cursorRow * lineHeight) - scrollY;
            String textBeforeCursor = "";
            if(cursorRow < lines.size()){
                StringBuilder currentLine = lines.get(cursorRow);// 範囲外エラー防止
                int safeCol = Math.min(cursorCol, currentLine.length());
                textBeforeCursor = currentLine.substring(0, safeCol);
            }
            int cursorDrawX = getX() + 30 + textRenderer.getWidth(textBeforeCursor);
            if((System.currentTimeMillis() / 500) % 2 == 0){
                context.fill(cursorDrawX, cursorDrawY - 1, cursorDrawX + 1, cursorDrawY + lineHeight - 1, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public boolean charTyped(CharInput input){
        char chr = (char) input.codepoint();
        StringBuilder line = lines.get(cursorRow);
        line.insert(cursorCol, chr);
        cursorCol++;
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input){
        if(!this.isFocused()) return false;
        StringBuilder currentLine = lines.get(cursorRow);
        switch(input.getKeycode()){
            case GLFW.GLFW_KEY_ENTER -> {
                // 改行処理
                String contextAfterCursor = currentLine.substring(cursorCol);
                currentLine.delete(cursorCol, currentLine.length());

                cursorRow++;
                cursorCol = 0;
                lines.add(cursorRow, new StringBuilder(contextAfterCursor));
                return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if(cursorCol > 0){
                    // 文字削除
                    currentLine.deleteCharAt(cursorCol - 1);
                    cursorCol--;
                }else if(cursorRow > 0){
                    // 行削除
                    StringBuilder prevLine = lines.get(cursorRow - 1);
                    int prevLineLength = prevLine.length();

                    prevLine.append(currentLine);
                    lines.remove(cursorRow);

                    cursorRow--;
                    cursorCol = prevLineLength;
                }
                return true;
            }
            // カーソル移動
            case GLFW.GLFW_KEY_RIGHT -> {
                if(cursorCol < currentLine.length()){
                    cursorCol++;
                }else if(cursorRow < lines.size() - 1){
                    cursorRow++;
                    cursorCol = 0;
                }
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                if(cursorCol > 0){
                    cursorCol--;
                }else if(cursorRow > 0){
                    cursorRow--;
                    cursorCol = lines.get(cursorRow).length();
                }
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                if(cursorRow > 0){
                    cursorRow--;
                    cursorCol = Math.min(cursorCol, lines.get(cursorRow).length());
                }
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                if(cursorRow < lines.size() - 1){
                    cursorRow++;
                    cursorCol = Math.min(cursorCol, lines.get(cursorRow).length());
                }
                return true;
            }
        }
        return super.keyPressed(input);
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {}

    public String getText(){
        StringBuilder sb = new StringBuilder();
        for(StringBuilder line : lines){
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public void setText(String text){
        this.lines.clear();
        String[] splitLines = text.split("\n", -1);
        for(String s : splitLines){
            // winの改行コード対策("\r")
            this.lines.add(new StringBuilder(s.replace("\r", "")));
        }

        // カーソル位置リセット
        this.cursorRow = 0;
        this.cursorCol = 0;
    }
}
