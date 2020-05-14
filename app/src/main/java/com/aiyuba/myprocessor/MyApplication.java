package com.aiyuba.myprocessor;

import android.app.Application;

import com.aiyuba.route_core.DNRouter;

/**
 * Created by maoyujiao on 2020/5/13.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DNRouter.init(this);
    }
}
