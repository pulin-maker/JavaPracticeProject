package com.nanjiabawa.concurrent.basics;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * 知识点：线程的三种创建方式 + 生命周期 + 企业必须知道的线程常识
 *
 * 面试高频：「创建线程有几种方式？」
 *   标准答案：本质上只有一种 —— new Thread().start()，
 *   所谓三种只是"任务从哪来"的三种形式：继承 Thread / Runnable / Callable。
 *   企业答案：都不是 —— 生产环境永远用线程池提交任务（阶段4细讲），
 *   直接 new Thread 是阿里规约明确禁止的（无法复用、无法管控数量）。
 */
public class ThreadBasicsDemo {

    public static void main(String[] args) throws Exception {
        // ============ 方式一：继承 Thread（不推荐，Java 单继承，类就被占用了）============
        Thread t1 = new Thread() {
            @Override
            public void run() {
                System.out.println("方式一(继承Thread)：" + currentThread().getName());
            }
        };
        t1.setName("worker-inherit");   // 企业习惯：线程必须命名！排查问题时 jstack 里看到的全是
                                        // Thread-0、Thread-1 会被祭天的，名字要能看出"谁在干什么"

        // ============ 方式二：Runnable（推荐，任务与线程解耦）============
        // 为什么推荐：任务本身是 Runnable，不绑定具体线程 —— 这正是线程池能复用线程的前提：
        // 线程池 = 一批活着的线程 + 一个任务队列，worker 线程不断从队列里捞 Runnable 执行
        Runnable task = () -> System.out.println("方式二(Runnable)：" + Thread.currentThread().getName());
        Thread t2 = new Thread(task, "worker-runnable");

        // ============ 方式三：Callable + FutureTask（任务有返回值、能抛异常）============
        // Runnable 的 run() 是 void 且不能抛受检异常 —— 要结果就得用 Callable
        Callable<String> callable = () -> {
            Thread.sleep(100);   // 模拟耗时任务（如调用下游接口）
            return "方式三(Callable)的结果：" + Thread.currentThread().getName();
        };
        FutureTask<String> future = new FutureTask<>(callable);
        new Thread(future, "worker-callable").start();
        // future.get() 会阻塞当前线程直到任务完成 —— 这是学习 Future/CompletableFuture（阶段5）的种子

        t1.start();   // start()：向操作系统申请一条新线程去执行 run()
        t2.start();   // 注意调 start 不要调 run！面试高频：t.run() 只是普通方法调用，
                      // 代码会跑在 main 线程里，根本没创建新线程
        System.out.println("方式三(Callable)拿到结果：" + future.get());

        // ============ 线程的 6 种状态（面试必背，画成状态机背）============
        System.out.println("\n----- 线程状态 -----");
        Thread t3 = new Thread(() -> { });
        System.out.println("新建后          ：" + t3.getState());   // NEW
        t3.start();
        System.out.println("start 后        ：" + t3.getState());   // RUNNABLE（含就绪+运行中）
        t3.join();                                                    // main 等 t3 死亡
        System.out.println("run 完后        ：" + t3.getState());   // TERMINATED
        // 其余三种：BLOCKED（等锁）、WAITING（无限等：wait/join 不带超时）、
        //           TIMED_WAITING（限时等：sleep(n)/wait(n)）—— OversellDemo 会现场制造

        // ============ 守护线程（daemon）：给"后台默默服务"的线程用的 ============
        Thread heartBeat = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    System.out.println("[守护线程] 心跳上报...");   // 你几乎看不到它打印
                } catch (InterruptedException ignored) { }
            }
        }, "heartbeat-reporter");
        heartBeat.setDaemon(true);   // 必须在 start() 之前设置
        heartBeat.start();
        // 守护线程的意义：JVM 退出时不管它死活。GC 线程就是守护线程。
        // 企业场景：监控上报、链路追踪的后台刷新，都是守护线程 —— 主业务挂了它们没资格苟活
        Thread.sleep(100);   // 给心跳一点表演时间，然后 main 结束 → JVM 退出 → 守护线程被掐

        // ============ 企业常识小结（面试聊到线程必说的三句话）============
        // 1. Tomcat 处理请求 = 每个请求分派一条工作线程执行你的 Controller 方法（默认200条）
        //    所以你的 Service 天然就是"多线程环境"，线程安全不是可选项
        // 2. 线程是 OS 资源，创建/销毁/切换都有成本（默认1MB栈内存+内核开销）
        //    -> 所以要有线程池（阶段4）
        // 3. main 线程死了 JVM 不一定退出：看还有没有"非守护线程"活着
    }
}
