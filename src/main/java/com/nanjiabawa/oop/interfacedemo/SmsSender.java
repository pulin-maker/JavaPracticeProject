package com.nanjiabawa.oop.interfacedemo;

/**
 * 短信发送器：同时实现两个接口（多实现，用逗号分隔）
 * 契约 1：MessageSender —— 必须实现 send
 * 契约 2：Retryable     —— 必须实现 retry
 * 漏实现任何一个，编译直接报错
 */
public class SmsSender implements MessageSender, Retryable {

    @Override
    public boolean send(String target, String content) {
        if (content.length() > MessageSender.MAX_CONTENT_LENGTH) {
            System.out.println("[短信] 内容超长，发送失败");
            return false;
        }
        System.out.println("[短信] 发送到 " + target + "：" + content);
        return true;
    }

    @Override
    public void retry(String target) {
        System.out.println("[短信] 网关抖动，第 " + Retryable.MAX_RETRY + " 次内重试 " + target);
    }
}