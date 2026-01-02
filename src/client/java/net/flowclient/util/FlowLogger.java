package net.flowclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class FlowLogger {
    public static boolean DEBUG_MODE = true;

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String GRAY = "\u001B[90m";


    public static void info(Object message){
        print("INFO", message, CYAN);
    }
    public static void warn(Object message){
        print("WARN", message, YELLOW);
    }
    public static void error(Object message){
        print("ERROR", message, RED);
    }
    public static void debug(Object message){
        if(DEBUG_MODE){
            print("DEBUG", message, GRAY);
        }
    }

    public static void chat(String message){
        if(MinecraftClient.getInstance().player != null){
            MinecraftClient.getInstance().player.sendMessage(Text.of("§b[Flow] §r" + message), false);
        }
    }

    private static void print(String level, Object message, String color) {
        // 1. 呼び出し元の情報を取得 (ここが魔法のポイント！)
        // StackWalkerを使って、このメソッドを呼んだクラスを探す
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        String callerInfo = walker.walk(frames -> frames
                .skip(2) // print -> info -> [呼び出し元] なので2つ飛ばす
                .findFirst()
                .map(frame -> {
                    String className = frame.getClassName();
                    // パッケージ名を短縮 (net.flowclient.module.Killaura -> Killaura)
                    // デバッグならパッケージ名を出す
                    if(Objects.equals(level, "DEBUG")){
                        return className + ":" + frame.getLineNumber();
                    }
                    String simpleName = className.substring(className.lastIndexOf('.') + 1);
                    return simpleName + ":" + frame.getLineNumber();
                })
                .orElse("Unknown"));


        // 整形して出力
        System.out.println(String.format("",
                color, level, callerInfo, message, RESET));
    }
}
