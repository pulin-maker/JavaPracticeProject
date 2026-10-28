package com.nanjiabawa.reflection.ioc;

/**
 * 业务 Bean 1：订单服务，依赖订单仓库
 * 注意：它自己从不 new 依赖对象，全靠容器反射注入
 */
@Component
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;   // private 字段，容器照样能注入

    public void createOrder(String orderNo) {
        System.out.println("[业务] OrderService 准备保存订单：" + orderNo);
        orderRepository.save(orderNo);
    }
}
