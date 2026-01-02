package net.flowclient.script.runtime;

import com.mojang.datafixers.types.Type;
import net.flowclient.script.ScriptManager;
import net.flowclient.script.parser.FlowScriptBaseVisitor;
import net.flowclient.script.parser.FlowScriptLexer;
import net.flowclient.script.parser.FlowScriptParser;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.core.script.Script;

import java.util.*;
import java.util.function.Supplier;

public class FlowScriptInterpreter extends FlowScriptBaseVisitor<Object> {
    // 変数管理
    // グローバル変数(text, color, countなど)
    private final Map<String, Object> globalVariables = new HashMap<>();
    // ローカル変数(関数呼び出しごとの一時変数)
    private final Deque<Map<String, Object>> scopeStack = new ArrayDeque<>();
    // 関数定義
    private final Map<String, FlowScriptParser.FunctionDeclContext> functions = new HashMap<>();
    // ネイティブ関数(Java側で定義する関数(get_fps(), min()など))
    private final Map<String, NativeFunction> nativeFunctions = new HashMap<>();
    // return, break, continueするための例外
    private static class ReturnException extends RuntimeException{
        private final Object value;
        public ReturnException(Object value) {this.value = value;}
        public Object getValue() {return value;}
    }
    private static class BreakException extends RuntimeException {}
    private static class ContinueException extends RuntimeException {}
    // ネイティブ関数用インターフェース
    @FunctionalInterface
    public interface NativeFunction{
        Object call(List<Object> args);
    }
    // 無限ループ防止
    private int executionCount = 0;
    private static final int MAX_EXECUTION_LIMIT = 50000;

    public FlowScriptInterpreter(){
        scopeStack.push(globalVariables);
    }

    // エントリーポイント
    public void load(ParseTree tree){
        this.visit(tree);
    }

    // 関数を外部から実行(on_tick, on_attackなど)
    public void callFunction(String name){
        executionCount = 0;
        if(functions.containsKey(name)){
            try{
                this.visit(functions.get(name).block());
            } catch (ReturnException e){
                // 正常終了
            } catch (RuntimeException e){
                // TODO: これをFlowScriptScreenに表示
                System.err.println("Runtime Error in " + name + ": " + e.getMessage());
            }
        }
    }

    // 変数を外部からセットする(FPSなど)
    public void setVariable(String name, Object value){
        globalVariables.put(name, value);
    }

    // 変数を外部から取得する
    public Object getVariable(String name){
        return globalVariables.get(name);
    }

    // ネイティブ関数登録
    public void registerNativeFunction(String name, NativeFunction function){
        nativeFunctions.put(name, function);
    }
    public void registerNativeFunction(String name, NativeFunction function, Supplier<Double> intervalSupplier){
        nativeFunctions.put(name, new CachedNativeFunction(function, intervalSupplier));
    }

    private static class CachedNativeFunction implements NativeFunction{
        private final NativeFunction original;
        private final Supplier<Double> intervalSupplier;
        private Object cachedValue = null;
        private long lastUpdateTime = 0;
        public CachedNativeFunction(NativeFunction original, Supplier<Double> intervalSupplier){
            this.original = original;
            this.intervalSupplier = intervalSupplier;
        }

        @Override
        public Object call(List<Object> args){
            long now = System.currentTimeMillis();
            double intervalSeconds = intervalSupplier.get();
            long intervalMillis = (long) (intervalSeconds * 1000);

            if (cachedValue == null || intervalMillis <= 0 || (now - lastUpdateTime) > intervalMillis) {
                cachedValue = original.call(args);
                lastUpdateTime = now;
            }

            return cachedValue;
        }
    }

    // 無限ループチェック
    private void checkSafety(){
        executionCount++;
        if(executionCount > MAX_EXECUTION_LIMIT){
            throw new RuntimeException("Execution limit exceeded (Infinite loop detected?)");
        }
    }

