package com.nekonade.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ProtoBufUtils {

    private final static Logger log = LoggerFactory.getLogger(ProtoBufUtils.class);

    public static List<String> baseTypeList =
            Arrays.asList("int", "Integer", "float", "Float", "long", "Long", "double", "Double", "String", "Boolean", "boolean");

    public static  <T,K> Object transformProtoReturnBean(T goalBuilder, K sourceBean) {
        try{
            transformProtoReturnBuilder(goalBuilder, sourceBean);
            Method build = goalBuilder.getClass().getDeclaredMethod("build");
            return build.invoke(goalBuilder);
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
    //
    public static <T,K> T transformProtoReturnBuilder(T goalBuilder, K sourceBean) {
        Method[] goalBuilderMethod = goalBuilder.getClass().getDeclaredMethods();
        Map<Field, Class> listClassMap = new HashMap<>();

        Map<Field, Method> listGetMethodMap = new HashMap<>();
        Map<Field, Method> goalBuilderAddMethodMap = new HashMap<>();
        Map<Field, Method> goalBuildBaseTypeAddMethodMap = new HashMap<>();

        Map<Field, Method> sourceBeanGetMethodMap = new HashMap<>();
        Map<Field, Method> goalBuilderSetMethodMap = new HashMap<>();

        Map<Field, Method> sourceBeanFunctionMap = new HashMap<>();

        //获取K中的所有属性名称，排除@ProtoField(ignore=false)的属性, 获取需要注入List的属性
        getAllNeedMethod(goalBuilder, sourceBean, goalBuilderMethod, listClassMap, listGetMethodMap,
                goalBuilderAddMethodMap, sourceBeanGetMethodMap, goalBuilderSetMethodMap, sourceBeanFunctionMap);
        //进行get set方法执行
        invokeGetAndSet(goalBuilder, sourceBean, sourceBeanGetMethodMap, goalBuilderSetMethodMap);
        //function执行
        sourceBeanFunctionMap.values().forEach(m -> invokeMethod(m, sourceBean, goalBuilder));

        for (Map.Entry<Field, Class> entry : listClassMap.entrySet()) {
            Field field = entry.getKey();
            Class listClass = entry.getValue();
            //get方法
            Method getListMethod = listGetMethodMap.get(field);
            List invoke = (List) invokeMethod(getListMethod, sourceBean);
            if(invoke == null) {
                continue;
            }
            if(baseTypeList.contains(listClass.getSimpleName())) {
                for (Object o : invoke) {
                    Optional.ofNullable(goalBuildBaseTypeAddMethodMap.get(field)).ifPresent(m -> invokeMethod(m, goalBuilder, o));
                }
            } else {
                for (int i = 0; i < invoke.size(); i++) {
                    try {
                        Object newBuilder = getMethod(getNewInstance(listClass), "newBuilder").invoke(null);
                        invokeMethod(goalBuilderAddMethodMap.get(field),  goalBuilder, transformProtoReturnBean(newBuilder, invoke.get(i)));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return goalBuilder;
    }

    private static Object getNewInstance(Class listClass) {
        Constructor cellConstruct = null;
        try {
            cellConstruct = listClass.getDeclaredConstructor();
            cellConstruct.setAccessible(true);
            return cellConstruct.newInstance();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static <T, K> void invokeGetAndSet(T goalBuilder, K sourceBean, Map<Field, Method> sourceBeanGetMethodMap,
                                               Map<Field, Method> goalBuilderSetMethodMap) {
        for (Map.Entry<Field, Method>  getMethodEntry: sourceBeanGetMethodMap.entrySet()) {
            Field field = getMethodEntry.getKey();
            field.setAccessible(true);

            Method getMethod = getMethodEntry.getValue();
            Method setMethod = goalBuilderSetMethodMap.get(field);

            Optional.ofNullable(invokeMethod(getMethod, sourceBean)).ifPresent(val -> invokeMethod(setMethod, goalBuilder, val));
        }
    }

    private static Object invokeMethod(Method method, Object obj, Object... args) {
        try {
            return method.invoke(obj, args);
        } catch (IllegalArgumentException e) {
            log.info(method.getName() + "参数类型匹配异常");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static <T, K> void getAllNeedMethod(T goalBuilder, K sourceBean, Method[] goalBuilderMethod,
                                                Map<Field, Class> listClassMap, Map<Field, Method> listGetMethodMap,
                                                Map<Field, Method> goalBuilderAddMethodMap, Map<Field, Method> sourceBeanGetMethodMap,
                                                Map<Field, Method> goalBuilderSetMethodMap, Map<Field, Method> sourceBeanFunctionMap) {
        List<Field> declaredFields = getClassField(sourceBean.getClass());//.getFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);

            AtomicReference<Boolean> inject = new AtomicReference<>(true);

            String fieldName = declaredField.getName();
            String getMethodName = "get" + upperCaseFirstLetter(fieldName);
            String boolGetMethodName = "is" + upperCaseFirstLetter(fieldName);
            String setMethodName = "set" + upperCaseFirstLetter(fieldName.replaceAll("_", ""));

            Arrays.stream(declaredField.getAnnotations())
                    .filter(anno -> anno instanceof ProtoField)
                    .map(anno -> (ProtoField) anno)
                    .findFirst()
                    .ifPresent(protoAnno -> {
                        //查看ignore屬性
                        if(protoAnno.Ignore()) {
                            inject.set(false);
                            return;
                        }
                        Class targetClass = protoAnno.TargetClass();
                        //如果K中有Function需要执行的，加入执行
                        if(!protoAnno.Function().isEmpty()) {
                            protoFieldFunction(sourceBean, sourceBeanFunctionMap, declaredField, protoAnno, targetClass);
                            inject.set(false);
                            return;
                        }
                        /**如果sourcebean中有List,检查declaredMethods是否有add的方法，没有则跳过，有则加入执行
                         * 譬如Bag类中的itemList字段
                         */
                        String simpleName = targetClass.getSimpleName();
                        try {
                            Method addMethod;
                            String addMethodName;
                            if(baseTypeList.contains(simpleName)) {
                                addMethodName = "add" + upperCaseFirstLetter(protoAnno.TargetRepeatedName());
                            } else {
                                addMethodName = "add" + upperCaseFirstLetter(simpleName);
                            }
                            if(!targetClass.equals(Void.class)
                                    && (addMethod = hasListAddMethond(goalBuilderMethod, addMethodName, targetClass)) != null) {
                                listClassMap.put(declaredField, targetClass);
                                //list的get方法
                                listGetMethodMap.put(declaredField, getMethod(sourceBean, getMethodName));
                                //add方法
                                goalBuilderAddMethodMap.put(declaredField, addMethod);
                            }
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } finally {
                            inject.set(false);
                        }
                    });
            //正常需要注入的屬性
            if(inject.get()) {
                try {
                    Method getMethod;
                    Class<?> type = declaredField.getType();
                    if(type.getName().equals("Boolean") || type.getName().equals("boolean")) {
                        getMethod = getMethod(sourceBean, boolGetMethodName);
                    } else {
                        getMethod = getMethod(sourceBean, getMethodName);
                    }
                    if(getMethod.getReturnType().equals(type)) {
                        sourceBeanGetMethodMap.put(declaredField, getMethod);
                        goalBuilderSetMethodMap.put(declaredField, getMethod(goalBuilder,  setMethodName, type));
                    } else {
                        throw  new NoSuchMethodException(sourceBean.getClass().getSimpleName() + "中，" + getMethodName + "方法返回的不是" + type);
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private static <K> void protoFieldFunction(K sourceBean, Map<Field, Method> sourceBeanFunctionMap,
                                               Field declaredField, ProtoField protoAnno, Class targetClass) {
        Method method = null;
        try {
            if(!targetClass.equals(Void.class)
                    && !protoAnno.TargetRepeatedName().isEmpty()) {
                method = sourceBean.getClass().getMethod(protoAnno.Function(), targetClass);
            } else {
                method = sourceBean.getClass().getDeclaredMethod(protoAnno.Function());
            }

        } catch (NoSuchMethodException e) {
            try{
                if(!targetClass.equals(Void.class)) {
                    method = getDeclaredMethod(sourceBean.getClass(), protoAnno.Function(), targetClass);
                } else {
                    method = getDeclaredMethod(sourceBean.getClass(), protoAnno.Function());
                }
            } catch (NoSuchMethodException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();

        }
        if(method != null) {
            sourceBeanFunctionMap.put(declaredField, method);
        }
    }

    //对java API中的获取方法做一个基本类型的兼容
    private static <T> Method getMethod(T goalBuilder, String methodName, Class<?>... type) throws NoSuchMethodException {
        if(type == null || type.length == 0) {
            return getDeclaredMethod(goalBuilder.getClass(), methodName);
        } else if(type.length == 1) {
            String typeName = type[0].getName();
            try {
                Method declaredMethod = getDeclaredMethod(goalBuilder.getClass(), methodName, type[0]);
                return declaredMethod;
            } catch (NoSuchMethodException e) {
                List<Method> declaredMethods = getClassMethod(goalBuilder.getClass(), methodName);
                for (Method declaredMethod : declaredMethods) {
                    Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
                    if(parameterTypes.length == 1 && baseTypeList.contains(parameterTypes[0].getName())) {
                        return declaredMethod;
                    }
                }
                e.printStackTrace();
                throw  new NoSuchMethodException("在"+ goalBuilder.getClass().getName() +"中没有"+ methodName +"方法，请检查proto文件是否跟bean定义的字段一致");
            }
        } else {
            return  getDeclaredMethod(goalBuilder.getClass(),methodName, type);
        }

    }

    private static Method hasListAddMethond(Method[] declaredMethods, String targetAddMethodName, Class targetClass) {
        return Arrays.stream(declaredMethods)
                .filter(method -> method.getName().equals(targetAddMethodName))
                .filter(method -> method.getGenericParameterTypes().length == 1
                        && method.getGenericParameterTypes()[0].getTypeName().equals(targetClass.getName()))
                .findFirst()
                .orElse(null);
    }

    private static String upperCaseFirstLetter(String word) {
        try {
            return String.valueOf(word.charAt(0)).toUpperCase() + word.substring(1);
        } catch (StringIndexOutOfBoundsException e) {
            System.out.println(word);
            throw e;
        }
    }

    static List<Field> getClassField(Class cur_class) {
        String class_name = cur_class.getName();
        Field[] obj_fields = cur_class.getDeclaredFields();
        List<Field> collect = Arrays.stream(obj_fields).collect(Collectors.toList());
        //Method[] methods = cur_class.getDeclaredMethods();

        if (cur_class.getSuperclass() != null && cur_class.getSuperclass() != Object.class) {
            collect.addAll(getClassField(cur_class.getSuperclass()));
        }
        return collect;
    }

    static List<Method> getClassMethod(Class cur_class, String methodName) {
        Method[] methods = cur_class.getDeclaredMethods();
        List<Method> collect = Arrays.stream(methods)
                .filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());

        if (cur_class.getSuperclass() != null && cur_class.getSuperclass() != Object.class) {
            collect.addAll(getClassMethod(cur_class.getSuperclass(), methodName));
        }
        return collect;
    }


    static Method getDeclaredMethod(Class cur_class, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException{
        Method result = null;
        try {
            result = cur_class.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            // e.printStackTrace();
        }
        if (result == null && (cur_class.getSuperclass() != null && cur_class.getSuperclass() != Object.class)) {
            result = getDeclaredMethod(cur_class.getSuperclass(), methodName);
            if(result == null) {
                String content = null;
                if(parameterTypes != null) {
                    for (Class<?> parameterType : parameterTypes) {
                        content += parameterType.getSimpleName();
                    }
                }
                throw new NoSuchMethodException("在" + cur_class.getName() + "中递归找不到该方法" + methodName + "(" + content + ")");
            }
        }
        return result;
    }
}
