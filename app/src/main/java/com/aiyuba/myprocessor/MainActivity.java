package com.aiyuba.myprocessor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.aiyuba.route_annotation.Extra;
import com.aiyuba.route_annotation.Route;
import com.aiyuba.route_core.DNRouter;

import java.util.Map;

@Route(path = "/main/module")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //注入后就可以获取属性中设置的extra
        DNRouter.getInstance().inject(this);


    }

    public void jump(View view) {
//        DNRouter.getInstance().build("/module/module").navigation(this);
        DNRouter.getInstance().build("/module/main").withBoolean("parma1",true).navigation(this);

    }
}