    // 変数アクセスヘルパー
    private void setVar(String name, Object value){
        if(scopeStack.peek() == null) throw new RuntimeException("Tried to set a variable when scopeStack was null.");
        scopeStack.peek().put(name, value);
    }
    private Object getVar(String name){
        for(Map<String, Object> scope : scopeStack){
            if(scope.containsKey(name)) return scope.get(name);
        }
        return null;
    }

    // 関数定義
    @Override
    public Object visitFunctionDecl(FlowScriptParser.FunctionDeclContext ctx){
        String funcName = ctx.ID().getText();
        functions.put(funcName, ctx);
        return null;
    }

    // 変数宣言
    @Override
    public Object visitVarDecl(FlowScriptParser.VarDeclContext ctx){
        String name = ctx.ID().getText();
        Object value = null;
        if(ctx.expression() != null) value = this.visit(ctx.expression());
        setVar(name, value);
        return value;
    }

    // 代入
    @Override
    public Object visitAssignment(FlowScriptParser.AssignmentContext ctx){
        checkSafety();
        String varName = ctx.ID().getText();
        Object right = this.visit(ctx.expression());
        String op = ctx.op.getText();
        if(op.equals("=")){
            boolean found = false;
            for(Map<String, Object> scope : scopeStack){
                if(scope.containsKey(varName)){
                    scope.put(varName, right);
                    found = true;
                    break;
                }
            }
            if(!found){
                scopeStack.peekLast().put(varName, right);
            }
            return right;
        }
        Object current = getVar(varName);
        if(current == null) current = 0.0;
        double curVal = ScriptUtils.asDouble(current);
        double rVal = ScriptUtils.asDouble(right);
        double result = curVal;
        switch(op){
            case "+=" -> result = curVal + rVal;
            case "-=" -> result = curVal - rVal;
            case "*=" -> result = curVal * rVal;
            case "/=" -> result = curVal / rVal;
            case "%=" -> result = curVal % rVal;
        }

        for(Map<String, Object> scope : scopeStack){
            if(scope.containsKey(varName)){
                scope.put(varName, result);
                return result;
            }
        }
        scopeStack.peekLast().put(varName, result);
        return result;
    }

    // 関数呼び出し(helper(5)やget_fps())
    @Override
    public Object visitFunctionCallExpr(FlowScriptParser.FunctionCallExprContext ctx){
        checkSafety();
        String name = ctx.ID().getText();
        List<Object> args = new ArrayList<>();
        if(ctx.exprList() != null){
            for(var expr : ctx.exprList().expression()){
                args.add(this.visit(expr));
            }
        }
        // ネイティブ関数か
        if(nativeFunctions.containsKey(name)){
            return nativeFunctions.get(name).call(args);
        }
        // スクリプト定義関数か
        if(functions.containsKey(name)){
            FlowScriptParser.FunctionDeclContext funcCtx = functions.get(name);
            Map<String, Object> localScope = new HashMap<>();
            if(funcCtx.paramList() != null){
                List<org.antlr.v4.runtime.tree.TerminalNode> paramNames = funcCtx.paramList().ID();
                for(int i = 0;i < paramNames.size();i++){
                    String paramName = paramNames.get(i).getText();
                    Object argValue = (i < args.size() ? args.get(i) : null);
                    localScope.put(paramName, argValue);
                }
            }
            scopeStack.push(localScope);
            Object returnValue = null;
            try{
                this.visit(funcCtx.block());
            }catch(ReturnException e){
                returnValue = e.getValue();
            }finally{
                scopeStack.pop();
            }
            return returnValue;
        }
        return null;
    }

    // return
    @Override
    public Object visitReturnStmt(FlowScriptParser.ReturnStmtContext ctx){
        Object val = null;
        if(ctx.expression() != null) val = this.visit(ctx.expression());
        throw new ReturnException(val);
    }

    // if
    @Override
    public Object visitIfStmt(FlowScriptParser.IfStmtContext ctx){
        checkSafety();
        Object cond = this.visit(ctx.expression());
        if(ScriptUtils.asBoolean(cond)){
            // then
            return this.visit(ctx.statement(0));
        }else if(ctx.statement().size() > 1){
            // else
            return this.visit(ctx.statement(1));
        }
        return null;
    }

