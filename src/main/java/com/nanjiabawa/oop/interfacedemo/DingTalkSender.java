package com.nanjiabawa.oop.interfacedemo;

/** 钉钉发送器：企业内部群通知的标配渠道 */
public class DingTalkSender implements MessageSender {

    @Override
    public boolean send(String target, String content) {
        System.out.println("[钉钉] 机器人推送到群 " + target + "：" + content);
        return true;
    }
}