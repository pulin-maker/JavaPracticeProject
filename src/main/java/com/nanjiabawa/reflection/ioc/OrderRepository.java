package com.nanjiabawa.reflection.ioc;

/**
 * 业务 Bean 2：订单仓库，模拟数据访问层（DAO）
 */
@Component
public class OrderRepository {

    public void save(String orderNo) {
        System.out.println("[数据] OrderRepository 已保存订单到数据库：" + orderNo);
    }
}
