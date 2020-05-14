package com.aiyuba.route_annotation.model;


import com.aiyuba.route_annotation.Route;

import javax.lang.model.element.Element;

/**
 * Created by maoyujiao on 2020/5/12.
 */

public class RouterMeta {
    public enum Type{
        ACTIVITY,
        ISERVICE
    }

    private Type type;
    private Element element;
    private String path;
    private Class<?> destinaation;
    private String group;

    public static RouterMeta build(Type type, Class<?> destination, String path, String
            group) {
        return new RouterMeta(type, null, destination, path, group);
    }

    public RouterMeta(Type type, Element element, Class<?> destinaation, String path, String group) {
        this.type = type;
        this.element = element;
        this.path = path;
        this.destinaation = destinaation;
        this.group = group;
    }

    public RouterMeta() {
    }

    public RouterMeta(Type type, Route route, Element element) {
        this(type, element, null, route.path(), route.group());
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Class<?> getDestinaation() {
        return destinaation;
    }

    public void setDestinaation(Class<?> destinaation) {
        this.destinaation = destinaation;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
