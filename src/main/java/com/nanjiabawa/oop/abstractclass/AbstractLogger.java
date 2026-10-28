package com.nanjiabawa.oop.abstractclass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 知识点：抽象类 —— 一个"半成品"类
 *
 * 抽象类的本质：既有"做好的部分"，也有"没做完的部分"：
 *   做好的部分   ：字段（loggerName）、构造器、具体方法（format）
 *   没做完的部分 ：write() 抽象方法 —— 只有声明，没有方法体
 *
 * 三个硬性规则：
 *   1. 有抽象方法的类必须声明为 abstract（编译器强制）
 *   2. 抽象类不能 new —— 半成品没法直接用（new AbstractLogger(...) 编译报错）
 *   3. 子类必须实现所有抽象方法，否则子类也得声明为 abstract
 *
 * 企业命名习惯：抽象类用 AbstractXxx 开头（JDK 的 AbstractList、Spring 的 AbstractApplicationContext）
 */
public abstract class AbstractLogger {

    protected final String loggerName; // 公共字段：所有日志器共享（谁在打日志）

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 抽象类也有构造器！它不能被 new，但会被子类的 super(...) 调用，
     * 用来初始化公共字段 —— 这就是抽象类和接口最大的不同之一
     */
    protected AbstractLogger(String loggerName) {
        this.loggerName = loggerName;
    }

    /**
     * 公共能力：拼日志格式。所有子类直接继承使用，一行不用写。
     * 普通方法可以写在抽象类里 —— 这是抽象类比接口"厚"的地方
     */
    protected String format(String level, String message) {
        return LocalDateTime.now().format(FMT)
                + " [" + level + "] " + loggerName + " - " + message;
    }

    /**
     * 统一日志入口：final 锁定流程（复用模板方法模式的经验）
     * 格式化是公共步骤，"落到哪里"是变化点
     */
    public final void log(String level, String message) {
        String line = format(level, message); // 做好的部分：公共格式化
        write(line);                          // 没做完的部分：交给子类
    }

    /**
     * 扩展点：日志写到哪里？控制台？文件？数据库？
     * 抽象方法 = 给子类下的"强制作业"
     */
    protected abstract void write(String formattedLine);
}