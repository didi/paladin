package com.xiaoju.automarket.paladin.core.common;

/**
 * @Author Luogh
 * @Date 2020/12/7
 **/
public class JTuple2<K, V> {
    private final K k;
    private final V v;

    public JTuple2(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public K getK() {
        return k;
    }

    public V getV() {
        return v;
    }
}
