package com.st.event.anno;


import com.st.event.factory.EventCannerBeanDefinitionRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author yhxst
 * @date 2019-02-22
 * 事件类扫描注解，需要声明在springBoot启动类上才能开启事件机制
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.TYPE})
@Import({EventCannerBeanDefinitionRegister.class})
public @interface EventScan {
    String[] value();
}
