package com.nanjiabawa.concurrent.pool;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 知识点：企业实战 —— 给"批量数据同步"配一个生产级线程池
 *
 * 场景（原型是 zsk 项目）：标注完成后，把 8 个数据集同步到 Neo4j。
 * 串行 8×200ms=1.6s；并行约 400ms。
 *
 * 生产级线程池的 5 个必做步骤：
 *   1. 参数有依据（IO 密集型 → 线程数 ≈ 2×CPU核数）
 *   2. 自定义 ThreadFactory 命名线程
 *   3. CountDownLatch 收口
 *   4. await 带超时，防任务卡死拖死主流程
 *   5. 优雅关闭三段式：shutdown → awaitTermination → shutdownNow
 */
public class Neo4jSyncPoolDemo {

    /** 模拟同步单个数据集到 Neo4j：约 200ms 的 IO 操作 */
    private static void syncDataset(String datasetId) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("[" + Thread.currentThread().getName() + "] 已同步 " + datasetId);
    }

    public static void main(String[] args) throws InterruptedException {
        // 步骤1：IO 密集型（等网络为主）→ 线程数 2×Ncpu；CPU 密集型才是 N+1。两类任务分池！
        int ncpu = Runtime.getRuntime().availableProcessors();
        int coreSize = ncpu * 2;
        System.out.println("本机 CPU 核数 = " + ncpu + "，IO 密集型 → 核心线程数 = " + coreSize);

        AtomicInteger threadNo = new AtomicInteger(1);
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                coreSize, coreSize,                       // 后台同步任务峰谷平稳：核心=最大
                60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),           // 有界队列！超了就拒绝+告警
                r -> {                                    // 步骤2：命名，排查时 grep 线程名即可
                    return new Thread(r, "neo4j-sync-worker-" + threadNo.getAndIncrement());
                },
                new ThreadPoolExecutor.AbortPolicy()      // 队列满：抛异常让调用方感知，绝不静默丢
        );

        List<String> datasets = List.of("ds-1", "ds-2", "ds-3", "ds-4", "ds-5", "ds-6", "ds-7", "ds-8");

        // ---- 串行版做对比 ----
        long start = System.nanoTime();
        for (String ds : datasets) {
            syncDataset(ds);
        }
        System.out.printf("串行同步 8 个数据集：%d ms%n", (System.nanoTime() - start) / 1_000_000);

        // ---- 步骤3+4：并行同步，CountDownLatch 收口 + 超时保护 ----
        start = System.nanoTime();
        CountDownLatch latch = new CountDownLatch(datasets.size());
        AtomicInteger failed = new AtomicInteger(0);
        for (String ds : datasets) {
            try {
                pool.execute(() -> {
                    try {
                        syncDataset(ds);
                    } catch (Exception e) {
                        failed.incrementAndGet();   // 任务内必须自己兜异常（见文末）
                    } finally {
                        latch.countDown();          // 放 finally！否则异常时 await 死等
                    }
                });
            } catch (Exception e) {                 // execute 被拒绝（AbortPolicy）也要收口
                failed.incrementAndGet();
                latch.countDown();
            }
        }
        boolean allDone = latch.await(5, TimeUnit.SECONDS);   // 步骤4：卡死最多等 5 秒
        System.out.printf("并行同步 8 个数据集：%d ms（全部完成=%s，失败=%d）%n",
                (System.nanoTime() - start) / 1_000_000, allDone, failed.get());

        // ---- 步骤5：优雅关闭三段式 ----
        pool.shutdown();
        if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
            pool.shutdownNow();
            System.out.println("强制中断！有任务超过 5 秒未完成");
        }
    }
}
