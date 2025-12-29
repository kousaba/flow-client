package net.flowclient.gui.widget;

import net.flowclient.gui.widget.setting.*;
import net.flowclient.module.setting.Setting;
import net.flowclient.module.setting.impl.BooleanSetting;
import net.flowclient.module.setting.impl.ColorSetting;
import net.flowclient.module.setting.impl.NumberSetting;
import net.flowclient.module.setting.impl.StringSetting;

import java.util.HashMap;

import java.util.Map;

public class WidgetFactory {
    private static final Map<Class<? extends Setting>, WidgetCreator<?, ?>> REGISTRY = new HashMap<>();

    static{
        register(BooleanSetting.class, (WidgetCreator<Boolean, BooleanSetting>) BooleanSettingWidget::new);
        register(NumberSetting.class, (WidgetCreator<Double, NumberSetting>) NumberSettingWidget::new);
        register(StringSetting.class, (WidgetCreator<String, StringSetting>) StringSettingWidget::new);
        register(ColorSetting.class, (WidgetCreator<Integer, ColorSetting>) ColorSettingWidget::new);
    }

    public static <S extends Setting<?>> void register(Class<S> clazz, WidgetCreator<?, S> creator) {
        REGISTRY.put(clazz, creator);
    }

    @SuppressWarnings("unchecked")
    public static SettingWidget<?, ?> create(int x, int y, int width, int height, Setting<?> setting){
        WidgetCreator<?, Setting<?>> creator = (WidgetCreator<?, Setting<?>>) REGISTRY.get(setting.getClass());

        if(creator == null){
            throw new IllegalArgumentException("No widget registered for setting type: " + setting.getClass().getName());
        }

        return creator.create(x,y,width,height,setting);
    }

    @FunctionalInterface
    public interface WidgetCreator<T, S extends Setting<T>>{
        SettingWidget<T, S> create(int x, int y, int width, int height, S setting);
    }
}
