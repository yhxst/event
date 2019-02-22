package com.st.event.factory;


import com.st.event.anno.EventListener;
import com.st.event.anno.EventProvider;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * @author yhxst
 * @date 2019-02-22
 * 用于扫描包下的EventProvider和EventListener
 */
public class EventBeanScanner extends ClassPathScanningCandidateComponentProvider {

    public EventBeanScanner(){
        super(false);
        addIncludeFilter(new AnnotationTypeFilter(EventProvider.class));
        addIncludeFilter(new AnnotationTypeFilter(EventListener.class));
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return true;
    }
}
