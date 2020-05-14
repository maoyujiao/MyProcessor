package com.aiyuba.route_compile.utils;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * Created by maoyujiao on 2020/5/12.
 */

public class Log {
    private Messager mMessager;
    private static Log INSTANCE;

    private Log(Messager messager) {
        mMessager = messager;

    }

    public static Log getINSTANCE(Messager messager) {
        if(INSTANCE == null){
            synchronized (Log.class){
                if(INSTANCE == null){
                    INSTANCE = new Log(messager);
                }
            }
        }
        return INSTANCE;
    }

    public void i(String str){
        mMessager.printMessage(Diagnostic.Kind.NOTE,str);
    }
}
