package com.nanjiabawa.oop.abstractclass;

/**
 * 文件日志器：write 的实现 = 追加写入日志文件
 * 模拟企业里滚动写入 app.log 的行为
 */
public class FileLogger extends AbstractLogger {

    public FileLogger(String loggerName) {
        super(loggerName);
    }

    @Override
    protected void write(String formattedLine) {
        // 真实框架里这里是：Files.write(Paths.get("logs/app.log"), line.getBytes(), APPEND);
        System.out.println("  -> [文件] 追加写入 logs/app.log");
        System.out.println(formattedLine);
    }
}