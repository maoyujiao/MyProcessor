package com.aiyuba.route_compile.processor;

import com.aiyuba.route_annotation.Route;
import com.aiyuba.route_annotation.model.RouterMeta;
import com.aiyuba.route_annotation.template.IRouteGroup;
import com.aiyuba.route_compile.utils.Consts;
import com.aiyuba.route_compile.utils.Log;
import com.aiyuba.route_compile.utils.Utils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by maoyujiao on 2020/5/13.
 */

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(Consts.TYPE_ROUTER)
@AutoService(Processor.class)
@SupportedOptions(Consts.ARGUMENTS_NAME)
public class RouteProcessor extends AbstractProcessor {
    Log log;
    //文件生成器 //用来创建java文件或者class文件
    private Filer filer;

    //节点工具 类函数 属性都是节点
    private Elements elementsUtil;
    //类信息工具类
    private Types typeUtil;
    //分组 key:组名 value:对应组的路由信息
    private Map<String, List<RouterMeta>> groupMap = new HashMap<>();
    private Map<String, String> rootMap = new TreeMap<>();
    private String moduleName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        log = Log.getINSTANCE(processingEnvironment.getMessager());
        log.i("init()");
        filer = processingEnvironment.getFiler();
        elementsUtil = processingEnvironment.getElementUtils();
        typeUtil = processingEnvironment.getTypeUtils();

