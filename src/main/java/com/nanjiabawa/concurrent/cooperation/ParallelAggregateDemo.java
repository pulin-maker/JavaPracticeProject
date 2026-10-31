package com.nanjiabawa.concurrent.cooperation;

import java.util.concurrent.CountDownLatch;

/**
 * 知识点：CountDownLatch —— "等人齐再开饭"的并行聚合
 *
 * 企业场景：电商首页/详情页聚合。页面需要的数据来自 3 个下游服务：
 *   用户信息(100ms) + 订单摘要(150ms) + 营销活动(80ms)
 * 串行调用：100+150+80 = 330ms（接口超时红线通常是 1s，稍加几个下游就爆）
 * 并行调用：耗时 = 最慢的那个 ≈ 150ms —— 一半以上的耗时被"白捡"回来。
 *
 * 真实大厂接口聚合就是这么干的（Spring Cloud 里叫"并行编排"，
 * 更现代的写法是 CompletableFuture，阶段5讲，但 CountDownLatch 是它的地基）。
 *
 * CountDownLatch 三件套：
 *   new CountDownLatch(n)  建立一个计数器
 *   latch.countDown()      每个干完活的线程把计数 -1
 *   latch.await()          主线程阻塞到计数归零（人齐开饭）
 * 特点：一次性（计数不能重置）、不可复用（要复用用 CyclicBarrier，面试对比题）
 */
public class ParallelAggregateDemo {

    /** 模拟调用下游服务：睡眠指定毫秒，返回一段"数据" */
    private static String callRemote(String serviceName, long costMs) {
        try {
            Thread.sleep(costMs);   // 模拟网络 IO（真实的耗时大头，也是"值得并行"的原因）
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return serviceName + "的数据";
    }

    public static void main(String[] args) throws InterruptedException {
        // ============ 方案A：串行调用（大多数新手代码的样子）============
        long start = System.nanoTime();
        String user = callRemote("用户服务", 100);
        String order = callRemote("订单服务", 150);
        String promo = callRemote("营销服务", 80);
        System.out.printf("方案A 串行聚合 耗时 %d ms，结果：%s%n",
                (System.nanoTime() - start) / 1_000_000, String.join(" | ", user, order, promo));

        // ============ 方案B：并行调用 + CountDownLatch ============
        start = System.nanoTime();
        CountDownLatch latch = new CountDownLatch(3);   // 3 个下游 = 计数器 3
        String[] results = new String[3];               // 每个线程写自己的槽位，无共享写冲突

        new Thread(() -> {
            results[0] = callRemote("用户服务", 100);
            latch.countDown();
        }, "agg-user").start();

        new Thread(() -> {
            results[1] = callRemote("订单服务", 150);
            latch.countDown();
        }, "agg-order").start();

        new Thread(() -> {
            results[2] = callRemote("营销服务", 80);
            latch.countDown();
        }, "agg-promo").start();

        latch.await();   // 主线程在这"等人"：3 个 countDown 全部到齐才往下走
        System.out.printf("方案B 并行聚合 耗时 %d ms，结果：%s%n",
                (System.nanoTime() - start) / 1_000_000, String.join(" | ", results));

        // ============ 企业思考题（答案见下方注释）============
        // 1. 某个下游超时/挂了，latch.await() 会永远等下去吗？
        //    → 会！生产必须用 latch.await(300, TimeUnit.MILLISECONDS) 带超时版本，
        //      超时后降级处理（该板块显示"加载失败"），否则一次下游抖动拖死整个首页。
        // 2. 聚合场景该开几个线程？
        //    → 这里 new 了 3 个裸线程只是演示。生产要用共享线程池（阶段4），
        //      否则每次请求都创建线程，QPS 一高线程数爆炸。
        // 3. 写 results[i] 需要 volatile 吗？
        //    → 不需要：latch.countDown() 与 latch.await() 之间自带 happens-before 保证，
        //      await 返回后能看到所有线程 countDown 前的写入 —— 这是 JUC 工具的隐藏福利。
    }
}
