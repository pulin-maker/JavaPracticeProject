package com.nanjiabawa.oop.abstractclass;

/** 控制台日志器：write 的实现 = 打印到控制台 */
public class ConsoleLogger extends AbstractLogger {

    public ConsoleLogger(String loggerName) {
        super(loggerName);
    }

    @Override
    protected void write(String formattedLine) {
        System.out.println(formattedLine);
    }
}