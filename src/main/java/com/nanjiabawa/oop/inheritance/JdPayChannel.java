package com.nanjiabawa.oop.inheritance;

import java.math.BigDecimal;
import java.util.UUID;

public class JdPayChannel extends PaymentChannel {
    protected JdPayChannel(String merchantId) {
        super("京东", merchantId);
    }

    @Override
    protected String doPay(PaymentRequest request) {
        // 模拟调用京东 SDK 下单
        String tradeNo = "ALI" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        System.out.println("[" + channelName + "] 调用京东网关，金额：" + request.getAmount() + " 元");
        return tradeNo;
    }

    @Override
    public BigDecimal getFeeRate() {
        return null;
    }
}
