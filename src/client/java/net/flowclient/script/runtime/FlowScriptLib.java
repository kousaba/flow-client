package net.flowclient.script.runtime;

import net.flowclient.Flow;
import net.flowclient.gui.screen.FlowScriptScreen;
import net.flowclient.module.impl.ScriptLibModule;
import net.flowclient.module.setting.impl.NumberSetting;
import net.flowclient.util.FlowLogger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;

public class FlowScriptLib {
    public static DrawContext currentContext = null;
    public static void registerAll(FlowScriptInterpreter interpreter){
        ScriptLibModule settings = Flow.INSTANCE.moduleManager.getModule(ScriptLibModule.class);
        interpreter.registerNativeFunction("print_log", args -> {
            StringBuilder sb = new StringBuilder();
            for(Object arg : args){
                sb.append(arg).append(" ");
            }
            System.out.println("[FlowScript] " + sb.toString().trim());
            FlowScriptScreen.addLog(sb.toString().trim());
            return null;
        });
        interpreter.registerNativeFunction("print_err", args -> {
            StringBuilder sb = new StringBuilder();
            for(Object arg : args){
                sb.append(arg).append(" ");
            }
            System.err.println("[FlowScript] " + sb.toString().trim());
            return null;
        });
        interpreter.registerNativeFunction("to_string", args -> {
            if(args.isEmpty()) return "null";
            return args.get(0).toString();
        });

        // クライアントの情報取得
        interpreter.registerNativeFunction("get_fps", args -> {
            return MinecraftClient.getInstance().getCurrentFps();
        }, () -> {
            return settings.getSetting("FPS_Interval", NumberSetting.class).getData();
        });
        interpreter.registerNativeFunction("get_ping", args -> {
           var player = MinecraftClient.getInstance().player;
           if(player == null) return 0;
           var entry = MinecraftClient.getInstance().getNetworkHandler().getPlayerListEntry(player.getUuid());
           return entry != null ? entry.getLatency() : 0;
        }, () -> {
            return settings.getSetting("Ping_Interval", NumberSetting.class).getData();
        });
        interpreter.registerNativeFunction("get_pos_x", args -> {
            var player = MinecraftClient.getInstance().player;
            return player != null ? player.getX() : 0.0;
        }, () -> {
            return settings.getSetting("Pos_Interval", NumberSetting.class).getData();
        });
        interpreter.registerNativeFunction( "get_pos_y", args -> {
            var player = MinecraftClient.getInstance().player;
            return player != null ? player.getY() : 0.0;
        }, () -> {
            return settings.getSetting("Pos_Interval", NumberSetting.class).getData();
        });
        interpreter.registerNativeFunction("get_pos_z", args -> {
            var player = MinecraftClient.getInstance().player;
            return player != null ? player.getZ() : 0.0;
        }, () -> {
            return settings.getSetting("Pos_Interval", NumberSetting.class).getData();
        });
        interpreter.registerNativeFunction("len", args -> {
            if(args.isEmpty()) return 0;
            if(args.get(0) instanceof List<?> list) return list.size();
            if(args.get(0) instanceof String s) return s.length();
            return 0;
        });
        interpreter.registerNativeFunction("push", args -> {
            if(args.size() == 2 && args.get(0) instanceof List list){
                list.add(args.get(1));
            }
            return null;
        });
        interpreter.registerNativeFunction("get", args -> {
            if(args.size() == 2 && args.get(0) instanceof List list && args.get(1) instanceof Number n){
                int idx = n.intValue();
                if(idx >= 0 && idx < list.size()) return list.get(idx);
            }
            return null;
        });
        interpreter.registerNativeFunction("contains", args -> {
            if(args.size() == 2 && args.get(0) instanceof List list){
                return list.contains(args.get(1));
            }
            return null;
        });
        interpreter.registerNativeFunction("substring", args -> {
            String s = args.get(0).toString();
            int start = ((Number) args.get(1)).intValue();
            int end = args.size() > 2 ? ((Number) args.get(2)).intValue() : s.length();
            return s.substring(Math.max(0, start), Math.min(s.length(), end));
        });
        interpreter.registerNativeFunction("round", args -> Math.round(ScriptUtils.asDouble(args.get(0))));
        interpreter.registerNativeFunction("max", args -> Math.max(ScriptUtils.asDouble(args.get(0)), ScriptUtils.asDouble(args.get(1))));
        interpreter.registerNativeFunction("min", args -> Math.min(ScriptUtils.asDouble(args.get(0)), ScriptUtils.asDouble(args.get(1))));
        interpreter.registerNativeFunction("get_screen_width", args -> MinecraftClient.getInstance().getWindow().getScaledWidth());
        interpreter.registerNativeFunction("get_screen_height", args -> MinecraftClient.getInstance().getWindow().getScaledHeight());
        interpreter.registerNativeFunction("measure_text_width", args -> {
            if(args.isEmpty()) return 0;
            String text = args.get(0).toString();
            return MinecraftClient.getInstance().textRenderer.getWidth(text);
        });
        interpreter.registerNativeFunction("draw_text", args -> {
            if(currentContext == null || args.size() < 4) return null;
            String text = args.get(0).toString();
            int x = (int) ScriptUtils.asDouble(args.get(1));
            int y = (int) ScriptUtils.asDouble(args.get(2));
            int color = (int) ScriptUtils.asDouble(args.get(3));
            currentContext.drawText(MinecraftClient.getInstance().textRenderer, text, x, y, color, true);
            return null;
        });
        interpreter.registerNativeFunction("draw_rect", args -> {
            if(currentContext == null || args.size() < 5) return null;
            int x = (int) ScriptUtils.asDouble(args.get(0));
            int y = (int) ScriptUtils.asDouble(args.get(1));
            int w = (int) ScriptUtils.asDouble(args.get(2));
            int h = (int) ScriptUtils.asDouble(args.get(3));
            int color = (int) ScriptUtils.asDouble(args.get(4));
            currentContext.fill(x,y,x + w,y + h,color);
            return null;
        });
        interpreter.registerNativeFunction("draw_item", args -> {
            if (currentContext == null || args.size() < 3) return null;

            String itemId = args.get(0).toString();

            // 【修正】インデックスを 1 と 2 に変更
            int x = (int) ScriptUtils.asDouble(args.get(1));
            int y = (int) ScriptUtils.asDouble(args.get(2));

            // アイテムIDのパース (コロンがない場合などの対策を含めるとなお良し)
            if (!itemId.contains(":")) {
                itemId = "minecraft:" + itemId;
            }

            Item item = Registries.ITEM.get(Identifier.of(itemId));
            currentContext.drawItem(item.getDefaultStack(), x, y);
            return null;
        });
        interpreter.registerNativeFunction("draw_slot", args -> {
            if(currentContext == null || args.size() < 3) return null;
            int slot = (int) ScriptUtils.asDouble(args.get(0));
            int x = (int) ScriptUtils.asDouble(args.get(1));
            int y = (int) ScriptUtils.asDouble(args.get(2));

            var player = MinecraftClient.getInstance().player;
            if(player != null){
                ItemStack stack = player.getInventory().getStack(slot);
                if(!stack.isEmpty()){
                    currentContext.drawItem(stack, x, y);
                    currentContext.drawStackOverlay(MinecraftClient.getInstance().textRenderer, stack, x, y);
                }
            }
            return null;
        });
    }
}
