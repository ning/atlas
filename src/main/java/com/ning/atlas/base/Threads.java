package com.ning.atlas.base;

import java.util.Stack;

public class Threads
{

    private static ThreadLocal<Stack<String>> names = new ThreadLocal<Stack<String>>() {
        @Override
        protected Stack<String> initialValue()
        {
            return new Stack<String>();
        }
    };

    public static void pushName(String t) {
        String old_name = Thread.currentThread().getName();
        names.get().push(old_name);
        Thread.currentThread().setName(t);
    }

    public static void popName() {
        Thread.currentThread().setName(names.get().pop());
    }

}
