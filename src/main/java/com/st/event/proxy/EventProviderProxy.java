package com.st.event.proxy;


import com.st.event.anno.EventTrigger;
import com.st.event.EventInitializationManager;
import org.springframework.lang.UsesJava7;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author yhxst
 * @date 2019-02-22
 * provider的动态代理对象
 */
public class EventProviderProxy<T> implements InvocationHandler {
    private EventInitializationManager providerMethod;

    public EventProviderProxy(EventInitializationManager manager) {
        providerMethod = manager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())){
                return method.invoke(this,args);
            }else if (isDefaultMethod(method)){
                return invokeDefaultMethod(proxy,method,args);
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
        EventTrigger eventTrigger = method.getAnnotation(EventTrigger.class);
        if (eventTrigger == null)
            return null;
        //使用代理方法执行
        providerMethod.execute(eventTrigger.triggerClass(),method,args);
        return null;
    }


    @UsesJava7
    private Object invokeDefaultMethod(Object proxy, Method method, Object[] args)
            throws Throwable {
        final Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                .getDeclaredConstructor(Class.class, int.class);
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        final Class<?> declaringClass = method.getDeclaringClass();
        return constructor
                .newInstance(declaringClass,
                        MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
                                | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC)
                .unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
    }


    private boolean isDefaultMethod(Method method) {
        return (method.getModifiers()
                & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC
                && method.getDeclaringClass().isInterface();
    }
}
