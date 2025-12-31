package net.flowclient.script.runtime;

public class ScriptUtils {
    // doubleとして取得
    public static double asDouble(Object o){
        if(o instanceof Number n) return n.doubleValue();
        if(o instanceof Boolean b) return b ? 1.0 : 0.0;
        return 0.0;
    }

    // 真偽値として取得
    public static boolean asBoolean(Object o){
        if(o instanceof Boolean b) return b;
        if(o instanceof Number n) return n.doubleValue() != 0;
        return o != null; // null以外はtrue
    }

    // 文字列化
    public static String asString(Object o){
        return o == null ? "null" : o.toString();
    }
}
