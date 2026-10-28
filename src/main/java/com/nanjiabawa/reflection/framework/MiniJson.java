package com.nanjiabawa.reflection.framework;

import java.lang.reflect.Field;

/**
 * 迷你 JSON 序列化器 —— Jackson/Fastjson 的底层原理
 *
 * 你调用 objectMapper.writeValueAsString(order) 时，Jackson 内部干的就是这件事：
 *   1. 拿到对象的 Class 说明书
 *   2. getDeclaredFields() 遍历所有字段
 *   3. setAccessible(true) 突破 private
 *   4. field.get(obj) 读出值，拼成 JSON
 *
 * 它不知道你要序列化什么类 —— 任何 Object 丢进来都能处理，这就是"通用框架"的写法。
 */
public class MiniJson {

    /** 把任意对象转成 JSON 字符串（仅演示：支持 String/数值/布尔/null） */
    public static String toJson(Object obj) {
        // null 直接输出
        if (obj == null) {
            return "null";
        }
        // String/数字/布尔：JSON 里不需要 {"字段":值} 的包装
        if (obj instanceof String) {
            return "\"" + obj + "\"";
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }

        // 复杂对象：逐个字段拼 {"name":value,"name":value}
        StringBuilder json = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);              // 反射核心：突破 private
            Object value;
            try {
                value = fields[i].get(obj);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("读取字段失败：" + fields[i].getName(), e);
            }
            json.append("\"").append(fields[i].getName()).append("\":").append(toJson(value));
            if (i < fields.length - 1) {
                json.append(",");
            }
        }
        return json.append("}").toString();
    }
}
