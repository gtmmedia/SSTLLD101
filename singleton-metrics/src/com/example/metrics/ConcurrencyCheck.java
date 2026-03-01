package com.example.metrics;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Spawns many threads racing on getInstance().
 * Starter is expected to sometimes create >1 instance. After fix, must always be 1.
 */
public class ConcurrencyCheck {

    public static void main(String[] args) throws Exception {
        int threads = 80;
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        Set<Integer> identities = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    MetricsRegistry r = MetricsRegistry.getInstance();
                    identities.add(System.identityHashCode(r));
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await(2, TimeUnit.SECONDS);
        start.countDown();
        done.await(3, TimeUnit.SECONDS);
        pool.shutdownNow();

        System.out.println("Unique instances seen: " + identities.size());
        System.out.println("Identities: " + identities);
    }
}
