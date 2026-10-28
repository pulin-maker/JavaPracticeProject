package com.nanjiabawa.reflection.framework;

import java.lang.reflect.Field;

/**
 * 直观感受 MiniJson 的 5 个例子 —— 配合 MiniJson.java 源码逐段对照看
 *
 * 核心直觉：MiniJson 里没有一个 if 在判断"这是什么业务类"，
 * 它只认识 Field 和 Object，所以任何类丢进来都能转 JSON。
 */
public class MiniJsonFeelDemo {

    // 两个内部类：故意设计成和 OrderEntity 毫无关系的业务类型
    static class Address {
        private String city = "南京";
        private String street = "仙林大道";
    }

    static class User {
        private String name = "小明";
        private int age = 24;
        private Address address = new Address();   // 嵌套对象：考验递归
        private String nickname = null;            // null 字段：考验边界处理
    }

    public static void main(String[] args) throws Exception {
        // ============ 例 1：递归出口 —— 基础类型根本不走反射 ============
        System.out.println("===== 例1：基础类型（走到 if 就返回了，一个反射都没发生）=====");
        System.out.println("toJson(42)      = " + MiniJson.toJson(42));
        System.out.println("toJson(\"hello\") = " + MiniJson.toJson("hello"));
        System.out.println("toJson(true)    = " + MiniJson.toJson(true));
        System.out.println("toJson(null)    = " + MiniJson.toJson(null));

        // ============ 例 2：完整流程 —— 简单对象，逐步对照源码 ============
        System.out.println("\n===== 例2：简单对象（第一次进入 for 循环 + setAccessible）=====");
        Address addr = new Address();
        System.out.println("toJson(address) = " + MiniJson.toJson(addr));
        // 源码对照：
        //   obj instanceof String? 否，Number/Boolean? 否 → 走到 StringBuilder "{"
        //   getDeclaredFields() 拿到 [city, street] —— 注意：此时还不知道值是什么
        //   setAccessible(true) → field.get(addr) 读出 "南京" → 递归 toJson("南京") → "\"南京\""

        // ============ 例 3：递归嵌套 —— 对象里套对象 ============
        System.out.println("\n===== 例3：嵌套对象（toJson 自己调用自己）=====");
        User user = new User();
        System.out.println("toJson(user) = " + MiniJson.toJson(user));
        // 源码对照：处理 address 字段时，get(addr) 读出来是个 Address 对象，
        // 不是 String/Number/Boolean → 于是再次调用 toJson(Address对象) → 进入第二层循环
        // 这就是为什么 MiniJson.toJson 会"自我调用"——深度由数据决定，代码长度不变

        // ============ 例 4：去掉 setAccessible 会怎样 —— 反射不是万能通行证 ============
        // 注意：不能用本类的内部类演示（同一顶层类内 Java 天然允许互访 private），
        // 必须用"别的包"的类 —— 这里用 entity 包下的 OrderEntity
        System.out.println("\n===== 例4：没有 setAccessible，private 字段读不出来 =====");
        Field amountField = com.nanjiabawa.reflection.entity.OrderEntity.class.getDeclaredField("amount");
        com.nanjiabawa.reflection.entity.OrderEntity order = new com.nanjiabawa.reflection.entity.OrderEntity();
        try {
            amountField.get(order);           // 故意不调用 setAccessible(true)
        } catch (IllegalAccessException e) {
            System.out.println("直接 field.get(order) 抛异常了：");
            System.out.println("    " + e.getClass().getSimpleName());
        }
        amountField.setAccessible(true);
        System.out.println("setAccessible(true) 之后再读：" + amountField.get(new com.nanjiabawa.reflection.entity.OrderEntity()));

        // ============ 例 5：通用性的终极验证 —— 同一个方法，N 个类 ============
        System.out.println("\n===== 例5：一个方法吃遍所有类 =====");
        System.out.println(MiniJson.toJson(new Address()));
        System.out.println(MiniJson.toJson(new User()));
        System.out.println(MiniJson.toJson(new com.nanjiabawa.reflection.entity.OrderEntity()));
        // 三个类互相不认识、字段完全不同，但 MiniJson 里没有出现任何一个类名。
        // 这就是 Jackson 能序列化"全世界的类"的原因：它面向 Field 编程，不面向业务类编程。
    }
}
