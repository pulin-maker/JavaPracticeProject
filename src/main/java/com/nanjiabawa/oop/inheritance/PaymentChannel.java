package com.nanjiabawa.oop.inheritance;

import java.math.BigDecimal;

/**
 * 知识点：抽象父类（模板方法模式）
 *
 * 支付渠道父类，定义所有渠道共有的"支付主流程"：
 *   校验参数 -> 调用具体支付 -> 记录日志
 *
 * 企业级设计要点：
 *   1. 抽象类不能被 new，只能被继承（比如不存在"一个纯粹的支付渠道"）
 *   2. pay() 用 final 修饰：主流程（模板）不允许子类改动，只允许替换其中的步骤
 *   3. doPay() / getFeeRate() 是抽象方法：强制每个子类必须实现自己的逻辑
 *   4. 字段用 protected：子类可以直接访问，包外依然封装
 *   5. queryOrder() 是普通方法：公共能力写在父类，子类白拿，不用重复写
 */
public abstract class PaymentChannel {

    protected final String channelName;
    protected final String merchantId;

    protected PaymentChannel(String channelName, String merchantId) {
        this.channelName = channelName;
        this.merchantId = merchantId;
    }

    /**
     * 模板方法：定义支付主流程，final 保证子类不能重写它
     */
    public final PaymentResult pay(PaymentRequest request) {
        System.out.println("[" + channelName + "] 收到支付请求，订单号：" + request.getOrderId());

        // 第一步：公共校验（所有渠道都一样）
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            System.out.println("[" + channelName + "] 校验失败：金额非法");
            return PaymentResult.fail(channelName, "金额非法");
        }

        // 第二步：具体支付（每个渠道不一样，交给子类实现）
        String tradeNo = doPay(request);

        // 第三步：公共收尾（所有渠道都一样）
        System.out.println("[" + channelName + "] 支付流程结束，费率：" + getFeeRate());
        return PaymentResult.success(channelName, tradeNo, "支付成功");
    }

    /**
     * 具体支付逻辑由子类实现（抽象方法 = 只声明不给实现）
     */
    protected abstract String doPay(PaymentRequest request);

    /**
     * 手续费率由子类实现
     */
    public abstract BigDecimal getFeeRate();

    /**
     * 公共能力：订单查询，所有渠道直接继承使用
     */
    public void queryOrder(String orderId) {
        System.out.println("[" + channelName + "] 查询订单 " + orderId + "：状态=已支付, 商户=" + merchantId);
    }
}
