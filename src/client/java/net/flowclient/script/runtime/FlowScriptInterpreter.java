package net.flowclient.script.runtime;

import net.flowclient.script.parser.FlowScriptBaseVisitor;
import net.flowclient.script.parser.FlowScriptParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.core.script.Script;

import java.util.HashMap;
import java.util.Map;

public class FlowScriptInterpreter extends FlowScriptBaseVisitor<Object> {
    // 変数スコープ
    private final Map<String, Object> globalVariables = new HashMap<>();
    private final Map<String, FlowScriptParser.FunctionDeclContext> functions = new HashMap<>();

    // スクリプト読み込み
    public void load(ParseTree tree){
        this.visit(tree);
    }

    // 特定の関数を実行する (on_tickなど)
    public void callFunction(String name) {
        if (functions.containsKey(name)) {
            // その関数のブロックを実行
            this.visit(functions.get(name).block());
        }
    }

    // 変数取得
    public Object getVariable(String name){
        return globalVariables.get(name);
    }

    // 変数をセットする(組み込み値よう)
    public void setVariable(String name, Object value){
        globalVariables.put(name, value);
    }

    // 関数定義: 実行せずに保存
    @Override
    public Object visitFunctionDecl(FlowScriptParser.FunctionDeclContext ctx){
        String funcName = ctx.ID().getText();
        functions.put(funcName, ctx);
        return null;
    }

    // 代入
    @Override
    public Object visitAssignment(FlowScriptParser.AssignmentContext ctx){
        String varName = ctx.ID().getText();
        Object value = this.visit(ctx.expression());

        String op = ctx.op.getText();

        if(!op.equals("=")){
            Object current = globalVariables.getOrDefault(varName, 0.0);
            double currentVal = ScriptUtils.asDouble(current);
            double addVal = ScriptUtils.asDouble(value);
            if(op.equals("+=")) value = currentVal - addVal;
            else if(op.equals("-=")) value = currentVal - addVal;
            else if(op.equals("*=")) value = currentVal * addVal;
            else if(op.equals("/=")) value = currentVal / addVal;
            else if(op.equals("%=")) value = currentVal % addVal;
        }
        globalVariables.put(varName, value);
        return value;
    }

    @Override
    public Object visitVarDecl(FlowScriptParser.VarDeclContext ctx){
        String name = ctx.ID().getText();
        Object value = null;
        if(ctx.expression() != null){
            value = this.visit(ctx.expression());
        }
        globalVariables.put(name, value);
        return value;
    }

    @Override
    public Object visitIfStmt(FlowScriptParser.IfStmtContext ctx){
        Object cond = this.visit(ctx.expression());
        if(ScriptUtils.asBoolean(cond)) return this.visit(ctx.statement(0));
        else if(ctx.statement().size() > 1) return this.visit(ctx.statement(1));
        return null;
    }

    @Override
    public Object visitAdditiveExpr(FlowScriptParser.AdditiveExprContext ctx){
        Object left = this.visit(ctx.expression(0));
        Object right = this.visit(ctx.expression(1));

        if(ctx.op.getText().equals("+")){
            if(left instanceof String || right instanceof String){
                return ScriptUtils.asString(left) + ScriptUtils.asString(right);
            }
            return ScriptUtils.asDouble(left) + ScriptUtils.asDouble(right);
        }
        return ScriptUtils.asDouble(left) - ScriptUtils.asDouble(right);
    }

    @Override
    public Object visitAtomExpr(FlowScriptParser.AtomExprContext ctx){
        if(ctx.atom().NUMBER() != null) return Double.parseDouble(ctx.atom().NUMBER().getText());
        if(ctx.atom().STRING() != null) {String s = ctx.atom().STRING().getText(); return s.substring(1,s.length() - 1);}
        if(ctx.atom().TRUE() != null) return true;
        if(ctx.atom().FALSE() != null) return false;
        if(ctx.atom().NULL() != null) return null;
        if(ctx.atom().ID() != null) return globalVariables.get(ctx.atom().ID().getText());
        if(ctx.atom().COLOR_LITERAL() != null){
            String hex = ctx.atom().COLOR_LITERAL().getText().substring(1);
            long val = Long.parseLong(hex, 16);
            if(hex.length() == 6) val |= 0xFF000000L;
            return (int) val;
        }
        return null;
    }
}
