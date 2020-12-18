package com.xiaoju.automarket.paladin.core.util;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * @Author Luogh
 * @Date 2020/12/8
 **/
public class Util {

    private static final Consumer<Object> IGNORE_FN = ignored -> {
    };

    @SuppressWarnings("unchecked")
    public static <T> Consumer<T> ignoreFn() {
        return (Consumer<T>) IGNORE_FN;
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
