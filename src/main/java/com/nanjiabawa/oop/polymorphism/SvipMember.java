package com.nanjiabawa.oop.polymorphism;

import java.math.BigDecimal;

/**
 * SVIP 会员：88 折，还独享"极速退款"特权
 * 注意：特有方法 useExpressRefund 不在父类契约里，
 * 通过父类引用直接调用会编译报错，必须向下转型后才能调用
 */
public class SvipMember extends Member {

    public SvipMember(String name) {
        super(name);
    }

    @Override
    public String getLevelName() {
        return "SVIP会员";
    }

    @Override
    public BigDecimal calcPrice(BigDecimal original) {
        return original.multiply(new BigDecimal("0.88"));
    }

    /** 子类特有能力：极速退款 */
    public void useExpressRefund() {
        System.out.println("[" + name + "] 走极速退款通道，2 小时内到账！");
    }
}
