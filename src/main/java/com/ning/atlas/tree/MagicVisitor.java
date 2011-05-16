package com.ning.atlas.tree;


import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MagicVisitor<TreeType extends Tree<TreeType>, BatonType> implements Visitor<TreeType, BatonType>
{
    private static final ConcurrentMap<Class, Dispatcher> DISPATCH_CACHE = new ConcurrentHashMap<Class, Dispatcher>();

    private final Object target;
    private final Dispatcher dispatcher;

    public MagicVisitor()
    {
        this(null);
    }

    public MagicVisitor(Object delegate)
    {
        this.target = delegate == null ? this : delegate;
        Dispatcher dispatch = DISPATCH_CACHE.get(this.target.getClass());
        if (dispatch == null) {
            dispatch = new Dispatcher();
            for (Method method : this.target.getClass().getDeclaredMethods()) {
                method.setAccessible(true);
                List<Class<?>> pts = Arrays.asList(method.getParameterTypes());
                if (pts.size() == 2 && method.getReturnType().equals(pts.get(1))) {
                    if ("enter".equals(method.getName())) {
                        dispatch.entersWithBaton.put(pts.get(0), method);
                    }
                    else if ("exit".equals(method.getName())) {
                        dispatch.exitsWithBaton.put(pts.get(0), method);
                    }
                    else if ("on".equals(method.getName())) {
                        dispatch.onsWithBaton.put(pts.get(0), method);
                    }
                }
                else if (pts.size() == 1 && "void".equals(method.getReturnType().getName())) {
                    if ("enter".equals(method.getName())) {
                        dispatch.entersNoBaton.put(pts.get(0), method);
                    }
                    else if ("exit".equals(method.getName())) {
                        dispatch.exitsNoBaton.put(pts.get(0), method);
                    }
                    else if ("on".equals(method.getName())) {
                        dispatch.onsNoBaton.put(pts.get(0), method);
                    }
                }
            }
            Dispatcher prev = DISPATCH_CACHE.putIfAbsent(this.target.getClass(), new Dispatcher());
            if (prev != null) {
                dispatch = prev;
            }
        }
        this.dispatcher = dispatch;
    }

    public final BatonType enter(TreeType node, BatonType baton)
    {
        withOutBaton(node, dispatcher.entersNoBaton);
        return withBaton(node, baton, dispatcher.entersWithBaton);
    }

    public BatonType on(TreeType node, BatonType baton)
    {
        withOutBaton(node, dispatcher.onsNoBaton);
        return withBaton(node, baton, dispatcher.onsWithBaton);
    }

    public final BatonType exit(TreeType node, BatonType baton)
    {
        withOutBaton(node, dispatcher.exitsNoBaton);
        return withBaton(node, baton, dispatcher.exitsWithBaton);
    }

    private <BatonType, TreeType> BatonType withBaton(TreeType node,
                                                      BatonType baton,
                                                      Map<Class, Method> handlers)
    {
        Class tt = node.getClass();
        if (handlers.containsKey(tt)) {
            Method m = handlers.get(tt);
            try {
                return (BatonType) m.invoke(target, node, baton);
            }
            catch (Exception e) {
                throw new IllegalStateException("unable to invoke visitor delegate", e);
            }
        }
        return baton;
    }

    private <TreeType> void withOutBaton(TreeType node,
                                         Map<Class, Method> handlers)
    {
        Class tt = node.getClass();
        if (handlers.containsKey(tt)) {
            Method m = handlers.get(tt);
            try {
                m.invoke(target, node);
            }
            catch (Exception e) {
                throw new IllegalStateException("unable to invoke visitor delegate", e);
            }
        }
    }

    private static class Dispatcher
    {
        private final Map<Class, Method> entersWithBaton = new HashMap<Class, Method>();
        private final Map<Class, Method> exitsWithBaton = new HashMap<Class, Method>();
        private final Map<Class, Method> onsWithBaton = new HashMap<Class, Method>();

        private final Map<Class, Method> entersNoBaton = new HashMap<Class, Method>();
        private final Map<Class, Method> exitsNoBaton = new HashMap<Class, Method>();
        private final Map<Class, Method> onsNoBaton = new HashMap<Class, Method>();


    }
}
