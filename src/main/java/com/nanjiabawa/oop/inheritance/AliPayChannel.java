package com.nanjiabawa.oop.inheritance;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 知识点：子类继承（extends）+ 方法重写（Override）
 *
 * 1. extends：继承父类所有非 private 的字段和方法
 * 2. super(...)：调用父类构造器，必须是子类构造器第一条语句
 * 3. @Override：重写父类抽象方法；编译器会校验方法签名是否真的存在于父类
 * 4. 重写规则"两同两小一大"：方法名和参数列表相同；
 *    返回值和抛出的异常不大于父类；访问权限不小于父类（这里 protected -> protected）
 */
public class AliPayChannel extends PaymentChannel {

    public AliPayChannel(String merchantId) {
        // 调用父类构造器，把渠道名定死为"支付宝"
        super("支付宝", merchantId);
    }

    @Override
    protected String doPay(PaymentRequest request) {
        // 模拟调用支付宝 SDK 下单
        String tradeNo = "ALI" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        System.out.println("[" + channelName + "] 调用支付宝网关，金额：" + request.getAmount() + " 元");
        return tradeNo;
    }

    @Override
    public BigDecimal getFeeRate() {
        return new BigDecimal("0.006"); // 支付宝费率 0.6%
    }
}
