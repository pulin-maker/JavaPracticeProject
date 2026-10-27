package com.nanjiabawa.oop.polymorphism;

import java.math.BigDecimal;

/** VIP 会员：95 折 */
public class VipMember extends Member {

    public VipMember(String name) {
        super(name);
    }

    @Override
    public String getLevelName() {
        return "VIP会员";
    }

    @Override
    public BigDecimal calcPrice(BigDecimal original) {
        return original.multiply(new BigDecimal("0.95"));
    }
}
