package net.flowclient.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;

public class EventBus {
    private final Map<Class<? extends Event>, List<EventData>> registry = new HashMap<>();

    // モジュールの発行
    public void register(Object instance){
        for(Method method : instance.getClass().getDeclaredMethods()){
            if(method.isAnnotationPresent(Subscribe.class) && method.getParameterCount() == 1){
                Class<?> paramType = method.getParameterTypes()[0];
                if(Event.class.isAssignableFrom(paramType)){
                    Class<? extends Event> eventClass = (Class<? extends Event>) paramType;
                    Subscribe subscribe = method.getAnnotation(Subscribe.class);
                    method.setAccessible(true);
                    List<EventData> dataList = registry.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>());
                    dataList.add(new EventData(instance, method, subscribe.priority()));
                    dataList.sort(Comparator.comparingInt(d -> d.priority().ordinal()));
                }
            }
        }
    }

    // イベントの発行
    public void post(Event event){
        List<EventData> dataList = registry.get(event.getClass());
        if(dataList == null) return;

        for(EventData data : dataList){
            if(event.isCancellable() && event.isCancelled()) break;
            try{
                data.method().invoke(data.instance(), event);
            } catch(Exception e){
                System.err.println("Failed to post event to " + data.instance().getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }

    // インスタンスとメソッドをセットで保持
    private record EventData(Object instance, Method method, EventPriority priority) {}
}
