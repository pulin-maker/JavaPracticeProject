package com.nanjiabawa.oop.inheritance;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 微信支付渠道：与支付宝平行的另一个子类
 * 注意它没有复制任何父类代码，只实现了"自己不一样"的部分
 */
public class WeChatPayChannel extends PaymentChannel {

    public WeChatPayChannel(String merchantId) {
        super("微信支付", merchantId);
    }

    @Override
    protected String doPay(PaymentRequest request) {
        String tradeNo = "WX" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        System.out.println("[" + channelName + "] 调用微信下单接口，金额：" + request.getAmount() + " 元");
        return tradeNo;
    }

    @Override
    public BigDecimal getFeeRate() {
        return new BigDecimal("0.0038"); // 微信费率 0.38%
    }
}
