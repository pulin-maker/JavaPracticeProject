package com.nanjiabawa.oop.inheritance;

import java.math.BigDecimal;
import java.util.List;

/**
 * 知识点：继承（Inheritance）
 *
 * 核心概念：
 *   - extends：子类继承父类，获得父类的字段和方法（代码复用）
 *   - super(...)：子类构造器第一条语句调用父类构造器
 *   - super.方法(...)：在重写的方法里复用父类实现
 *   - @Override + 方法重写：子类替换/扩展父类行为（多态的基础）
 *   - abstract：抽象类不能实例化，抽象方法强制子类实现
 *   - final 方法：禁止子类重写（保护核心流程）
 *
 * 企业级套路 —— 模板方法模式：
 *   父类定流程骨架（不变），子类填具体步骤（可变）。
 *   新增一个支付渠道时，只需写一个新子类，主流程代码一行不动 —— 这就是"对扩展开放，对修改关闭"。
 */
public class InheritanceDemo {

    public static void main(String[] args) {
        // 1. 抽象类不能 new：PaymentChannel c = new PaymentChannel(...) 编译报错
        //    只能 new 它的子类

        // 2. 面向父类编程：变量声明为父类类型，实际指向子类对象
        List<PaymentChannel> channels = List.of(
                new AliPayChannel("M10001"),
                new WeChatPayChannel("M10001"),
                new UnionPayChannel("M10001"),
                new JdPayChannel("M10001")
        );

        PaymentRequest request = new PaymentRequest("ORD20261027001", new BigDecimal("199.00"));

        // 3. 同一行代码 channel.pay(request)，运行时各自走自己的 doPay —— 继承带来的多态
        for (PaymentChannel channel : channels) {
            System.out.println("--------------");
            System.out.println(channel.pay(request));
        }

        // 4. 公共方法直接继承：所有渠道都有 queryOrder，父类只写了一次
        System.out.println("--------------");
        new AliPayChannel("M10001").queryOrder("ORD20261027001");

        // 5. 银联重写了 queryOrder：先执行父类逻辑（super.queryOrder），再追加自己的逻辑
        new UnionPayChannel("M10001").queryOrder("ORD20261027001");
    }
}
