package com.xiaoju.automarket.paladin.core.common;

import lombok.Getter;

/**
 * @Author Luogh
 * @Date 2020/12/7
 **/
@Getter
public class JTuple2<K, V> {
    private final K k;
    private final V v;

    public JTuple2(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public static <K, V> JTuple2<K, V> of (K k, V v) {
       return new JTuple2<>(k, v);
    }

}