        //参数是模块名 为了防止多模块/组件化开发的时候 生成相同的 xx$$ROOT$$文件
        Map<String, String> options = processingEnvironment.getOptions();
        if (!Utils.isEmpty(options)) {
            moduleName = options.get(Consts.ARGUMENTS_NAME);
        }
        log.i("RouteProcessor Parmaters:" + moduleName);
        if (Utils.isEmpty(moduleName)) {
            throw new RuntimeException("Not set Processor moduleName.");
        }

    }

    /**
     * 该方法将一轮一轮的遍历源代码
     *
     * @param set              该方法需要处理的注解类型
     * @param roundEnvironment 关于一轮遍历中提供给我们调用的信息.
     * @return 改轮注解是否处理完成 true 下轮或者其他的注解处理器将不会接收到次类型的注解.用处不大.
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        log.i("注册成功");
        if (!set.isEmpty()) {
            //获取所有被router注解的元素集合
            Set<? extends Element> routeElements = roundEnvironment.getElementsAnnotatedWith(Route.class);
            // 处理router注解
            if (!routeElements.isEmpty()) {
                log.i("router class size = " + routeElements.size());
                for (Element element : routeElements) {
                    log.i("class " + ClassName.get((TypeElement) element));
                }
                parseRoutes(routeElements);
            }
        }
        return false;
    }

    private void parseRoutes(Set<? extends Element> routeElements) {
        //支持配置路由类的类型 TypeElement extends Element
        TypeElement activityElement = elementsUtil.getTypeElement(Consts.ACTIVITY);
        //
        TypeMirror activityMirror = activityElement.asType();

        TypeElement serviceElement = elementsUtil.getTypeElement(Consts.ISERVICE);
        //
        TypeMirror serviceMirror = serviceElement.asType();

        for (Element element : routeElements) {
            RouterMeta routerMeta;
            TypeMirror typeMirror = element.asType();
            Route route = element.getAnnotation(Route.class);
            //是否是 Activity 使用了Route注解
            if (typeUtil.isSubtype(typeMirror, activityMirror)) {
                routerMeta = new RouterMeta(RouterMeta.Type.ACTIVITY, route, element);
            } else if (typeUtil.isSubtype(typeMirror, serviceMirror)) {
                routerMeta = new RouterMeta(RouterMeta.Type.ISERVICE, route, element);
            } else {
                throw new RuntimeException("[Just Support Activity/IService Route]" + element.getSimpleName());
            }
            //分组信息记录  groupMap <Group分组,RouteMeta路由信息> 集合
            categories(routerMeta);
        }
        //生成group的类
        TypeElement groupTypeElement = elementsUtil.getTypeElement(Consts.IROUTE_GROUP);
        TypeElement rootTypeElement = elementsUtil.getTypeElement(Consts.IROUTE_ROOT);

        //生成路由java文件
        generateGroup(groupTypeElement);
        /**
         * 生成Root类 作用:记录 <分组，对应的Group类>
         */
        generatedRoot(rootTypeElement, groupTypeElement);
    }

    private void generatedRoot(TypeElement rootTypeElement, TypeElement groupTypeElement) {
        //void loadInto(Map<String, Class<? extends IRouteGroup>> routes);
        ////Wildcard 通配符
        ParameterizedTypeName typeName = ParameterizedTypeName.get(
            ClassName.get(Map.class),
            ClassName.get(String.class),
                ParameterizedTypeName.get(
                        ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(groupTypeElement)))
        );

        ParameterSpec parameterSpec = ParameterSpec.builder(typeName,"routes").build();

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Consts.METHOD_LOAD_INTO)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterSpec)
                .returns(TypeName.VOID);


        for (Map.Entry<String, String> map : rootMap.entrySet()) {
//            routes.put("","");
            methodBuilder.addStatement("routes.put($S,$T.class)",
                    map.getKey(),
                    ClassName.get(Consts.PACKAGE_OF_GENERATE_FILE,map.getValue()));
        }

        //  ClassName extends TypeName
        String rootName = Consts.NAME_OF_ROOT + moduleName;
        try {
            JavaFile.builder(Consts.PACKAGE_OF_GENERATE_FILE,TypeSpec.classBuilder(rootName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ClassName.get(rootTypeElement))
                    .addMethod(methodBuilder.build()).build()).build().writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void generateGroup(TypeElement groupTypeElement) {
        //类名 父类
        Set<Map.Entry<String, List<RouterMeta>>> set = groupMap.entrySet();

        // Map<String, RouterMeta> atlas
        ParameterizedTypeName typeName = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterMeta.class));
        ParameterSpec parameterSpec = ParameterSpec.builder(typeName, "atlas").build();

        //遍历map生成多个group java文件
        for (Map.Entry<String, List<RouterMeta>> entry : set) {
            //方法   public void loadInto(Map<String, RouteMeta> atlas) {}
            MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(Consts.METHOD_LOAD_INTO)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class) //重写
                    .addParameter(parameterSpec)
                    .returns(TypeName.VOID);

            String groupName = entry.getKey();
            List<RouterMeta> metaList = entry.getValue();

            for (RouterMeta routerMeta : metaList) {
                /**
                 * atlas.put("/main/test", RouteMeta.build(
                 * RouteMeta.Type.ACTIVITY,SecondActivity.class, "/main/test", "main"));
                 */
                methodSpec.addStatement(
                        "atlas.put($S,$T.build($T.$L,$T.class,$S,$S))",
                        routerMeta.getPath(),
                        ClassName.get(RouterMeta.class),
                        ClassName.get(RouterMeta.Type.class),
                        routerMeta.getType(),
                        ClassName.get((TypeElement) routerMeta.getElement()),
                        routerMeta.getPath(),
                        routerMeta.getGroup()
                );
            }

            //类名
            String groupClassName = Consts.NAME_OF_GROUP + groupName;

            try {
                JavaFile.builder(Consts.PACKAGE_OF_GENERATE_FILE,
                        TypeSpec.classBuilder(groupClassName)
                                .addMethod(methodSpec.build())
                                .addSuperinterface(ClassName.get(groupTypeElement))
                                .addModifiers(Modifier.PUBLIC).build()).build().writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            rootMap.put(groupName, groupClassName);


        }

    }

    private void categories(RouterMeta routerMeta) {
        //设置group
        vertify(routerMeta);
        //添加分组
        if (!groupMap.containsKey(routerMeta.getGroup())) {
            groupMap.put(routerMeta.getGroup(), new ArrayList<RouterMeta>());
        }
        //添加路由
        List<RouterMeta> list = groupMap.get(routerMeta.getGroup());
        list.add(routerMeta);
    }

    private boolean vertify(RouterMeta meta) {
        String path = meta.getPath();
        String group = meta.getGroup();
        //路由地址必须以 / 开头
        if (Utils.isEmpty(path) || !path.startsWith("/")) {
            return false;
        }
        if (Utils.isEmpty(group)) {
            String defaultGroup = path.substring(1, path.indexOf("/", 1));
            log.i("group name " + defaultGroup);
            if (Utils.isEmpty(defaultGroup)) {
                return false;
            }
            meta.setGroup(defaultGroup);
            return true;
        }
        return true;

    }
}
