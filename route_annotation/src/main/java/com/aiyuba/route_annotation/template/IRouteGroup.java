package com.aiyuba.route_annotation.template;



import com.aiyuba.route_annotation.model.RouterMeta;

import java.util.Map;

public interface IRouteGroup {
    void loadInto(Map<String, RouterMeta> atlas);
}
