package com.st.event.anno;


import com.st.event.enums.ListenerTriggeriOpportunityEnum;

import java.lang.annotation.*;

/**
 * @author yhxst
 * @date 2019-02-22
 * 事件监听者注解，提供同步监听和异步监听两种方式
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.TYPE})
public @interface EventListener {
    ListenerTriggeriOpportunityEnum type() default ListenerTriggeriOpportunityEnum.ASYNCHRONOUS; // 默认异步触发
}
