package com.nanjiabawa.oop.inheritance;

/**
 * 支付结果（统一出参，各渠道返回格式一致，调用方无需关心细节）
 */
public class PaymentResult {

    private final String channelName;
    private final String tradeNo;
    private final boolean success;
    private final String message;

    private PaymentResult(String channelName, String tradeNo, boolean success, String message) {
        this.channelName = channelName;
        this.tradeNo = tradeNo;
        this.success = success;
        this.message = message;
    }

    public static PaymentResult success(String channelName, String tradeNo, String message) {
        return new PaymentResult(channelName, tradeNo, true, message);
    }

    public static PaymentResult fail(String channelName, String message) {
        return new PaymentResult(channelName, null, false, message);
    }

    @Override
    public String toString() {
        return "渠道=" + channelName + ", 成功=" + success
                + (tradeNo != null ? ", 交易号=" + tradeNo : "")
                + ", 信息=" + message;
    }
}
