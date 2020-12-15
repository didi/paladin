package com.xiaoju.automarket.paladin.core.util;

import akka.dispatch.OnComplete;
import scala.concurrent.Future;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @Author Luogh
 * @Date 2020/12/15
 **/
public class FutureUtil {

    public static <T, U extends T> CompletableFuture<T> toJava(Future<U> scalaFuture) {
        final CompletableFuture<T> result = new CompletableFuture<>();

        scalaFuture.onComplete(new OnComplete<U>() {
            @Override
            public void onComplete(Throwable failure, U success) {
                if (failure != null) {
                    result.completeExceptionally(failure);
                } else {
                    result.complete(success);
                }
            }
        }, Executors.directExecutionContext());

        return result;
    }

    public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[futures.size()]))
                .thenApply(ignored -> futures.stream().map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }

}
