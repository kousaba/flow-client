package net.flowclient.script.runtime;

import net.flowclient.Flow;
import net.flowclient.event.impl.ItemTooltipEvent;
import net.flowclient.gui.screen.FlowScriptScreen;
import net.flowclient.module.impl.ScriptLibModule;
import net.flowclient.module.impl.ScriptModule;
import net.flowclient.module.setting.impl.NumberSetting;
import net.flowclient.script.parser.FlowScriptParser;
import net.flowclient.util.FlowLogger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.List;

public class FlowScriptLib {
    public static double fovModifier = 1.0;
    public static Double overrideGamma = null;
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
        interpreter.registerNativeFunction("is_key_down", args -> {
            String keyName = args.get(0).toString().toUpperCase();
            if(keyName.equals("LMB")) return GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;
            if(keyName.equals("RMB")) return GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_2) == GLFW.GLFW_PRESS;
            int keyCode = getKeyCode(keyName);
            if(keyCode == -1) return false;
            Window handle = MinecraftClient.getInstance().getWindow();
            return InputUtil.isKeyPressed(handle, keyCode);
        });
        interpreter.registerNativeFunction("set_fov_modifier", args -> {
            if(!args.isEmpty()){
                fovModifier = ScriptUtils.asDouble(args.get(0));
            }
            return null;
        });
        interpreter.registerNativeFunction("get_fov_mofidier", args -> fovModifier);
        interpreter.registerNativeFunction("set_gamma", args -> {
            if(!args.isEmpty()){
                overrideGamma = ScriptUtils.asDouble(args.get(0));
            }else{
                overrideGamma = null;
            }
            return null;
        });
        interpreter.registerNativeFunction("get_gamma", args -> overrideGamma);
        interpreter.registerNativeFunction("set_sprinting", args -> {
            if (!args.isEmpty()) {
                boolean sprint = ScriptUtils.asBoolean(args.get(0));
                var player = MinecraftClient.getInstance().player;
                if (player != null) {
                    player.setSprinting(sprint);
                }
            }
            return null;
        });
        interpreter.registerNativeFunction("send_chat", args -> {
            if(!args.isEmpty()){
                String msg = args.get(0).toString();
                var player = MinecraftClient.getInstance().player;
                if(player != null){
                    player.networkHandler.sendChatMessage(msg);
                }
            }
            return null;
        });
        interpreter.registerNativeFunction("contains", args -> {
            if(args.size() < 2) return false;
            String target = args.get(0).toString();
            String search = args.get(1).toString();
            return target.contains(search);
        });
        interpreter.registerNativeFunction("to_lower", args -> {
            if(args.isEmpty()) return "";
            return args.get(0).toString().toLowerCase();
        });
        interpreter.registerNativeFunction("tooltip_add", args -> {
            if(ScriptContext.currentTooltipEvent != null && !args.isEmpty()){
                String text = args.get(0).toString();
                ScriptContext.currentTooltipEvent.add(text);
            }
            return null;
        });
        interpreter.registerNativeFunction("tooltip_get_item_id", args -> {
            if(ScriptContext.currentTooltipEvent != null){
                var stack = ScriptContext.currentTooltipEvent.getStack();
                return net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
            }
            return "null";
        });
        interpreter.registerNativeFunction("tooltip_get_item_name", args -> (ScriptContext.currentTooltipEvent != null ? ScriptContext.currentTooltipEvent.getStack().getName().toString() : "null"));
        interpreter.registerNativeFunction("tooltip_get_item_damage", args -> (ScriptContext.currentTooltipEvent != null ? (double) ScriptContext.currentTooltipEvent.getStack().getDamage() : 0.0));
        interpreter.registerNativeFunction("tooltip_get_item_max_damage", args -> (ScriptContext.currentTooltipEvent != null ? (double) ScriptContext.currentTooltipEvent.getStack().getMaxDamage() : 0.0));
    }

    private static int getKeyCode(String keyName){
        if(keyName.length() == 1){
            char c = keyName.charAt(0);
            if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                return c;
            }
        }
        if(keyName.charAt(0) == 'F'){
            try{
                int fn = Integer.parseInt(keyName.substring(1));
                if(fn >= 1 && fn <= 12){
                    return GLFW.GLFW_KEY_F1 + (fn - 1);
                }
            } catch(Exception ignored){}
        }
        return switch(keyName){
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "SHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RSHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "CTRL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "ALT" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "ENTER" -> GLFW.GLFW_KEY_ENTER;
            case "ESC" -> GLFW.GLFW_KEY_ESCAPE;
            case "UP" -> GLFW.GLFW_KEY_UP;
            case "DOWN" -> GLFW.GLFW_KEY_DOWN;
            case "LEFT" -> GLFW.GLFW_KEY_LEFT;
            case "RIGHT" -> GLFW.GLFW_KEY_RIGHT;
            case "BACKSPACE" -> GLFW.GLFW_KEY_BACKSPACE;
            case "DELETE" -> GLFW.GLFW_KEY_DELETE;
            case "INSERT" -> GLFW.GLFW_KEY_INSERT;
            case "HOME" -> GLFW.GLFW_KEY_HOME;
            case "END" -> GLFW.GLFW_KEY_END;
            case "PAGEUP" -> GLFW.GLFW_KEY_PAGE_UP;
            case "PAGEDOWN" -> GLFW.GLFW_KEY_PAGE_DOWN;
            default -> -1;
        };
    }
}
