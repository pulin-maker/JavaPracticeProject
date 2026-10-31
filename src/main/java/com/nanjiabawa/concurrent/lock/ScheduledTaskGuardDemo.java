package com.nanjiabawa.concurrent.lock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 知识点：ReentrantLock.tryLock —— 定时任务防重入（企业日常痛点）
 *
 * 企业场景：@Scheduled(fixedRate = 1000) 每 1 秒触发一次数据同步任务，
 * 但任务本身可能跑 3 秒。Spring 默认会怎么干？
 *   → 单线程调度器：下一次触发排队等待（任务堆积，节奏错乱）
 *   → 配了线程池：多个实例同时跑同一个任务（数据错乱、重复同步）
 * 两种都不对，正确姿势：任务开头 tryLock，抢不到锁说明"上一轮还没跑完"，直接跳过。
 *
 * 为什么不用 synchronized？
 *   synchronized 是"排队等"——上一轮没跑完，这一轮的触发会阻塞等锁，
 *   等到了又执行一遍，任务越积越多，等于变相堆积。tryLock 是"试一下，不行就算了"。
 *
 * 注意边界：tryLock 只防单机重入。分布式部署（多台机器跑同一个定时任务）要用
 * 分布式锁（Redis setnx / 数据库锁）或调度平台（XXL-JOB 的路由策略）。
 */
public class ScheduledTaskGuardDemo {

    private static final ReentrantLock GUARD = new ReentrantLock();

    public static void main(String[] args) throws InterruptedException {
        // 模拟调度器：每 100ms 触发一次任务（类比 @Scheduled(fixedRate=1000)）
        for (int tick = 1; tick <= 10; tick++) {
            final int currentTick = tick;
            new Thread(() -> tryRunSyncTask(currentTick), "trigger-" + tick).start();
            Thread.sleep(100);
        }
        Thread.sleep(2500);   // 等最后一轮跑完
        System.out.println("===== 观察：10 次触发，真正执行的只有 2 次，其余都'跳过'了 =====");
    }

    /** 模拟一个耗时 500ms 的同步任务（> 触发间隔 100ms，必然重入） */
    private static void tryRunSyncTask(int tick) {
        // tryLock：立刻返回 true/false，不等待 —— 与 lock() 的本质区别
        if (!GUARD.tryLock()) {
            System.out.println("[触发" + tick + "] 上一轮还在跑，本轮跳过（没有堆积！）");
            return;
        }
        try {
            System.out.println("[触发" + tick + "] 开始执行同步任务...（约500ms）");
            Thread.sleep(500);   // 模拟：查数据集 → 同步 Neo4j → 更新状态
            System.out.println("[触发" + tick + "] 同步完成");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            GUARD.unlock();      // 必须在 finally 里解锁！忘了 = 死锁（面试必问）
        }
        // 对比题：lock() vs tryLock() vs tryLock(超时) vs lockInterruptibly()
        //   lock()               死等（拿不到就一直阻塞）
        //   tryLock()            立刻试，失败返回 false
        //   tryLock(3, SECONDS)  限时等待，超时放弃 —— 企业最常用，防无限阻塞
        //   lockInterruptibly()  等待中可被中断（synchronized 等锁时死活不可中断，这是大劣势）
    }
}
