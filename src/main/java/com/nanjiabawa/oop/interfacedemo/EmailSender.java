package com.nanjiabawa.oop.interfacedemo;

/** 邮件发送器：只实现 MessageSender（没有重试能力，不能被当作 Retryable 使用） */
public class EmailSender implements MessageSender {

    @Override
    public boolean send(String target, String content) {
        System.out.println("[邮件] 发送到 " + target + "，主题：系统通知，正文：" + content);
        return true;
    }
}