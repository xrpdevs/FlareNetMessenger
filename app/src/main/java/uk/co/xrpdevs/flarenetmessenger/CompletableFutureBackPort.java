package uk.co.xrpdevs.flarenetmessenger;

import android.content.Context;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import java.util.concurrent.CompletableFuture;
import java9.util.concurrent.CompletableFuture;

public class CompletableFutureBackPort extends org.web3j.utils.Async {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    static {
        run();
    }
    public static <T> java9.util.concurrent.CompletableFuture<T> runCompat(Callable<T> callable) {
        CompletableFuture<T> result = null;

            result = new java9.util.concurrent.CompletableFuture<>();
        CompletableFuture<T> finalResult = result;
        java9.util.concurrent.CompletableFuture.runAsync(
                () -> {
                    // we need to explicitly catch any exceptions,
                    // otherwise they will be silently discarded
                    try {
                        finalResult.complete(callable.call());
                    } catch (Throwable e) {
                        finalResult.completeExceptionally(e);
                    }
                },
                executor);
        return result;
    }

    private static void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(CompletableFutureBackPort::run));
    }
}
