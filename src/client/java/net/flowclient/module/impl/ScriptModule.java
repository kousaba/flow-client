package net.flowclient.module.impl;

import net.flowclient.event.Subscribe;
import net.flowclient.event.impl.Render2DEvent;
import net.flowclient.event.impl.TickEvent;
import net.flowclient.module.HudModule;
import net.flowclient.module.TextHudModule;
import net.flowclient.script.ScriptManager;
import net.flowclient.script.parser.FlowScriptLexer;
import net.flowclient.script.parser.FlowScriptParser;
import net.flowclient.script.runtime.FlowScriptInterpreter;
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
        }
    }

    public ScriptModule(){
        super("Script", 10, 10, "", List.of(""));
        reload();
    }

    public void loadScript(String scriptCode){
        FlowScriptLexer lexer = new FlowScriptLexer(CharStreams.fromString(scriptCode));
        FlowScriptParser parser = new FlowScriptParser(new CommonTokenStream(lexer));
        ParseTree tree = parser.script();
        interpreter.load(tree);
        interpreter.callFunction("on_init");
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
        System.out.println("ScriptModule Debug: Loaded=" + isLoaded + ", Enabled=" + isEnabled());
        if(!isLoaded || !isEnabled()) return;
        Object textObj = interpreter.getVariable("text");
        Object colorObj = interpreter.getVariable("color");
        String text = textObj != null ? textObj.toString() : "";
        System.out.println("get text: " + textObj + " color: " + colorObj);
        int color = colorObj instanceof Number ? ((Number) colorObj).intValue() : -1;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawText(textRenderer, text, (int)getX(), (int)getY(), color, true);
    }

    @Override
    public Map<String, String> getValue(){
        return null;
    }
}
