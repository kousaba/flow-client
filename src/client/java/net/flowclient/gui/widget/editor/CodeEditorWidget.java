package net.flowclient.gui.widget.editor;

import net.flowclient.script.parser.FlowScriptLexer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CodeEditorWidget extends ClickableWidget {
    private List<StringBuilder> lines = new ArrayList<>();

    // カーソル位置
    private int cursorRow = 0;
    private int cursorCol = 0;

    // 選択範囲の開始位置 (Anchor)
    // Shiftを押しながら移動すると、AnchorからCursorまでが選択範囲になる
    private int anchorRow = -1;
    private int anchorCol = -1;

    // 表示オフセット
    private int scrollY = 0;
    private int scrollX = 0;

    private static final int COLOR_DEFAULT = 0xFFD4D4D4;
    private static final int COLOR_KEYWORD = 0xFF569CD6;
    private static final int COLOR_CONTROL = 0xFFC586C0;
    private static final int COLOR_STRING = 0xFFCE9178;
    private static final int COLOR_NUMBER = 0xFFB5CEA8;
    private static final int COLOR_COMMENT = 0xFF6A9955;
    private static final int COLOR_LITERAL = 0xFF4EC9B0;
    private static final int COLOR_COLOR = 0xFFFF00FF; // カラーリテラル
    private static final int COLOR_FUNCTION = 0xFFDCDCAA; // 薄い黄色 (関数)
    private static final int COLOR_VARIABLE = 0xFF9CDCFE; // 水色 (変数)
    private static final int COLOR_OPERATOR = 0xFFD4D4D4; // 白/グレー (記号)

    // 履歴 (Undo/Redo用)
    private final Stack<EditorState> undoStack = new Stack<>();
    private final Stack<EditorState> redoStack = new Stack<>();
    private static final int MAX_HISTORY = 50;

    public CodeEditorWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Text.empty());
        lines.add(new StringBuilder());
        saveHistory(); // 初期状態を保存
    }

    // --- 描画処理 ---
    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getX() + width, getY() + height, 0xFF1E1E1E);
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int lineHeight = tr.fontHeight + 2;

        for (int i = 0; i < lines.size(); i++) {
            int drawY = getY() + 5 + (i * lineHeight) - scrollY;
            if (drawY + lineHeight < getY() || drawY > getY() + height) continue;

            String lineStr = lines.get(i).toString();

            // 1. 選択範囲の描画 (背景を青くする)
            if (hasSelection()) {
                drawSelectionHighlight(context, tr, i, lineStr, drawY, lineHeight);
            }

            // 2. 行番号
            context.drawText(tr, String.valueOf(i + 1), getX() + 5, drawY, 0xFF606060, false);

            // 3. テキスト
            drawSyntaxHighlightedText(context, tr, lineStr, getX() + 35 - scrollX, drawY);
        }

        // 4. カーソル描画
        if (this.isFocused()) {
            drawCursor(context, tr, lineHeight);
        }
    }

    private void drawCursor(DrawContext context, TextRenderer tr, int lineHeight) {
        int cursorDrawY = getY() + 5 + (cursorRow * lineHeight) - scrollY;
        if (cursorDrawY < getY() || cursorDrawY > getY() + height) return;

        String currentLine = lines.get(cursorRow).toString();
        int safeCol = Math.min(cursorCol, currentLine.length());
        String textBefore = currentLine.substring(0, safeCol);
        int cursorDrawX = getX() + 35 + tr.getWidth(textBefore) - scrollX;

        if ((System.currentTimeMillis() / 500) % 2 == 0) {
            context.fill(cursorDrawX, cursorDrawY - 1, cursorDrawX + 1, cursorDrawY + lineHeight - 1, 0xFFFFFFFF);
        }
    }

    private void drawSelectionHighlight(DrawContext context, TextRenderer tr, int row, String lineStr, int drawY, int lineHeight) {
        // 選択範囲の正規化 (Start < End にする)
        Position start = new Position(Math.min(anchorRow, cursorRow), (anchorRow < cursorRow || (anchorRow == cursorRow && anchorCol < cursorCol)) ? anchorCol : cursorCol);
        Position end = new Position(Math.max(anchorRow, cursorRow), (anchorRow > cursorRow || (anchorRow == cursorRow && anchorCol > cursorCol)) ? anchorCol : cursorCol);

        // 正規化ロジックの修正（行が違う場合の大小関係）
        if (anchorRow > cursorRow) { start = new Position(cursorRow, cursorCol); end = new Position(anchorRow, anchorCol); }
        else if (anchorRow < cursorRow) { start = new Position(anchorRow, anchorCol); end = new Position(cursorRow, cursorCol); }
        else {
            start = new Position(cursorRow, Math.min(cursorCol, anchorCol));
            end = new Position(cursorRow, Math.max(cursorCol, anchorCol));
        }

        // この行が選択範囲に含まれているか
        if (row >= start.row && row <= end.row) {
            int selStartCol = (row == start.row) ? start.col : 0;
            int selEndCol = (row == end.row) ? end.col : lineStr.length();

            // 選択部分の幅計算
            int x1 = getX() + 35 - scrollX + tr.getWidth(lineStr.substring(0, selStartCol));
            int x2 = getX() + 35 - scrollX + tr.getWidth(lineStr.substring(0, selEndCol));

            // 全選択などで空行が含まれる場合の幅確保
            if (x1 == x2) x2 += 5;

            context.fill(x1, drawY, x2, drawY + lineHeight, 0xFF264F78); // VSCode風の青
        }
    }

    // --- 入力処理 ---

    @Override
    public boolean charTyped(CharInput input) {
        saveHistory(); // 入力前に履歴保存
        deleteSelection(); // 選択範囲があれば消す
        insertText(Character.toString((char) input.codepoint()));
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput key) {
        int keyCode = key.getKeycode(), modifiers = key.modifiers();
        if (!this.isFocused()) return false;

        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

        // --- コピペ・Undo/Redo ---
        if (ctrl) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_Z -> {
                    if (shift) redo(); else undo();
                    return true;
                }
                case GLFW.GLFW_KEY_Y -> { redo(); return true; }
                case GLFW.GLFW_KEY_A -> { selectAll(); return true; }
                case GLFW.GLFW_KEY_C -> { copy(); return true; }
                case GLFW.GLFW_KEY_V -> {
                    saveHistory();
                    deleteSelection();
                    paste();
                    return true;
                }
                case GLFW.GLFW_KEY_X -> {
                    saveHistory();
                    cut();
                    return true;
                }
            }
        }

        // --- 通常のキー操作 ---
        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER -> {
                saveHistory();
                deleteSelection();
                insertNewLineWithIndent();
                return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                saveHistory();
                if (hasSelection()) {
                    deleteSelection();
                } else {
                    deleteBack();
                }
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                saveHistory();
                if (hasSelection()) deleteSelection();
                else deleteForward();
                return true;
            }
            // --- カーソル移動 ---
            case GLFW.GLFW_KEY_RIGHT -> {moveCursor(0, 1, shift); return true;}
            case GLFW.GLFW_KEY_LEFT -> {moveCursor(0, -1, shift); return true;}
            case GLFW.GLFW_KEY_UP -> {moveCursor(-1, 0, shift); return true;}
            case GLFW.GLFW_KEY_DOWN -> {moveCursor(1, 0, shift); return true;}
        }
        return super.keyPressed(key);
    }

    // --- ロジック実装 ---

    private void insertText(String text) {
        text = text.replace("\r", "");
        String[] parts = text.split("\n", -1);
        StringBuilder line = lines.get(cursorRow);

        // 最初の行に残りを結合
        String afterCursor = line.substring(cursorCol);
        line.delete(cursorCol, line.length());
        line.append(parts[0]);
        cursorCol += parts[0].length();

        // 複数行の場合
        for (int i = 1; i < parts.length; i++) {
            lines.add(cursorRow + 1, new StringBuilder(parts[i]));
            cursorRow++;
            cursorCol = parts[i].length();
        }

        // 最後の行に残っていた文字を結合
        lines.get(cursorRow).append(afterCursor);
    }

    private void insertNewLineWithIndent() {
        String currentLineContent = lines.get(cursorRow).toString();

        // インデント計算
        String indent = "";
        int spaces = 0;
        while (spaces < currentLineContent.length() && currentLineContent.charAt(spaces) == ' ') {
            spaces++;
        }
        indent = currentLineContent.substring(0, spaces);

        // { で終わっていたらインデントを増やす
        String trimmed = currentLineContent.trim();
        if (trimmed.endsWith("{")) {
            indent += "    "; // 4スペース
        }

        // 改行処理
        String afterCursor = currentLineContent.substring(cursorCol);
        lines.get(cursorRow).delete(cursorCol, currentLineContent.length());

        cursorRow++;
        cursorCol = indent.length();
        lines.add(cursorRow, new StringBuilder(indent + afterCursor));

        // } で閉じる場合、その行のインデントを戻す処理（高度な機能）は今回は割愛
    }

    private void deleteBack() {
        if (cursorCol > 0) {
            lines.get(cursorRow).deleteCharAt(cursorCol - 1);
            cursorCol--;
        } else if (cursorRow > 0) {
            StringBuilder current = lines.get(cursorRow);
            StringBuilder prev = lines.get(cursorRow - 1);
            cursorCol = prev.length();
            prev.append(current);
            lines.remove(cursorRow);
            cursorRow--;
        }
    }

    private void deleteForward() {
        if (cursorCol < lines.get(cursorRow).length()) {
            lines.get(cursorRow).deleteCharAt(cursorCol);
        } else if (cursorRow < lines.size() - 1) {
            StringBuilder next = lines.get(cursorRow + 1);
            lines.get(cursorRow).append(next);
            lines.remove(cursorRow + 1);
        }
    }

    // --- 選択範囲・クリップボード ---

    private boolean hasSelection() {
        return anchorRow != -1 && (anchorRow != cursorRow || anchorCol != cursorCol);
    }

    private void startSelection() {
        if (anchorRow == -1) {
            anchorRow = cursorRow;
            anchorCol = cursorCol;
        }
    }

    private void clearSelection() {
        anchorRow = -1;
        anchorCol = -1;
    }

    private String getSelectedText() {
        if (!hasSelection()) return "";
        Position start = getSelectionStart();
        Position end = getSelectionEnd();

        StringBuilder sb = new StringBuilder();
        if (start.row == end.row) {
            return lines.get(start.row).substring(start.col, end.col);
        }

        sb.append(lines.get(start.row).substring(start.col));
        for (int i = start.row + 1; i < end.row; i++) {
            sb.append("\n").append(lines.get(i));
        }
        sb.append("\n").append(lines.get(end.row).substring(0, end.col));
        return sb.toString();
    }

    private void deleteSelection() {
        if (!hasSelection()) return;
        Position start = getSelectionStart();
        Position end = getSelectionEnd();

        if (start.row == end.row) {
            lines.get(start.row).delete(start.col, end.col);
        } else {
            lines.get(start.row).delete(start.col, lines.get(start.row).length());
            lines.get(start.row).append(lines.get(end.row).substring(end.col));
            for (int i = 0; i < end.row - start.row; i++) {
                lines.remove(start.row + 1);
            }
        }
        cursorRow = start.row;
        cursorCol = start.col;
        clearSelection();
    }

    private void selectAll() {
        anchorRow = 0;
        anchorCol = 0;
        cursorRow = lines.size() - 1;
        cursorCol = lines.get(cursorRow).length();
    }

    private void copy() {
        if (hasSelection()) {
            MinecraftClient.getInstance().keyboard.setClipboard(getSelectedText());
        }
    }

    private void paste() {
        String clipboard = MinecraftClient.getInstance().keyboard.getClipboard();
        if (clipboard != null && !clipboard.isEmpty()) {
            insertText(clipboard);
        }
    }

    private void cut() {
        copy();
        deleteSelection();
    }

    // --- Undo / Redo ---

    private void saveHistory() {
        // 現在の状態をコピーしてスタックに積む
        List<String> currentLines = new ArrayList<>();
        for (StringBuilder sb : lines) currentLines.add(sb.toString());

        undoStack.push(new EditorState(currentLines, cursorRow, cursorCol));
        if (undoStack.size() > MAX_HISTORY) undoStack.remove(0);
        redoStack.clear();
    }

    private void undo() {
        if (undoStack.isEmpty()) return;
        redoStack.push(new EditorState(snapshotLines(), cursorRow, cursorCol));
        restoreState(undoStack.pop());
    }

    private void redo() {
        if (redoStack.isEmpty()) return;
        undoStack.push(new EditorState(snapshotLines(), cursorRow, cursorCol));
        restoreState(redoStack.pop());
    }

    private List<String> snapshotLines() {
        List<String> list = new ArrayList<>();
        for (StringBuilder sb : lines) list.add(sb.toString());
        return list;
    }

    private void restoreState(EditorState state) {
        this.lines.clear();
        for (String s : state.lines) this.lines.add(new StringBuilder(s));
        this.cursorRow = state.row;
        this.cursorCol = state.col;
        clearSelection();
    }

    // --- カーソル移動ヘルパー ---

    private void moveCursor(int dRow, int dCol, boolean select) {
        if (select) startSelection();
        else clearSelection();

        if (dRow != 0) {
            cursorRow += dRow;
        } else if (dCol != 0) {
            cursorCol += dCol;
        }

        // 範囲制限
        cursorRow = Math.max(0, Math.min(cursorRow, lines.size() - 1));
        int len = lines.get(cursorRow).length();

        // 左右移動で行またぎ
        if (dCol > 0 && cursorCol > len && cursorRow < lines.size() - 1) {
            cursorRow++; cursorCol = 0;
        } else if (dCol < 0 && cursorCol < 0 && cursorRow > 0) {
            cursorRow--; cursorCol = lines.get(cursorRow).length();
        } else {
            cursorCol = Math.max(0, Math.min(cursorCol, lines.get(cursorRow).length()));
        }
    }

    // --- 補助クラス ---

    private Position getSelectionStart() {
        if (anchorRow < cursorRow) return new Position(anchorRow, anchorCol);
        if (anchorRow > cursorRow) return new Position(cursorRow, cursorCol);
        return new Position(cursorRow, Math.min(anchorCol, cursorCol));
    }

    private Position getSelectionEnd() {
        if (anchorRow > cursorRow) return new Position(anchorRow, anchorCol);
        if (anchorRow < cursorRow) return new Position(cursorRow, cursorCol);
        return new Position(cursorRow, Math.max(anchorCol, cursorCol));
    }

    private record Position(int row, int col) {}
    private record EditorState(List<String> lines, int row, int col) {}

    // テキスト取得・設定（保存用）
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (StringBuilder line : lines) sb.append(line).append("\n");
        return sb.toString();
    }

    public void setText(String text) {
        this.lines.clear();
        for (String s : text.replace("\r", "").split("\n", -1)) lines.add(new StringBuilder(s.replace("\r", "")));
        cursorRow = 0; cursorCol = 0;
        undoStack.clear(); redoStack.clear();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

    // AIが修正しました。
    private void drawSyntaxHighlightedText(DrawContext context, TextRenderer tr, String text, int x, int y) {
        if (text.isEmpty()) return;

        try {
            FlowScriptLexer lexer = new FlowScriptLexer(CharStreams.fromString(text));
            lexer.removeErrorListeners();

            // 1. まず全てのトークンをリストに取得する
            List<Token> tokens = new ArrayList<>();
            while (true) {
                Token token = lexer.nextToken();
                if (token.getType() == Token.EOF) break;
                tokens.add(token);
            }

            int currentX = x;
            int lastIndex = 0;

            // 2. トークンごとにループ
            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);
                int startIndex = token.getStartIndex();

                // --- 隙間（スペース）の描画 ---
                if (startIndex > lastIndex) {
                    if (startIndex <= text.length()) {
                        String skippedText = text.substring(lastIndex, startIndex);
                        context.drawText(tr, skippedText, currentX, y, COLOR_DEFAULT, true);
                        currentX += tr.getWidth(skippedText);
                    }
                }

                // --- 色の決定ロジック ---
                int color = getColorFromToken(token.getType());
                String tokenText = token.getText();

                // ★ IDの場合の特殊処理 (関数 vs 変数) ★
                if (token.getType() == FlowScriptLexer.ID) {
                    boolean isFunction = false;

                    // 次のトークンがあるかチェック
                    if (i + 1 < tokens.size()) {
                        Token nextToken = tokens.get(i + 1);
                        // 次の文字が '(' なら関数呼び出しとみなす
                        if (nextToken.getText().equals("(")) {
                            isFunction = true;
                        }
                    }

                    // fn の直後の ID も関数定義なので黄色にする
                    if (i > 0) {
                        Token prevToken = tokens.get(i - 1);
                        if (prevToken.getType() == FlowScriptLexer.FN) {
                            isFunction = true;
                        }
                    }

                    color = isFunction ? COLOR_FUNCTION : COLOR_VARIABLE;
                }

                // カラーリテラルの処理
                if (token.getType() == FlowScriptLexer.COLOR_LITERAL) {
                    try {
                        String hex = tokenText.substring(1);
                        long val = Long.parseLong(hex, 16);
                        if (hex.length() == 6) val |= 0xFF000000L;
                        color = (int) val;
                    } catch (Exception ignored) {}
                }

                // 描画
                context.drawText(tr, tokenText, currentX, y, color, true);
                currentX += tr.getWidth(tokenText);

                lastIndex = token.getStopIndex() + 1;
            }

            if (lastIndex < text.length()) {
                String remainingText = text.substring(lastIndex);
                context.drawText(tr, remainingText, currentX, y, COLOR_DEFAULT, true);
            }

        } catch (Exception e) {
            context.drawText(tr, text, x, y, COLOR_DEFAULT, true);
        }
    }

    private int getColorFromToken(int tokenType){
        return switch (tokenType) {
            case FlowScriptLexer.FN, FlowScriptLexer.LET -> COLOR_KEYWORD;
            case FlowScriptLexer.IF, FlowScriptLexer.ELSE, FlowScriptLexer.WHILE, FlowScriptLexer.FOR, FlowScriptLexer.RETURN ->
                    COLOR_CONTROL;
            case FlowScriptLexer.STRING -> COLOR_STRING;
            case FlowScriptLexer.NUMBER -> COLOR_NUMBER;
            case FlowScriptLexer.TRUE, FlowScriptLexer.FALSE, FlowScriptLexer.NULL -> COLOR_LITERAL;
            case FlowScriptLexer.COLOR_LITERAL -> COLOR_COLOR;
            case FlowScriptLexer.COMMENT, FlowScriptLexer.BLOCK_COMMENT -> COLOR_COMMENT;
            case FlowScriptLexer.ID -> COLOR_VARIABLE;
            default -> COLOR_OPERATOR;
        };
    }
}