package com.nanjiabawa.concurrent.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 知识点：ConcurrentHashMap —— 线程安全的本地缓存 + 防缓存击穿
 *
 * 企业场景：热点数据（如数据集配置、用户权限）每次都查 DB 太浪费。
 * 本地缓存是最轻量的方案：进程内一个 ConcurrentHashMap 搞定，微秒级访问。
 *
 * 两个企业级考点都在这个 Demo 里：
 *   1. 为什么不用 HashMap？—— HashMap 多线程下会丢数据（JDK7 扩容甚至链表成环死循环），
 *      Hashtable 全表锁性能太差；CHM 用 CAS + synchronized 锁单个桶，并发度极高。
 *   2. 缓存击穿：某个热点 key 过期瞬间，100 个请求同时发现"缓存没有"，
 *      全部打到 DB —— computeIfAbsent 保证"同一个 key 只有一个线程执行加载函数"。
 */
public class LocalCacheDemo {

    /** "数据库"查询计数器：验证击穿防护有没有生效 */
    private static int dbQueryCount = 0;

    /** 本地缓存：key=数据集ID, value=配置JSON。生产环境用 Caffeine（带过期/容量上限），原理相同 */
    private static final ConcurrentHashMap<String, String> CACHE = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        // ============ 演示：10 个线程并发请求同一个 key（模拟热点过期瞬间）============
        System.out.println("----- 10 个线程同时请求 dataset:1001 -----");
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> getConfig("dataset:1001"), "req-" + i);
            threads[i].start();
            Thread.sleep(5);   // 错开一点点启动，让"第一个加载、其余等待"更清晰
        }
        for (Thread t : threads) {
            t.join();
        }
        System.out.println("DB 实际被查次数 = " + dbQueryCount + "（理想值 1 —— 这就是防击穿）");

        // ============ 对比：错误写法（get 后再 put，两步分离，必有并发窗口）============
        // String v = CACHE.get(key);          // 100 个线程同时走到这，全是 null
        // if (v == null) {                    // 100 个线程全部判定"缓存没有"
        //     v = queryDb(key);               // DB 被打死 —— 这就是击穿
        //     CACHE.put(key, v);              // 后写的覆盖先写的
        // }
        // computeIfAbsent 把"检查-加载-放入"合并成一个原子操作，从根上消灭这个窗口。
    }

    /** 带防击穿的缓存读取：computeIfAbsent 的加载函数对同一 key 只会执行一次 */
    private static String getConfig(String key) {
        return CACHE.computeIfAbsent(key, k -> {
            // 只有"缓存未命中且第一个到达"的线程会进这里（对同一 key 而言）
            System.out.println(Thread.currentThread().getName() + " 缓存未命中，回源查询 DB...");
            dbQueryCount++;
            try {
                Thread.sleep(200);   // 模拟 DB 查询耗时：没有防护的话，10 个线程都查询一遍
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println(Thread.currentThread().getName() + " 加载完成，写入缓存");
            return "{\"datasetId\":1001,\"graphType\":\"NER\",\"status\":\"ACTIVE\"}";
        });
        // 企业注解：
        // 1. computeIfAbsent 的加载函数里不要做重活/长阻塞 —— CHM 对该桶的锁会被长时间持有，
        //    同桶其他 key 的操作也会被卡住（这是它和"手动 double-check"的真实差异）
        // 2. 真实项目本地缓存三件套：过期时间 + 容量上限 + 淘汰策略，直接用 Caffeine
        // 3. 缓存三大经典问题：击穿（热点key过期瞬间）、穿透（查不存在的key）、雪崩（大量key同时过期）
        //    击穿→computeIfAbsent/逻辑过期；穿透→缓存空值/布隆过滤器；雪崩→过期时间加随机抖动
    }
}
