package com.nanjiabawa.oop.interfacedemo;

import java.util.List;

/**
 * 知识点：接口（Interface）
 *
 * 心智模型：接口 = "能力标准/插座"
 *   - 类实现接口 = 插头符合国家标准，谁符合谁就能插上用
 *   - 使用方只认标准（接口），不认具体品牌（实现类）
 *
 * 核心规则：
 *   1. implements 实现接口，可以同时实现多个（弥补单继承限制）
 *   2. 接口不能 new、没有构造器、没有普通字段（无状态）
 *   3. 接口常量天生 public static final；抽象方法天生 public abstract
 *   4. default 方法：接口自带的默认实现，实现类可直接继承使用
 *   5. 接口引用 = 更"薄"的抽象：只有接口里声明的能力可用
 */
public class InterfaceDemo {

    public static void main(String[] args) {
        // 1. 面向接口编程：企业里最常见的形态
        //    真实项目里这里是 Spring 的 @Autowired 注入，本地用 new 模拟
        NotificationService service = new NotificationService(List.of(
                new SmsSender(),
                new EmailSender(),
                new DingTalkSender()
        ));
        service.broadcast("13800138000", "您的订单已发货");

        // 2. 接口引用：左边是接口，右边是实现类
        MessageSender sender = new EmailSender();
        System.out.println("====== 单渠道发送 ======");
        sender.send("zhang@corp.com", "验证码 6666");
        // sender.retry("x"); // 编译报错：MessageSender 接口里没有 retry —— 能力由接口决定

        // 3. default 方法：接口白送的群发能力，EmailSender 没写一行代码
        System.out.println("====== 群发（default 方法）======");
        int ok = sender.sendBatch(new String[]{"a@corp.com", "b@corp.com"}, "系统维护通知");
        System.out.println("群发成功数：" + ok);

        // 4. 接口常量：public static final，直接用接口名访问
        System.out.println("====== 接口常量 ======");
        System.out.println("内容上限：" + MessageSender.MAX_CONTENT_LENGTH + " 字");

        // 5. 多实现的能力探测：同一个对象，实现几个接口就具备几种"身份"
        System.out.println("====== 能力探测（instanceof）======");
        MessageSender sms = new SmsSender();
        if (sms instanceof Retryable retryable) {
            retryable.retry("13800138000"); // 短信渠道有重试能力
        }
        if (!(sender instanceof Retryable)) {
            System.out.println("[邮件] 邮件渠道不支持自动重试");
        }

        // 6. 对比点：接口没有构造器、不能 new
        // MessageSender m = new MessageSender(); // 编译报错
    }
}