package com.nanjiabawa.reflection.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解：标记"这个类是一个组件，请容器帮我管理"
 *
 * 注解本身只是个"标签"，自己不会执行任何代码；
 * 真正让它生效的是反射 —— 容器扫描类时用 isAnnotationPresent 检查有没有贴这个标签。
 *
 * 两个必写元注解：
 *   @Retention(RUNTIME)  ：保留到运行期！否则反射读不到（默认只保留到编译期，这是新手最常踩的坑）
 *   @Target(TYPE)        ：只能贴在类/接口上
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {

    /** 可选的 Bean 名称，比如 @Component("userService")，不填就默认用类名 */
    String value() default "";
}
