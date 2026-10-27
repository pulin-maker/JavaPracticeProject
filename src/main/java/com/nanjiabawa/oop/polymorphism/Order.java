package com.nanjiabawa.oop.polymorphism;

import java.math.BigDecimal;

/**
 * 订单：演示"多态参数"——方法参数声明为父类类型
 */
public class Order {

    private final String orderNo;
    private final BigDecimal originalAmount;

    public Order(String orderNo, BigDecimal originalAmount) {
        this.orderNo = orderNo;
        this.originalAmount = originalAmount;
    }

    /**
     * 多态参数：参数是抽象父类 Member，任何会员子类都能传进来。
     * 结算时 member.calcPrice(...) 会按实际类型分发到对应折扣逻辑，
     * 未来新增"黑卡会员"这个方法一行都不用改。
     */
    public BigDecimal payFor(Member member) {
        System.out.println("订单 " + orderNo + "，会员：" + member + "，原价：" + originalAmount + " 元");
        BigDecimal payable = member.calcPrice(originalAmount);
        System.out.println("  应付：" + payable + " 元");
        return payable;
    }
}
