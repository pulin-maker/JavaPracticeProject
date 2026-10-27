package com.nanjiabawa.oop.inheritance;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 银联渠道：演示"重写父类的普通方法"
 *
 * 不只是抽象方法才能重写，父类的普通方法也可以重写；
 * 用 super.方法名(...) 可以先执行父类的逻辑，再追加子类自己的逻辑
 */
public class UnionPayChannel extends PaymentChannel {

    public UnionPayChannel(String merchantId) {
        super("银联", merchantId);
    }

    @Override
    protected String doPay(PaymentRequest request) {
        String tradeNo = "UNP" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        System.out.println("[" + channelName + "] 走银联清算网络，金额：" + request.getAmount() + " 元");
        return tradeNo;
    }

    @Override
    public BigDecimal getFeeRate() {
        return new BigDecimal("0.005"); // 银联费率 0.5%
    }

    /**
     * 重写父类普通方法：先复用父类实现（super.queryOrder），再补充自己的增强逻辑
     * 这是企业代码里非常常见的"扩展而非替换"写法
     */
    @Override
    public void queryOrder(String orderId) {
        super.queryOrder(orderId);
        System.out.println("[" + channelName + "] 追加：同步对账系统，订单 " + orderId + " 已入账");
    }
}
