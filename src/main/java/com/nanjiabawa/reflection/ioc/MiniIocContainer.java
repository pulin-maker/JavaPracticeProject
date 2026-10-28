package com.nanjiabawa.reflection.ioc;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 迷你 IoC 容器 —— Spring 容器的核心原理（约 40 行代码看懂 Spring）
 *
 * Spring 启动时干的三件事，这里全都有：
 *   1. 扫描：找出所有贴了 @Component 的类      （真实 Spring 扫 classpath，这里手动注册）
 *   2. 实例化：反射调用无参构造器 new 出对象，放进 Map 缓存（这就是"单例池"）
 *   3. 注入：遍历字段，发现 @Autowired 就从容器里取出来，反射塞进去
 *
 * 所以 Spring 里你从来没写过 "new UserService()"，
 * 对象是容器用反射造的，依赖也是容器用反射注入的。
 */
public class MiniIocContainer {

    /** 单例池：Bean 名称 -> Bean 实例。Spring 里叫 singletonObjects */
    private final Map<String, Object> singletonPool = new HashMap<>();

    /** 第 1+2 步：注册并实例化（真实 Spring 是扫描 classpath 找到这些类） */
    public void register(Class<?>... componentClasses) {
        for (Class<?> clazz : componentClasses) {
            // 检查标签：这个类贴了 @Component 吗？
            if (!clazz.isAnnotationPresent(Component.class)) {
                throw new IllegalArgumentException(clazz.getSimpleName() + " 没有贴 @Component 注解");
            }
            try {
                // 反射实例化：等价于 clazz.newInstance()，但这个写法能处理更多场景
                Object instance = clazz.getDeclaredConstructor().newInstance();

                // Bean 名称：注解里指定了就用指定的，否则用类名首字母小写（Spring 的默认规则）
                Component comp = clazz.getAnnotation(Component.class);
                String beanName = comp.value().isEmpty()
                        ? clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1)
                        : comp.value();
                singletonPool.put(beanName, instance);
                System.out.println("[容器] 实例化 Bean：" + beanName + " -> " + clazz.getSimpleName());

            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("实例化失败：" + clazz.getSimpleName(), e);
            }
        }
        // 所有 Bean 都创建完，再统一做依赖注入（真实 Spring 也分这两步，处理循环依赖）
        injectDependencies();
    }

    /** 第 3 步：依赖注入 —— 遍历每个 Bean 的字段，@Autowired 的字段从容器取值塞入 */
    private void injectDependencies() {
        for (Object bean : singletonPool.values()) {
            for (Field field : bean.getClass().getDeclaredFields()) {
                if (!field.isAnnotationPresent(Autowired.class)) {
                    continue;                       // 没贴 @Autowired 的字段不管
                }
                Object dependency = getBean(field.getType());
                try {
                    field.setAccessible(true);      // 突破 private！
                    field.set(bean, dependency);    // 等价于 bean.field = dependency
                    System.out.println("[容器] 注入依赖：" + field.getType().getSimpleName()
                            + " -> " + bean.getClass().getSimpleName());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("注入失败：" + field.getName(), e);
                }
            }
        }
    }

    /** 按类型获取 Bean：遍历单例池找类型匹配的（真实 Spring 用更高效的类型索引） */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        for (Object bean : singletonPool.values()) {
            if (type.isInstance(bean)) {
                return (T) bean;
            }
        }
        throw new RuntimeException("容器里没有类型为 " + type.getSimpleName() + " 的 Bean");
    }
}
