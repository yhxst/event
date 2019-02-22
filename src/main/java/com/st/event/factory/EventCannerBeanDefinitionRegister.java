package com.st.event.factory;


import com.st.event.EventInitializationManager;
import com.st.event.anno.EventScan;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author yhxst
 * @date 2019-02-22
 * 将eventInitializationManage注入容器用的类
 */
public class EventCannerBeanDefinitionRegister implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EventScan.class.getName(), false);
        if(annotationAttributes != null) {
            BeanDefinition beanDefinition = getBeanDefinition((String[])annotationAttributes.get("value"));
            registry.registerBeanDefinition("com.st.event.eventInitializationManage", beanDefinition);
        }
    }

    private BeanDefinition getBeanDefinition(String[] values) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(EventInitializationManager.class);
        beanDefinition.setSource(null);
        beanDefinition.getPropertyValues().add("packages",values);
        beanDefinition.setRole(2);
        return beanDefinition;
    }
}
