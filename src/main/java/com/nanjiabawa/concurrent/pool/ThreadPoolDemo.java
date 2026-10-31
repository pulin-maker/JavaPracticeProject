package com.nanjiabawa.concurrent.pool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 知识点：ThreadPoolExecutor 七参数 —— 逐个用运行结果"演"给你看
 *
 * 面试必背：提交一个任务后，线程池的决策流程（顺序反直觉，务必记住）：
 *   1. 线程数 < corePoolSize？       → 新建核心线程执行（哪怕别的核心线程闲着）
 *   2. 队列没满？                    → 任务入队等待（不新建线程！）
 *   3. 线程数 < maximumPoolSize？    → 新建"临时工"线程执行
 *   4. 都不行                        → 触发拒绝策略
 * 注意第 2、3 步的顺序：队列先吃满，才会扩线程 —— 和很多人直觉的"先扩线程再排队"相反！
 */
public class ThreadPoolDemo {

    public static void main(String[] args) throws InterruptedException {
        AtomicInteger threadNo = new AtomicInteger(1);

        // ============ 七参数逐个注释（这是面试的核心背诵材料）============
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                2,                                    // corePoolSize     核心线程数：常驻不回收（默认）
                4,                                    // maximumPoolSize  最大线程数：核心+临时工的上限
                60, TimeUnit.SECONDS,                 // keepAliveTime    临时工空闲多久被回收
                new ArrayBlockingQueue<>(5),          // workQueue        有界队列：容量 5（必须有界！）
                r -> {                                // threadFactory    线程工厂：必须自定义命名（企业铁律）
                    Thread t = new Thread(r, "demo-worker-" + threadNo.getAndIncrement());
                    t.setDaemon(false);
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy()  // handler          拒绝策略：抛异常（默认）
        );

        // ============ 实验：连续提交 12 个任务（每个耗时 1 秒），观察走向 ============
        // 预测（先自己推一遍再看运行结果！）：
        //   任务1-2  → 建 2 个核心线程
        //   任务3-7  → 入队（队列容量 5）
        //   任务8-9  → 队列满了！建 2 个临时工（总线程数到 max=4）
        //   任务10-12 → 线程满 + 队列满 → AbortPolicy 抛 RejectedExecutionException
        AtomicInteger rejected = new AtomicInteger(0);
        for (int i = 1; i <= 12; i++) {
            final int taskId = i;
            try {
                pool.execute(() -> {
                    System.out.printf("任务%d 开始 [%s]%n", taskId, Thread.currentThread().getName());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.printf("任务%d 完成 [%s]%n", taskId, Thread.currentThread().getName());
                });
            } catch (RejectedExecutionException e) {
                rejected.incrementAndGet();
                System.out.printf("任务%d 被拒绝！（AbortPolicy 抛异常，调用方要自己兜底）%n", taskId);
            }
            // 边提交边观察：线程数、队列积压、已完成数 —— 三个数字就是线程池的"心电图"
            System.out.printf("  提交任务%d后 [线程数=%d, 队列=%d, 已完成=%d]%n",
                    taskId, pool.getPoolSize(), pool.getQueue().size(), pool.getCompletedTaskCount());
        }

        // ============ 优雅关闭：先 shutdown（不再收新任务），再等存量任务跑完 ============
        pool.shutdown();                            // 温柔关：已提交的任务全部执行完
        if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
            pool.shutdownNow();                     // 超时还没完 → 强制中断（对运行中任务发 interrupt）
        }
        System.out.printf("%n拒绝数=%d，最终完成=%d%n", rejected.get(), pool.getCompletedTaskCount());
        // 四种拒绝策略（面试必问，企业里改得最多的是最后一个）：
        //   AbortPolicy      抛异常（默认）——调用方能感知，但要求调用方处理
        //   CallerRunsPolicy 谁提交谁执行 —— 天然"反压"：提交线程被占用，提交速度自然降下来
        //   DiscardPolicy    静默丢弃 —— 最危险！任务无声消失，线上事故制造机
        //   DiscardOldest    丢最老的 —— 适合"只关心最新值"的场景（如行情推送）
    }
}
