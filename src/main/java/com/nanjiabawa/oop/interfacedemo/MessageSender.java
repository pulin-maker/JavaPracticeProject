package com.nanjiabawa.oop.interfacedemo;

/**
 * 知识点：接口 —— 纯粹的"能力契约"
 *
 * 接口里只有三样东西：
 *   1. 常量        ：默认 public static final，天生不可变
 *   2. 抽象方法    ：默认 public abstract，实现类必须实现
 *   3. default 方法：Java 8+ 接口自带的默认实现（相当于接口版的"公共代码"）
 *
 * 接口没有：构造器、普通字段、代码块 —— 它是纯契约，无状态
 * 命名习惯：能力型接口用 -able/-er 结尾（Comparable、Runnable、MessageSender）
 */
public interface MessageSender {

    /** 接口常量：无论写不写修饰符，都是 public static final */
    int MAX_CONTENT_LENGTH = 500;

    /**
     * 发送消息：这是接口的"主契约"
     * 返回 true 表示发送成功 —— 实现类各自决定怎么发
     */
    boolean send(String target, String content);

    /**
     * default 方法（Java 8+）：群发是所有渠道通用的能力，
     * 在接口里给一份默认实现，实现类自动获得，不用每个都重写
     * （相当于把"模板方法"的思想搬进了接口）
     */
    default int sendBatch(String[] targets, String content) {
        int success = 0;
        for (String target : targets) {
            if (send(target, content)) {
                success++;
            }
        }
        return success;
    }
}