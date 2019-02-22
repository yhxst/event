package com.st.event.factory;

import com.st.event.EventInitializationManager;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author yhxst
 * @date 2019-02-22
 * provider bean创建工厂，使用动态代理注册对象
 */
public class EventProviderFactoryBean<T> implements FactoryBean<T> {
    private Class<T> clz;

    private EventInitializationManager manager;

    public EventProviderFactoryBean(Class<T> clz, EventInitializationManager manager) {
        this.clz = clz;
        this.manager = manager;
    }

    @Override
    public T getObject() throws Exception {
        //使用代理工厂获取对象
        return (T) new EventProxyFactory<T>(clz).newInstance(manager);
    }

    @Override
    public Class<?> getObjectType() {
        return clz;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
