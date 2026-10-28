package com.nanjiabawa.reflection.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解：标记"这个字段需要容器注入依赖"
 *
 * 等价于 Spring 的 @Autowired —— 你在项目里天天用，
 * 它的执行者就是下面 MiniIocContainer 里那几行反射代码。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)          // 只能贴在字段上
public @interface Autowired {
}
