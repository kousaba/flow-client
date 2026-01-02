package net.flowclient.module.impl;

import net.flowclient.event.Subscribe;
import net.flowclient.event.impl.ChatReceiveEvent;
import net.flowclient.event.impl.Render2DEvent;
import net.flowclient.event.impl.TickEvent;
import net.flowclient.module.HudModule;
import net.flowclient.module.TextHudModule;
import net.flowclient.script.ScriptManager;
import net.flowclient.script.parser.FlowScriptLexer;
import net.flowclient.script.parser.FlowScriptParser;
import net.flowclient.script.runtime.FlowScriptInterpreter;
import net.flowclient.script.runtime.FlowScriptLib;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ScriptModule extends TextHudModule {
    private final FlowScriptInterpreter interpreter = new FlowScriptInterpreter();
    private boolean isLoaded = false;
    private static final String TARGET_FILE = "main.flow";

    public void reload() {
        // ScriptManagerを使ってファイルの中身(String)を取得
        String code = ScriptManager.loadScript(TARGET_FILE);

        if(code != null && !code.isEmpty()){
            loadScript(code);
            System.out.println("Script reloaded: " + TARGET_FILE);
            interpreter.callFunction("on_init");
        }
    }

    public FlowScriptInterpreter getInterpreter(){
        return interpreter;
    }

    public ScriptModule(){
        super("Script", 10, 10, "", List.of(""));
        System.out.println("ScriptModule Created: " + System.identityHashCode(this));
        reload();
    }

    public void loadScript(String scriptCode){
        scriptCode = scriptCode.replace("\r", "");
        FlowScriptLexer lexer = new FlowScriptLexer(CharStreams.fromString(scriptCode));
        FlowScriptParser parser = new FlowScriptParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new org.antlr.v4.runtime.BaseErrorListener() {
            @Override
            public void syntaxError(org.antlr.v4.runtime.Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                                    String msg, org.antlr.v4.runtime.RecognitionException e){
                System.err.println("FlowScript Error at line " + line + ":" + charPositionInLine + " -> " + msg);
            }
        });
        ParseTree tree = parser.script();
        interpreter.load(tree);
        isLoaded = true;
    }

    @Subscribe
    public void onTick(TickEvent event){
        if(!isLoaded || !isEnabled()) return;
        interpreter.setVariable("fps", MinecraftClient.getInstance().getCurrentFps());
        interpreter.callFunction("on_tick");
    }

    @Override
    public void render(DrawContext context){
        if(!isLoaded || !isEnabled()) return;
        FlowScriptLib.currentContext = context;
        try{
            interpreter.callFunction("on_render");
            Object textObj = interpreter.getVariable("text");
            Object colorObj = interpreter.getVariable("color");
            Object shadowObj = interpreter.getVariable("shadow");
            if(textObj != null){
                String text = textObj != null ? textObj.toString() : "";
                int color = colorObj instanceof Number ? ((Number) colorObj).intValue() : -1;
                boolean shadow = shadowObj instanceof Boolean ? (Boolean) shadowObj : (shadowObj instanceof Number && ((Number) shadowObj).intValue() != 0);
                context.drawText(MinecraftClient.getInstance().textRenderer, text, (int)getX(), (int)getY(), color, shadow);
            }
        } finally {
            FlowScriptLib.currentContext = null;
        }
    }

    @Subscribe
    public void onChat(ChatReceiveEvent event){
        if(!isLoaded || !isEnabled()) return;
        System.out.println("on_chat called");
        interpreter.callFunction("on_chat", List.of(event.message));
    }

    @Override
    public Map<String, String> getValue(){
        return null;
    }
}
