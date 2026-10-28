package com.nanjiabawa.oop.abstractclass;

import java.util.List;

/**
 * 知识点：抽象类（Abstract Class）
 *
 * 心智模型：抽象类 = 半成品
 *   - 做好的部分：字段、构造器、具体方法 -> 子类直接继承白拿
 *   - 没做完的部分：抽象方法 -> 子类必须补完，否则编译不过
 *   - 不能 new：半成品没有资格被直接使用
 *
 * 抽象类 vs 普通父类：普通父类能 new，抽象类把"不完整的类"挡在门外
 * 抽象类 vs 接口：抽象类"是什么"（有状态、有构造器、有实现），接口"能做什么"（学接口时细讲）
 */
public class AbstractClassDemo {

    public static void main(String[] args) {
        // 1. 抽象类不能实例化（解开注释会编译报错）
        // AbstractLogger logger = new AbstractLogger("demo");

        // 2. 三种日志器：格式化逻辑是同一份（父类的 format），落盘方式各不同（子类的 write）
        List<AbstractLogger> loggers = List.of(
                new ConsoleLogger("order-service"),
                new FileLogger("order-service"),
                new DatabaseLogger("order-service")
        );

        System.out.println("========== 三种日志器打同一条日志 ==========");
        for (AbstractLogger logger : loggers) {
            logger.log("ERROR", "下单失败：库存不足，sku=1001");
        }

        System.out.println("========== 换个级别再打一条 ==========");
        loggers.get(0).log("INFO", "订单 ORD001 支付成功");

        // 3. 观察点：log() 是父类的 final 方法，三个子类走的都是同一个流程
        //    差异只发生在流程中间的 write() 那一步 —— 这就是抽象类的价值：
        //    "公共的部分写一次，变化的部分各做各的，且变化的部分必须被实现"
    }
}