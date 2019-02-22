package com.st.event.factory;



import com.st.event.EventInitializationManager;
import com.st.event.proxy.EventProviderProxy;
import java.lang.reflect.Proxy;

/**
 * @author yhxst
 * @date 2019-02-22
 * 动态代理对象工厂
 */
public class EventProxyFactory<T> {

    private final Class<T> providerInterface;

    public EventProxyFactory(Class<T> providerInterface) {
        this.providerInterface = providerInterface;
    }

    //获得动态代理对象
    @SuppressWarnings("unchecked")
    public T newInstance(EventInitializationManager manager){
        return (T) Proxy.newProxyInstance(providerInterface.getClassLoader(),new Class[]{providerInterface},new EventProviderProxy<T>(manager));
    }

    public Class<T> getProviderInterface() {
        return providerInterface;
    }
}
