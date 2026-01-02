package net.flowclient.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;

public class EventBus {
    private final Map<Class<? extends Event>, List<EventData>> registry = new HashMap<>();

    // 【追加】登録済みのオブジェクトを記録して、二重登録を防ぐセット
    private final Set<Object> registeredObjects = Collections.synchronizedSet(new HashSet<>());

    // モジュールの登録
    public void register(Object instance) {
        // 【重要】既に登録されているインスタンスなら何もしないで終了
        if (registeredObjects.contains(instance)) {
            // System.out.println("Already registered: " + instance.getClass().getSimpleName());
            return;
        }
        registeredObjects.add(instance);

        Class<?> clazz = instance.getClass();
        Set<String> registeredSignatures = new HashSet<>();

        // 親クラスが存在する限りループする
        while (clazz != null && clazz != Object.class) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Subscribe.class) && method.getParameterCount() == 1) {

                    // メソッドのシグネチャ（名前+引数型）で重複チェック
                    // オーバーライドされたメソッドが親クラスで再度登録されるのを防ぐ
                    String signature = method.getName() + Arrays.toString(method.getParameterTypes());

                    if (registeredSignatures.add(signature)) {
                        Class<?> paramType = method.getParameterTypes()[0];
                        if (Event.class.isAssignableFrom(paramType)) {
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
            clazz = clazz.getSuperclass();
        }

        // デバッグ用: 実際に登録されたメソッド数を確認したい場合
        // System.out.println("Registered " + instance.getClass().getSimpleName() + ": " + registeredSignatures.size() + " methods");
    }

    // イベントの発行
    public void post(Event event) {
        List<EventData> dataList = registry.get(event.getClass());
        if (dataList == null) return;

        for (EventData data : dataList) {
            if (event.isCancellable() && event.isCancelled()) break;
            try {
                data.method().invoke(data.instance(), event);
            } catch (Exception e) {
                System.err.println("Failed to post event to " + data.instance().getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }

    // アンロード時などに登録解除するメソッドもあると便利です
    public void unregister(Object instance) {
        if (registeredObjects.remove(instance)) {
            for (List<EventData> list : registry.values()) {
                list.removeIf(data -> data.instance() == instance);
            }
        }
    }

    private record EventData(Object instance, Method method, EventPriority priority) {}
}