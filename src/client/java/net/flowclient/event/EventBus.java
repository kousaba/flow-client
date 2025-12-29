package net.flowclient.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Flow;

public class EventBus {
    private final Map<Class<? extends Event>, List<Subscriber>> registry = new HashMap<>();

    // モジュールの発行
    public void register(Object instance){
        for(Method method : instance.getClass().getDeclaredMethods()){
            if(method.isAnnotationPresent(Subscribe.class) && method.getParameterCount() == 1){
                Class<?> paramType = method.getParameterTypes()[0];
                if(Event.class.isAssignableFrom(paramType)){
                    Class<? extends Event> eventClass = (Class<? extends Event>) paramType;
                    registry.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(new Subscriber(instance,method));
                }
            }
        }
    }

    // イベントの発行
    public void post(Event event){
        List<Subscriber> subscribers = registry.get(event.getClass());
        if(subscribers != null){
            for(Subscriber s : subscribers){
                s.invoke(event);
            }
        }
    }

    // インスタンスとメソッドをセットで保持
    private record Subscriber(Object instance, Method method){
        public void invoke(Event event){
            try{
                method.invoke(instance, event);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
