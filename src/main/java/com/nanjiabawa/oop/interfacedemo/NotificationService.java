package com.nanjiabawa.oop.interfacedemo;

import java.util.List;

/**
 * 通知服务：企业里"Service 接口 + Impl"的雏形
 * 只依赖 MessageSender 接口，不认识任何具体渠道
 */
public class NotificationService {

    private final List<MessageSender> senders; // 接口类型做字段 —— 面向接口编程

    public NotificationService(List<MessageSender> senders) {
        this.senders = senders;
    }

    /** 全渠道广播：同一行 send 调用，运行时分发到各实现 */
    public void broadcast(String target, String content) {
        System.out.println("====== 全渠道广播 ======");
        for (MessageSender sender : senders) {
            sender.send(target, content); // 编译看左边（接口），运行看右边（实现类）
        }
    }
}