    // while
    @Override
    public Object visitWhileStmt(FlowScriptParser.WhileStmtContext ctx){
        while(ScriptUtils.asBoolean(this.visit(ctx.expression()))){
            checkSafety();
            try{
                this.visit(ctx.statement());
            } catch(BreakException e){
                break;
            } catch(ContinueException e){
                continue;
            }
        }
        return null;
    }

    // for
    @Override
    public Object visitForStmt(FlowScriptParser.ForStmtContext ctx){
        scopeStack.push(new HashMap<>());
        try{
            if(ctx.forInit() != null) this.visit(ctx.forInit());
            while(true){
                checkSafety();
                if(ctx.expression() != null){
                    if(!ScriptUtils.asBoolean(this.visit(ctx.expression()))) break;
                }
                try{
                    this.visit(ctx.statement());
                } catch(BreakException e){
                    break;
                } catch(ContinueException e){
                    // Exceptionを投げたら停止するため、何もしない
                }
                if(ctx.forUpdate() != null) this.visit(ctx.forUpdate());
            }
        } finally{
            scopeStack.pop();
        }
        return null;
    }
    @Override
    public Object visitForInit(FlowScriptParser.ForInitContext ctx){
        String name = ctx.ID().getText();
        Object value = this.visit(ctx.expression());
        if(ctx.LET() != null){
            scopeStack.peek().put(name, value);
        } else{
            updateVariable(name, value);
        }
        return value;
    }
    @Override
    public Object visitForUpdate(FlowScriptParser.ForUpdateContext ctx){
        String name = ctx.ID().getText();
        Object right = this.visit(ctx.expression());
        String op = ctx.op.getText();
        if (op.equals("=")){
            updateVariable(name, right);
            return right;
        }
        Object current = getVar(name);
        if(current == null) current = 0.0;
        double curVal = ScriptUtils.asDouble(current);
        double rVal = ScriptUtils.asDouble(right);
        double result = curVal;

        switch (op) {
            case "+=" -> result = curVal + rVal;
            case "-=" -> result = curVal - rVal;
            case "*=" -> result = curVal * rVal;
            case "/=" -> result = curVal / rVal;
        }

        updateVariable(name, result);
        return result;
    }

    // 変数を探して更新する（見つからなければグローバルに入れる）
    private void updateVariable(String name, Object value) {
        // ローカルスコープから順に探す
        for (Map<String, Object> scope : scopeStack) {
            if (scope.containsKey(name)) {
                scope.put(name, value);
                return;
            }
        }
        // どこにもなければグローバル（一番下）に追加
        scopeStack.peekLast().put(name, value);
    }

    // 演算子
    @Override
    public Object visitParenExpr(FlowScriptParser.ParenExprContext ctx){
        return this.visit(ctx.expression());
    }
    @Override
    public Object visitPowerExpr(FlowScriptParser.PowerExprContext ctx){
        Object left = this.visit(ctx.expression(0));
        Object right = this.visit(ctx.expression(1));
        return Math.pow(ScriptUtils.asDouble(left), ScriptUtils.asDouble(right));
    }

    @Override
    public Object visitListExpr(FlowScriptParser.ListExprContext ctx){
        List<Object> list = new ArrayList<>();
        if(ctx.exprList() != null){
            for(var expr : ctx.exprList().expression()){
                list.add(this.visit(expr));
            }
        }
        return list;
    }

