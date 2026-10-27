package com.nanjiabawa.oop.polymorphism;

import java.math.BigDecimal;
import java.util.List;

/**
 * 知识点：多态（Polymorphism）
 *
 * 多态三要素（缺一不可）：
 *   1. 有继承（或实现）关系：Member <- VipMember / SvipMember
 *   2. 子类重写了父类方法：calcPrice / getLevelName
 *   3. 父类引用指向子类对象：Member m = new SvipMember("小明")
 *
 * 核心口诀：编译看左边（引用类型决定能用哪些方法），运行看右边（实际对象决定执行谁的方法）
 *
 * 两种转型：
 *   - 向上转型：Member m = new SvipMember(...)，自动、安全，"代价"是只能调父类声明的方法
 *   - 向下转型：SvipMember svip = (SvipMember) m; 恢复子类特有能力，
 *     转型前必须 instanceof 判断，否则类型不符会抛 ClassCastException
 *
 * 企业级价值：payFor(Member member) 这种"面向抽象编程"让代码对扩展开放——
 * 新增会员等级不改任何已有代码，这正是低耦合的来源。
 */
public class PolymorphismDemo {

    public static void main(String[] args) {
        // 1. 向上转型：声明类型是 Member，实际对象是 SvipMember
        Member member = new SvipMember("小明");
        System.out.println("编译期可见的方法：只有 Member 声明的部分");
        // member.useExpressRefund(); // 取消注释会编译报错：Member 里没有这个方法

        // 2. 多态参数 + 动态绑定：同一订单、同一行结算代码，不同会员不同价
        Order order = new Order("ORD20261028001", new BigDecimal("100.00"));
        List<Member> members = List.of(
                new NormalMember("小张"),
                new VipMember("小红"),
                new SvipMember("小明")
        );
        System.out.println("--------------");
        for (Member m : members) {
            order.payFor(m); // 运行时按 m 的实际类型分发折扣逻辑
        }

        // 3. 向下转型前必须用 instanceof 判断（Java 16+ 模式匹配写法：判断+转型一步完成）
        System.out.println("--------------");
        for (Member m : members) {
            if (m instanceof SvipMember svip) {
                svip.useExpressRefund(); // 只有 SVIP 才有极速退款
            } else {
                System.out.println(m + "：无极速退款特权");
            }
        }

        // 4. 反面教材：不做 instanceof 直接强转，运行时爆炸
        Member normal = new NormalMember("老王");
        try {
            SvipMember wrong = (SvipMember) normal; // 编译能过，运行时 ClassCastException
            wrong.useExpressRefund();
        } catch (ClassCastException e) {
            System.out.println("强转失败：" + e.getClass().getSimpleName() + "（所以必须先 instanceof）");
        }
    }
}
