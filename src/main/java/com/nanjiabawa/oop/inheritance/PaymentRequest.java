package com.nanjiabawa.oop.inheritance;

import java.math.BigDecimal;

/**
 * 支付请求（简单 POJO，模拟企业里统一的入参对象）
 */
public class PaymentRequest {

    private final String orderId;
    private final BigDecimal amount;

    public PaymentRequest(String orderId, BigDecimal amount) {
        this.orderId = orderId;
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
