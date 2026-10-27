package com.nanjiabawa.oop.polymorphism;

import java.math.BigDecimal;

/** 普通会员：不打折，原价支付 */
public class NormalMember extends Member {

    public NormalMember(String name) {
        super(name);
    }

    @Override
    public String getLevelName() {
        return "普通会员";
    }

    @Override
    public BigDecimal calcPrice(BigDecimal original) {
        return original; // 原价
    }
}
