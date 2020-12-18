package com.xiaoju.automarket.paladin.core.util;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @Author Luogh
 * @Date 2020/12/15
 **/
public class FutureUtil {

    public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[futures.size()]))
                .thenApply(ignored -> futures.stream().map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }

}
