package com.nanjiabawa.oop.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 知识点：企业级日志（SLF4J + Logback）
 *
 * 架构：业务代码 -> SLF4J 门面(接口) -> Logback 实现
 *   代码只 import org.slf4j，换实现（如 Log4j2）业务代码零改动 —— 面向接口编程的实战
 *
 * 使用三步：
 *   1. 加依赖：slf4j-api（门面）+ logback-classic（实现）
 *   2. 获取 Logger：LoggerFactory.getLogger(当前类.class)
 *   3. 按级别打日志：trace < debug < info < warn < error
 *
 * 企业铁律：
 *   1. 禁用 System.out.println —— 无法控制级别、无时间戳、不能统一关闭
 *   2. 用 {} 占位符，不要用 + 拼接 —— 字符串拼接是立即执行的，
 *      而占位符在日志被过滤时不拼接，性能好得多
 *   3. 异常对象作为最后一个参数传入 error —— 自动打印完整堆栈
 *   4. 不要打印敏感信息（密码、身份证），手机号要脱敏（138****8000）
 *   5. 级别语义要准确：error 出事了要人处理 / warn 有隐患 / info 关键业务节点 / debug 开发调试
 */
public class LoggingDemo {

    /** 每个类一个 Logger，参数传当前类的 Class —— 日志里能看出是哪个类打的 */
    private static final Logger log = LoggerFactory.getLogger(LoggingDemo.class);

    public static void main(String[] args) {
        // 五个级别，从低到高：TRACE < DEBUG < INFO < WARN < ERROR
        log.trace("这是 trace：最详细的追踪，生产环境一般不开");
        log.debug("这是 debug：调试信息，下单入参 orderId={}", "ORD001");
        log.info("这是 info：关键业务节点，订单 {} 支付成功，金额 {} 元", "ORD001", 199.00);
        log.warn("这是 warn：有隐患但要人工关注，库存预警 sku={}", "1001");

        // error 的标准姿势：异常对象作为最后一个参数，自动打印完整堆栈
        try {
            int result = 1 / 0;
        } catch (Exception e) {
            log.error("下单流程异常，orderId={}", "ORD001", e);
        }

        // 反面教材（别学）：log.error("出错：" + e.getMessage()); 丢失堆栈，排查不了问题
    }
}
