package com.nanjiabawa.reflection.framework;

import com.nanjiabawa.reflection.entity.OrderEntity;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 迷你结果集映射器 —— MyBatis 把数据库一行数据变成 Java 对象的原理
 *
 * JDBC 查询回来的是 Map<列名, 值>（ResultSet 的本质），
 * MyBatis 帮你变成 OrderEntity 对象，靠的就是反射：
 *   1. 拿到目标类的所有字段
 *   2. 按字段名去 Map 里找对应的列值
 *   3. setAccessible + field.set 塞进对象
 *
 * 你项目里写的 @ResultMap、驼峰转换，底层全是这几行代码。
 */
public class RowMapper {

    /** 模拟 JDBC 查出来的一行数据（列名 -> 值），实际项目里是 ResultSet */
    public static OrderEntity mapRow(Map<String, Object> row) {
        try {
            // 反射创建空对象（等价于 new OrderEntity()，但类是"动态"的）
            OrderEntity order = new OrderEntity();

            for (Field field : OrderEntity.class.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = row.get(field.getName());   // 字段名当列名去查
                if (value == null) {
                    continue;                              // 该列没查出来，保持默认值
                }
                // 类型转换：数据库驱动的类型和 Java 类型往往不完全一致（真实框架这里更细致）
                Object converted = convert(value, field.getType());
                field.set(order, converted);               // 反射赋值，突破 private
            }
            return order;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("结果集映射失败", e);
        }
    }

    /** 简易类型转换：把查出来的值转成字段声明的类型 */
    private static Object convert(Object value, Class<?> targetType) {
        if (targetType == Long.class)       return Long.valueOf(value.toString());
        if (targetType == BigDecimal.class) return new BigDecimal(value.toString());
        if (targetType == boolean.class)    return Boolean.parseBoolean(value.toString());
        if (targetType == LocalDateTime.class) return LocalDateTime.parse(value.toString());
        return value;   // String 等直接返回
    }

    /** 演示入口 */
    public static void main(String[] args) {
        // 模拟 "SELECT order_id, order_no, amount, paid, create_time FROM t_order WHERE id = 1"
        Map<String, Object> row = new HashMap<>();
        row.put("orderId", 1001L);
        row.put("orderNo", "TB20261027001");
        row.put("amount", "199.00");                 // 注意：数据库常返回字符串，框架帮你转
        row.put("paid", "true");
        row.put("createTime", "2026-10-27T10:30:00");

        OrderEntity order = mapRow(row);
        order.printSummary();   // 输出：订单[TB20261027001] 金额：199.00 元，支付状态：已支付
    }
}
