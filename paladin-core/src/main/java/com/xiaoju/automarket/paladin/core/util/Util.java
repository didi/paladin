package com.xiaoju.automarket.paladin.core.util;

import java.util.UUID;

/**
 * @Author Luogh
 * @Date 2020/12/8
 **/
public class Util {

    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
