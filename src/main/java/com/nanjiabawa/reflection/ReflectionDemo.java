package com.nanjiabawa.reflection;

import com.nanjiabawa.reflection.entity.OrderEntity;
import com.nanjiabawa.reflection.framework.MiniJson;
import com.nanjiabawa.reflection.framework.RowMapper;
import com.nanjiabawa.reflection.ioc.MiniIocContainer;
import com.nanjiabawa.reflection.ioc.OrderService;

/**
 * 反射总览入口：一次跑完 4 个场景，建议按下面的顺序逐段看
 *
 *   场景 1  ReflectionBasicsDemo —— 反射 API 基本功（Class/Field/Method/Constructor）
 *   场景 2  MiniJson             —— Jackson 序列化的原理
 *   场景 3  RowMapper            —— MyBatis 结果集映射的原理（有独立 main）
 *   场景 4  MiniIocContainer     —— Spring IoC 容器的原理（注解 + 反射）
 *
 * 企业级一句话总结：
 *   框架 = "配置/注解（声明你想要什么）" + "反射（运行期动态干活）"
 *   学反射不是为了天天手写它，而是为了看懂 Spring/MyBatis/Jackson 在背后做了什么。
 */
public class ReflectionDemo {

    public static void main(String[] args) {
        // ============ 场景 1：反射基本功 ============
        System.out.println("################ 场景 1：反射基本功 ################");
        try {
            ReflectionBasicsDemo.main(args);   // 反射相关方法大多声明了受检异常，需要处理
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // ============ 场景 2：迷你 JSON 序列化器 ============
        System.out.println("\n################ 场景 2：MiniJson（Jackson 原理）################");
        OrderEntity order = new OrderEntity();
        // 这里假装字段已被赋值（实际反射赋值过程见 ReflectionBasicsDemo 第 4 步）
        try {
            var orderNoField = OrderEntity.class.getDeclaredField("orderNo");
            orderNoField.setAccessible(true);
            orderNoField.set(order, "TB20261027002");
            var amountField = OrderEntity.class.getDeclaredField("amount");
            amountField.setAccessible(true);
            amountField.set(order, new java.math.BigDecimal("299.00"));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        System.out.println("序列化结果：" + MiniJson.toJson(order));

        // ============ 场景 3：迷你 IoC 容器 ============
        System.out.println("\n################ 场景 3：MiniIoC（Spring 原理）################");
        MiniIocContainer container = new MiniIocContainer();
        container.register(OrderService.class, com.nanjiabawa.reflection.ioc.OrderRepository.class);

        // 从容器拿 Bean（等价于 Spring 的 @Autowired / applicationContext.getBean）
        OrderService orderService = container.getBean(OrderService.class);
        orderService.createOrder("TB20261027003");
        // 看输出：OrderService 里的 private orderRepository 已经有值了 ——
        // 但我们从头到尾没写过 new OrderRepository() 塞进去，这就是反射注入的威力

        // ============ 场景 4：迷你结果集映射 ============
        System.out.println("\n################ 场景 4：RowMapper（MyBatis 原理）################");
        RowMapper.main(args);
    }
}
