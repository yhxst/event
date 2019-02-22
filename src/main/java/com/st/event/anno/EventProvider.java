package com.st.event.anno;

import java.lang.annotation.*;

/**
 * @author yhxst
 * @date 2019-02-22
 * 事件提供者注解，事件提供者是接口，用来触发事件
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.TYPE})
public @interface EventProvider {
}
