package com.aiyuba.route_core;


import com.aiyuba.route_annotation.model.RouterMeta;
import com.aiyuba.route_annotation.template.IRouteGroup;
import com.aiyuba.route_annotation.template.IService;

import java.util.HashMap;
import java.util.Map;

public class Warehouse {

    // root 映射表 保存分组信息
    static Map<String, Class<? extends IRouteGroup>> groupsIndex = new HashMap<>();

    // group 映射表 保存组中的所有数据
    static Map<String, RouterMeta> routes = new HashMap<>();

    // group 映射表 保存组中的所有数据
    static Map<Class, IService> services = new HashMap<>();


}
