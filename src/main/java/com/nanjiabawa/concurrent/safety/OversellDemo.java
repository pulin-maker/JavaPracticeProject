package com.nanjiabawa.concurrent.safety;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

/**
 * 知识点：线程安全的"事故现场"与三种修复方案 —— 企业经典"超卖"问题的简化模型
 *
 * 场景：计数器初始 0，10 个线程并发各加 10000 次，理论结果 100000。
 * 跑四次对比：不加锁 / volatile / synchronized / AtomicInteger —— 差距就是线上事故本身。
 *
 * 为什么 count++ 不安全？它不是一条指令，是三步：
 *   1. 读：  把 count 从主内存读到线程工作内存（寄存器/栈）
 *   2. 加：  在工作内存里 +1
 *   3. 写：  把结果写回主内存
 * 十个线程在这三步之间随意交错，就会"互相覆盖"——典型丢更新（Lost Update）。
 * 这是所有"超卖/重复扣款/库存为负"类线上事故的底层原理。
 */
public class OversellDemo {

    private static final int THREADS = 10;
    private static final int LOOP = 10000;

    private static int unsafeCount = 0;                            // 方案0：裸奔
    private static volatile int volatileCount = 0;                 // 方案1：只加 volatile
    private static int syncCount = 0;                              // 方案2：synchronized
    private static final AtomicInteger atomicCount = new AtomicInteger(0); // 方案3：CAS 原子类

    public static void main(String[] args) throws Exception {
        runTest("方案0：什么都不加    ", () -> unsafeCount++, () -> unsafeCount);
        runTest("方案1：volatile      ", () -> volatileCount++, () -> volatileCount);
        runTest("方案2：synchronized  ", OversellDemo::syncIncrement, () -> syncCount);
        runTest("方案3：AtomicInteger ", () -> atomicCount.incrementAndGet(), atomicCount::get);
    }

    /** 起 10 个线程并发执行自增，等待全部结束后打印最终值 */
    private static void runTest(String label, Runnable action, IntSupplier reader) throws InterruptedException {
        // 每轮测试前归零
        unsafeCount = 0; volatileCount = 0; syncCount = 0; atomicCount.set(0);

        // CountDownLatch：线程协作工具（阶段3详解）—— 这里先当"终点线"用
        CountDownLatch latch = new CountDownLatch(THREADS);
        for (int i = 0; i < THREADS; i++) {
            new Thread(() -> {
                for (int j = 0; j < LOOP; j++) {
                    action.run();
                }
                latch.countDown();      // 一个线程跑完，计数 -1
            }, "test-worker-" + i).start();
        }
        latch.await();                  // main 阻塞到计数归零（10个线程全部跑完）才开始读结果
        int result = reader.getAsInt();
        System.out.printf("%s 最终 = %d %s%n", label, result,
                result == THREADS * LOOP ? "✓ 正确" : "✗ 丢更新了！");
    }

    /** 方案2 的自增：整个"读-加-写"三步被锁成一个原子操作（方法锁 = 锁 OversellDemo.class） */
    private static synchronized void syncIncrement() {
        syncCount++;
    }
}
