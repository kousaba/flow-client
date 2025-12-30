package net.flowclient.script;

import net.flowclient.script.parser.FlowScriptLexer;
import net.flowclient.script.parser.FlowScriptParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class ScriptText {
    public static void testParser(String code){
        System.out.println("Testing code: " + code);
        var input = CharStreams.fromString(code);
        var lexer = new FlowScriptLexer(input);
        var tokens = new CommonTokenStream(lexer);
        var parser = new FlowScriptParser(tokens);
        ParseTree tree = parser.script();
        System.out.println("Parse Tree: " + tree.toStringTree());
    }
}
