package com.aiyuba.module1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.aiyuba.route_annotation.Extra;
import com.aiyuba.route_annotation.Route;
import com.aiyuba.route_core.DNRouter;

@Route(path = "/module/main")
public class ModuleActivity extends AppCompatActivity {
    private static final String TAG = "ModuleActivity";

    //设置跳转改界面需要接受的参数
    @Extra(name = "parma1")
    boolean param;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);
        Log.d(TAG, "onCreate: " + param);
    }
}
