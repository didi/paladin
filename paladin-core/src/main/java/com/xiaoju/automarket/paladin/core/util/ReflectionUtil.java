package com.xiaoju.automarket.paladin.core.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ReflectionUtil {


    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(String subClassName, Class<T> interfaceClass) {
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(subClassName);
            return clazz.asSubclass(interfaceClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(String subClassName, Class<T> interfaceClass, Class<?>[] paramTypes, Object[] params) {
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(subClassName);
            return clazz.asSubclass(interfaceClass).getConstructor(paramTypes).newInstance(params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}