package org.tmmi.events;


import org.tmmi.PlayerEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;

import static org.tmmi.Main.log;

public class EventExecutor {

    public static List<Class<?>> listenerClasses;
    public static List<Method> listenerMethods;

    public static void callEvent(Object o) {
        log(Level.WARNING, "Ok so we workin");
        if (o instanceof PlayerEvent) {
            Listener l = new Listener() {
                @Override
                public int hashCode() {
                    return super.hashCode();
                }
            };
            for (Method m : listenerMethods) {
                if (m.getParameterTypes()[0] == o.getClass()) {
                    try {
                        m.invoke(l, o);
                    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    public static void load() {

    }
}
