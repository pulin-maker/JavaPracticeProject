package com.nanjiabawa.concurrent.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 知识点：为什么阿里规约禁止直接用 Executors 工厂方法 —— 复现两个"定时炸弹"
 *
 * 《Java开发手册》原文：线程池不允许使用 Executors 去创建，
 * 而是通过 ThreadPoolExecutor 的方式，明确指定核心/最大线程数与有界队列。
 * 原因是 Executors 的几个工厂方法各埋了一颗雷，这个 Demo 复现其中两颗：
 *
 *   雷1：newFixedThreadPool / newSingleThreadExecutor
 *        → 队列是无界的 LinkedBlockingQueue（容量 Integer.MAX_VALUE）
 *        → 下游一慢，任务无限堆积 → 内存爆掉（OOM）
 *   雷2：newCachedThreadPool
 *        → 最大线程数是 Integer.MAX_VALUE，队列是"零容量" SynchronousQueue
 *        → 一波流量进来疯狂创建线程 → 线程数爆炸 → OOM / 频繁 Full GC
 *
 * 本 Demo 用小规模数据演示"堆积"和"线程爆炸"的现象；真 OOM 需要大流量，别在家试。
 */
public class ExecutorsTrapDemo {

    public static void main(String[] args) throws InterruptedException {
        trapOneUnboundedQueue();
        System.out.println();
        trapTwoThreadExplosion();
    }

    /** 雷1：FixedThreadPool 的无界队列 —— 任务堆积，内存持续上涨 */
    private static void trapOneUnboundedQueue() throws InterruptedException {
        System.out.println("===== 雷1：newFixedThreadPool 的无界队列 =====");
        // 源码等价于：new ThreadPoolExecutor(n, n, 0, MS, new LinkedBlockingQueue<>()) ← 队列容量是 int 最大值！
        ExecutorService fixed = Executors.newFixedThreadPool(2);

        // 模拟下游变慢：每个任务要 100ms（比如 Neo4j 同步突然变慢）
        for (int i = 0; i < 5000; i++) {
            fixed.execute(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        // 提交完立刻看：2 个线程在干活，剩下 4998 个全堆在无界队列里
        // 关键洞察：maxPoolSize == corePoolSize == 2，队列永远不会满，"扩容临时工"的分支永远走不到！
        // 线程池的"自我保护"（扩线程→拒绝）整个被无界队列废掉了
        System.out.printf("提交 5000 个任务后：线程数=%d，队列堆积=%d%n",
                ((ThreadPoolExecutor) fixed).getPoolSize(),
                ((ThreadPoolExecutor) fixed).getQueue().size());
        System.out.println("→ 真实生产：堆积的就是一万个 Runnable 对象，GC 压力飙升，最终 OOM");
        fixed.shutdown();
        fixed.awaitTermination(2, TimeUnit.MINUTES);
        System.out.println("处理完堆积远超预期（4998 个任务排队 = 用户请求全部超时）");
    }

    /** 雷2：CachedThreadPool 的无限线程 —— 一波流量创建一波线程 */
    private static void trapTwoThreadExplosion() throws InterruptedException {
        System.out.println("===== 雷2：newCachedThreadPool 的线程爆炸 =====");
        // 源码等价于：new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60s, new SynchronousQueue<>())
        // SynchronousQueue 零容量：任务永远不入队，必须立刻有线程接 → 接不住就无限建线程
        ExecutorService cached = Executors.newCachedThreadPool();

        AtomicInteger peak = new AtomicInteger(0);
        AtomicInteger alive = new AtomicInteger(0);
        for (int i = 0; i < 200; i++) {
            cached.execute(() -> {
                alive.incrementAndGet();
                peak.accumulateAndGet(alive.get(), Math::max);
                try {
                    Thread.sleep(1000);   // 模拟 IO 任务
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                alive.decrementAndGet();
            });
        }
        Thread.sleep(200);
        System.out.printf("提交 200 个任务后：当前存活线程=%d，峰值=%d%n", alive.get(), peak.get());
        System.out.println("→ 200 个线程 ≈ 200MB 栈内存 + 疯狂的上下文切换；QPS 再高一层就是 OOM");
        System.out.println("→ 真实生产：一次 MQ 积压消费、一次定时任务并发触发，就能把它引爆");
        cached.shutdown();
        cached.awaitTermination(2, TimeUnit.MINUTES);
        // 正确姿势：无论什么场景，都 new ThreadPoolExecutor(...) 手写七参数 —— 参数即文档，
        // 看到 core/max/队列容量/拒绝策略，就知道这个池子的"脾气"，出问题也拦得住
    }
}