    @Override
    public Object visitIndexExpr(FlowScriptParser.IndexExprContext ctx){
        Object target = this.visit(ctx.expression(0));
        Object indexObj = this.visit(ctx.expression(1));
        if(target instanceof List<?> list && indexObj instanceof Number n){
            int index = n.intValue();
            if(index >= 0 && index < list.size()){
                return list.get(index);
            }
        }
        return null;
    }
    @Override
    public Object visitAdditiveExpr(FlowScriptParser.AdditiveExprContext ctx){
        Object left = this.visit(ctx.expression(0));
        Object right = this.visit(ctx.expression(1));
        String op = ctx.op.getText();
        if(op.equals("+")){
            if(left instanceof String || right instanceof String){
                return ScriptUtils.asString(left) + ScriptUtils.asString(right);
            }
            return ScriptUtils.asDouble(left) + ScriptUtils.asDouble(right);
        }
        return ScriptUtils.asDouble(left) - ScriptUtils.asDouble(right);
    }

    @Override
    public Object visitMultiplicativeExpr(FlowScriptParser.MultiplicativeExprContext ctx){
        double left = ScriptUtils.asDouble(this.visit(ctx.expression(0)));
        double right = ScriptUtils.asDouble(this.visit(ctx.expression(1)));
        String op = ctx.op.getText();
        if(op.equals("*")) return left * right;
        if(op.equals("/")) return left / right;
        if(op.equals("%")) return left % right;
        throw new RuntimeException("An unknown operator was found in MultiplicativeExpr.");
    }

    @Override
    public Object visitRelationalExpr(FlowScriptParser.RelationalExprContext ctx) {
        Object left = this.visit(ctx.expression(0));
        Object right = this.visit(ctx.expression(1));
        String op = ctx.op.getText();

        if(op.equals("==")) {
            if(left == null && right == null) return true;
            if(left == null || right == null) return false;
            return left.equals(right);
        }
        if(op.equals("!=")) {
            if(left == null && right == null) return false;
            if(left == null || right == null) return true;
            return !left.equals(right);
        }
        double lVal = ScriptUtils.asDouble(left);
        double rVal = ScriptUtils.asDouble(right);
        switch (op) {
            case "<": return lVal < rVal;
            case ">": return lVal > rVal;
            case "<=": return lVal <= rVal;
            case ">=": return lVal >= rVal;
        }
        return false;
    }

    @Override
    public Object visitLogicAndExpr(FlowScriptParser.LogicAndExprContext ctx){
        Object left = this.visit(ctx.expression(0));
        if(!ScriptUtils.asBoolean(left)) return false; // 片方がfalseならすぐにfalseにする
        return ScriptUtils.asBoolean(this.visit(ctx.expression(1)));
    }

    @Override
    public Object visitLogicOrExpr(FlowScriptParser.LogicOrExprContext ctx){
        Object left = this.visit(ctx.expression(0));
        if(ScriptUtils.asBoolean(left)) return true; // Andと同じで、片方がtrueならすぐtrue
        return ScriptUtils.asBoolean(this.visit(ctx.expression(1)));
    }

    @Override
    public Object visitUnaryExpr(FlowScriptParser.UnaryExprContext ctx){
        Object val = this.visit(ctx.expression());
        if(ctx.op.getText().equals("-")) return -ScriptUtils.asDouble(val);
        if(ctx.op.getText().equals("!")) return !ScriptUtils.asBoolean(val);
        return val;
    }

    @Override
    public Object visitAtomExpr(FlowScriptParser.AtomExprContext ctx) {
        if (ctx.atom().NUMBER() != null) return Double.parseDouble(ctx.atom().NUMBER().getText());
        if (ctx.atom().STRING() != null) {
            String s = ctx.atom().STRING().getText();
            return s.substring(1, s.length() - 1);
        }
        if (ctx.atom().TRUE() != null) return true;
        if (ctx.atom().FALSE() != null) return false;
        if (ctx.atom().NULL() != null) return null;
        if (ctx.atom().ID() != null) return getVar(ctx.atom().ID().getText());
        if (ctx.atom().COLOR_LITERAL() != null) {
            String hex = ctx.atom().COLOR_LITERAL().getText().substring(1);
            long val = Long.parseLong(hex, 16);
            if (hex.length() == 6) val |= 0xFF000000L;
            return (int) val;
        }
        return null;
    }
}
