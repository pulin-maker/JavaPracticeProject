package com.nanjiabawa.reflection.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模拟数据库表 t_order 对应的实体类
 *
 * 关键点：字段全部是 private —— 正常代码外部根本碰不到，
 * 但反射可以"破例"访问（这就是框架能帮你赋值的底气）。
 */
public class OrderEntity {

    private Long orderId;               // 订单ID
    private String orderNo;             // 订单号
    private BigDecimal amount;          // 订单金额
    private boolean paid;               // 是否已支付
    private LocalDateTime createTime;   // 创建时间

    public OrderEntity() {
        // 无参构造器：反射 newInstance 时最常用的入口，别随手删掉！
    }

    /** 业务方法：模拟"展示订单"，后面用反射来调用它 */
    public void printSummary() {
        System.out.println("订单[" + orderNo + "] 金额：" + amount + " 元，支付状态：" + (paid ? "已支付" : "未支付"));
    }

    public Long getOrderId() { return orderId; }
    public String getOrderNo() { return orderNo; }
    public BigDecimal getAmount() { return amount; }
    public boolean isPaid() { return paid; }
    public LocalDateTime getCreateTime() { return createTime; }
}
