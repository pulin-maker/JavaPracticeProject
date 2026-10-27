package com.nanjiabawa.oop.polymorphism;

import java.math.BigDecimal;

/**
 * 会员抽象父类：定义"会员能做什么"的统一契约
 */
public abstract class Member {

    protected final String name;

    protected Member(String name) {
        this.name = name;
    }

    /** 会员等级名称（子类各自实现） */
    public abstract String getLevelName();

    /**
     * 根据订单原价计算实际应付金额（各等级折扣不同，子类各自实现）
     * 这就是多态的核心：方法名一样，行为不一样
     */
    public abstract BigDecimal calcPrice(BigDecimal original);

    @Override
    public String toString() {
        return getLevelName() + "(" + name + ")";
    }
}
