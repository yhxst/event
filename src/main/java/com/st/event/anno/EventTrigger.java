package com.st.event.anno;

import java.lang.annotation.*;

/**
 * @author yhxst
 * @date 2019-02-22
 * 事件触发器，需要声明触发器类和对应的方法，方法参数需要对应
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.METHOD})
public @interface EventTrigger {
    Class<?> triggerClass();

    String method();
}
