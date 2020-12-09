package com.xiaoju.automarket.paladin.core.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author Luogh
 * @Date 2020/12/7
 **/
@AllArgsConstructor
@Getter
public class JTuple2<K, V> {
    private final K k;
    private final V v;
}
