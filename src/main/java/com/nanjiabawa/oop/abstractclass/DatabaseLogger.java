package com.nanjiabawa.oop.abstractclass;

/**
 * 数据库日志器：write 的实现 = 插入日志表（审计类系统常用）
 * 对比：三个子类各自只有十几行，因为公共逻辑全在抽象父类里
 */
public class DatabaseLogger extends AbstractLogger {

    public DatabaseLogger(String loggerName) {
        super(loggerName);
    }

    @Override
    protected void write(String formattedLine) {
        // 真实框架里这里是：INSERT INTO sys_log(content) VALUES (?)
        System.out.println("  -> [数据库] INSERT INTO sys_log(content) VALUES ('" + formattedLine + "')");
    }
}