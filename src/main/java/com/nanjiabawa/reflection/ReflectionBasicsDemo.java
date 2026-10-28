package com.nanjiabawa.reflection;

import com.nanjiabawa.reflection.entity.OrderEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射入门：把 Class 对象当成"类的说明书"来读、来用
 *
 * 一句话理解反射：
 *   普通代码：编译期就知道 new OrderEntity()，类是"写死的"
 *   反射代码：运行期才拿到 Class 对象，类是"动态进来的"
 *   —— 所以框架（Spring/MyBatis）在写代码时根本不知道你的业务类，全靠反射在运行期操作
 */
public class ReflectionBasicsDemo {

    public static void main(String[] args) throws Exception {
        // ============ 1. 获取 Class 对象的三种方式 ============
        System.out.println("===== 1. 获取 Class 对象 =====");
        // 方式一：类名.class —— 编译期就知道类（最常用在工具类里）
        Class<OrderEntity> c1 = OrderEntity.class;
        // 方式二：对象.getClass() —— 手上已有一个实例
        Class<?> c2 = new OrderEntity().getClass();
        // 方式三：Class.forName(全类名) —— 只有字符串，运行期才决定加载谁（框架最爱，如 JDBC 驱动加载）
        Class<?> c3 = Class.forName("com.nanjiabawa.reflection.entity.OrderEntity");

        // 三种方式拿到的是同一个 Class 对象（一个类在 JVM 里只有一份说明书）
        System.out.println("c1 == c2 ? " + (c1 == c2) + "，c2 == c3 ? " + (c2 == c3));
        System.out.println("全类名：" + c1.getName() + "，简单名：" + c1.getSimpleName());

        // ============ 2. 读"说明书"：字段、方法、构造器 ============
        System.out.println("\n===== 2. 查看类的结构（包括 private 成员）=====");
        // getDeclaredXxx：本类声明的所有成员（含 private）；getXxx：只含 public（含继承的）
        for (Field field : c1.getDeclaredFields()) {
            System.out.println("字段：" + field.getType().getSimpleName() + " " + field.getName());
        }
        for (Method method : c1.getDeclaredMethods()) {
            System.out.println("方法：" + method.getName() + "()" );
        }
        for (Constructor<?> constructor : c1.getDeclaredConstructors()) {
            System.out.println("构造器：" + constructor.getName() + "，参数个数：" + constructor.getParameterCount());
        }

        // ============ 3. 动态创建对象（等价于 new，但类名是运行期决定的）============
        System.out.println("\n===== 3. 反射创建实例 =====");
        Object order = c1.getDeclaredConstructor().newInstance();
        System.out.println("创建成功：" + order.getClass().getSimpleName() + "，此时各字段还是默认值");

        // ============ 4. 突破 private：给字段赋值 ============
        System.out.println("\n===== 4. 反射操作 private 字段 =====");
        Field orderNoField = c1.getDeclaredField("orderNo");
        orderNoField.setAccessible(true);               // 打破 private 封锁（框架赋值的核心一步）
        orderNoField.set(order, "JD20261027001");       // 等价于 order.orderNo = "..."（但那行编译都过不了）

        Field paidField = c1.getDeclaredField("paid");
        paidField.setAccessible(true);
        paidField.set(order, true);

        // 读值：field.get(order) 返回 Object，需要强转
        System.out.println("读取 private 字段 orderNo = " + orderNoField.get(order));

        // ============ 5. 动态调用方法 ============
        System.out.println("\n===== 5. 反射调用方法 =====");
        Method printSummary = c1.getMethod("printSummary");  // 无参方法
        printSummary.invoke(order);   // 等价于 order.printSummary()

        // 带参数的方法调用：invoke(对象, 参数1, 参数2...)
        // Method m = c1.getMethod("xxx", String.class, int.class);
        // m.invoke(order, "参数", 100);
    }
}
