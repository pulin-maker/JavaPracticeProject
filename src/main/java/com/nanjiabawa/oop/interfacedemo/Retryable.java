package com.nanjiabawa.oop.interfacedemo;

/**
 * 第二个接口：可重试能力
 * 一个类可以同时实现多个接口 —— 这是 Java 弥补"单继承"限制的方式
 */
public interface Retryable {

    int MAX_RETRY = 3;

    void retry(String target);
}