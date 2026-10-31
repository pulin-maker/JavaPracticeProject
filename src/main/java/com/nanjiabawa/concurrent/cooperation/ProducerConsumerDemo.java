package com.nanjiabawa.concurrent.cooperation;

/**
 * 知识点：生产者-消费者模式（wait/notify）—— 手写一个有界缓冲区
 *
 * 企业场景：这是所有消息队列（Kafka/RocketMQ/RabbitMQ）的思想原型。
 *   为什么日志框架要"异步落库"？为什么下单高峰要"先进队列慢慢消费"？
 *   都是同一个答案：生产与消费速率不匹配时，用缓冲区削峰 + 解耦。
 *
 * 面试必考：手写生产者-消费者。三个得分点：
 *   1. while 判断（不是 if）—— 防止"虚假唤醒"，醒来后必须重新检查条件
 *   2. 判断条件不满足时 wait()（会释放锁！），被唤醒后从 wait 处继续
 *   3. 修改状态后 notifyAll()（而不是 notify），避免唤醒同类线程造成"全体躺平"死锁
 */
public class ProducerConsumerDemo {

    /** 有界缓冲区：容量 5，满了生产者等，空了消费者等 */
    static class BoundedBuffer {
        private final int[] items = new int[5];
        private int putIdx = 0, takeIdx = 0, count = 0;   // count 是共享状态，读写都要在锁内

        public void put(int value) throws InterruptedException {
            synchronized (this) {
                // 必须用 while：被唤醒后条件可能又被别人抢了，要重新检查（虚假唤醒防线）
                while (count == items.length) {
                    System.out.println(Thread.currentThread().getName() + " 缓冲区已满，生产者等待");
                    wait();             // 释放锁并休眠；被唤醒后从这里继续，重新走 while 判断
                }
                items[putIdx] = value;
                putIdx = (putIdx + 1) % items.length;   // 环形数组：JDK 里 ArrayBlockingQueue 同款
                count++;
                System.out.println(Thread.currentThread().getName() + " 生产 -> " + value + "（当前 " + count + " 个）");
                notifyAll();            // 生产了，唤醒可能在等待的消费者
            }
        }

        public int take() throws InterruptedException {
            synchronized (this) {
                while (count == 0) {
                    System.out.println(Thread.currentThread().getName() + " 缓冲区已空，消费者等待");
                    wait();
                }
                int value = items[takeIdx];
                takeIdx = (takeIdx + 1) % items.length;
                count--;
                System.out.println(Thread.currentThread().getName() + " 消费 <- " + value + "（当前 " + count + " 个）");
                notifyAll();            // 消费了，唤醒可能在等待的生产者
                return value;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        BoundedBuffer buffer = new BoundedBuffer();

        // 2 个生产者各产 5 件，3 个消费者各消费 3 件余 1（总数守恒：10 = 10）
        for (int i = 1; i <= 2; i++) {
            final int producerId = i;
            new Thread(() -> {
                for (int j = 1; j <= 5; j++) {
                    try {
                        buffer.put(producerId * 100 + j);
                        Thread.sleep(10);   // 放慢节奏，让"等待/唤醒"肉眼可见
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "producer-" + i).start();
        }
        for (int i = 1; i <= 3; i++) {
            final int consumerId = i;
            new Thread(() -> {
                for (int j = 0; j < 3; j++) {
                    try {
                        buffer.take();
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "consumer-" + i).start();
        }
        // main 不等待直接退出也行（demo 线程都是非守护），这里 sleep 稳定输出
        Thread.sleep(2000);
        System.out.println("===== 演示结束（看输出：count 永远不会越界，消费者从不读到空值）=====");
        // 企业注解：生产中不要手写这个 —— 直接用 java.util.concurrent.ArrayBlockingQueue
        // （put/take 自动阻塞）或 LinkedBlockingQueue。手写是面试需求，不是工程需求。
        // 但 MQ 的"缓冲削峰 + 解耦 + 异步"三大价值，你已经亲手实现了。
    }
}
