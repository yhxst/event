package com.st.event;

import com.st.event.anno.EventListener;
import com.st.event.anno.EventProvider;
import com.st.event.anno.EventTrigger;
import com.st.event.enums.ListenerTriggeriOpportunityEnum;
import com.st.event.factory.EventBeanScanner;
import com.st.event.factory.EventProviderFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ResourceLoader;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author yhxst
 * @date 2019-02-22
 * 事件的管理类，职责为在spring初始化单实例bean之前
 * 主要职责为
 * 1.把provider动态代理对象和listener对象扫描出来并注册到spring容器中
 * 2.在容器初始化完成后，构建listener，trigger的映射关系
 * 3.提供触发事件的统一方法execute()
 */
public class EventInitializationManager implements ApplicationListener<ContextRefreshedEvent>, BeanFactoryPostProcessor, ResourceLoaderAware {
    private ApplicationContext applicationContext;

    //保存格式为：trigger接口类-->{trigger触发执行的method-->[listener对象]}
    private Map<Class<?>, Map<Method,Set<Object>>> PROVIDER_LISTENER_SYNC_MAP = new HashMap<>(); //同步执行队列
    private Map<Class<?>, Map<Method,Set<Object>>> PROVIDER_LISTENER_ASYNC_MAP = new HashMap<>(); //异步执行队列

    private ResourceLoader resourceLoader;

    // @EventProvider接口类所在的包
    private String[] packages = {};

    //保存所有事件提供者类
    private List<Class<?>> eventProviders = new ArrayList<>();
    //保存所有监听者类
    private List<Class<?>> eventListeners = new ArrayList<>();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Set<BeanDefinition> beans = scanInterfaceDef();
        DefaultListableBeanFactory bf = (DefaultListableBeanFactory)beanFactory;
        registerProxyBean(beans, bf);
    }

    /**
     * 将扫描到的provider和listener注册到spring容器中
     * @param beans
     * @param beanFactory
     */
    private void registerProxyBean(Set<BeanDefinition> beans, DefaultListableBeanFactory beanFactory){
        for (BeanDefinition bd : beans){
            Class<?> clazz;
            try {
                clazz = Class.forName(bd.getBeanClassName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            }

            if (clazz.isAnnotationPresent(EventProvider.class)){
                eventProviders.add(clazz);

                String beanName = getBeanName(clazz);
                //直接实例化并注册上provider的FactoryBean
                EventProviderFactoryBean obj = new EventProviderFactoryBean<>(clazz,this);
                beanFactory.registerSingleton(beanName,obj );
            }else if (clazz.isAnnotationPresent(EventListener.class)){
                eventListeners.add(clazz);
                //将listener的信息注册到spring容器中，初始化交给spring来完成
                BeanDefinition listenerBeanefinition = new RootBeanDefinition();
                listenerBeanefinition.setBeanClassName(clazz.getName());

                beanFactory.registerBeanDefinition(getBeanName(clazz),listenerBeanefinition);
            }
            //有可能是被@Component修饰的其他接口或类
        }
    }

    private String getBeanName(Class<?> clazz) {
        String name = clazz.getSimpleName();
        //按首字母小写的形式生成bean名
        name = name.substring(0,1).toLowerCase() + name.substring(1);
        return name;
    }

    /**
     * 扫描各个包下的provider和listener
     * @return
     */
    private Set<BeanDefinition> scanInterfaceDef() {
        ClassPathScanningCandidateComponentProvider componentProvider = new EventBeanScanner();
        componentProvider.setResourceLoader(resourceLoader);

        Set<BeanDefinition> beans = new LinkedHashSet<>();
        for (String pkg:packages){
            beans.addAll(componentProvider.findCandidateComponents(pkg));
        }
        return beans;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 监听容器初始化完毕时间，开始映射监听
     * @param event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.applicationContext = event.getApplicationContext();

        System.out.println("读取到所有的事件提供者：:"+ eventProviders);
        if (eventProviders == null || eventProviders.isEmpty())
            return;

        for (Class<?> providerClz : eventProviders){
            initializationProvider(providerClz);
        }
    }

    private void initializationProvider(Class<?> providerClass) {
        Method[] methods = providerClass.getMethods();
        for (Method method:methods){
            if (!method.isAnnotationPresent(EventTrigger.class))
                continue;
            List<Object> asynList = new ArrayList<>();
            List<Object> synList = new ArrayList<>();

            //获得对应的触发器类
            EventTrigger eventTrigger = method.getAnnotation(EventTrigger.class);
            Class<?> triggerClass = eventTrigger.triggerClass();
            String methodName = eventTrigger.method();
            //通过反射获取监听器的方法
            Method triggerMethod;
            try {
                triggerMethod = triggerClass.getMethod(methodName,method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("没有找到对应的方法,trigger:["+triggerClass+"],method:["+methodName+"],param:["+method.getParameterTypes()+"]");
            }
            System.out.println(eventListeners);
            for (Class<?> listenerClz : eventListeners){
                Object listener = applicationContext.getBean(listenerClz);
                Class<?> listenerClass = listener.getClass();

                if (!isImplOf(listenerClass,triggerClass)){
                    continue;
                }
                //根据监听器注解配置的执行方法，放入相应同步异步的map
                EventListener eventListener = listenerClass.getAnnotation(EventListener.class);
                if (ListenerTriggeriOpportunityEnum.ASYNCHRONOUS.equals(eventListener.type())){
                    asynList.add(listener);
                }else {
                    synList.add(listener);
                }
            }
            addListenerToMap(triggerClass,triggerMethod,asynList,synList);
        }
    }

    /**
     * 将所有的映射关系都交给Manager管理
     * @param trigger
     * @param asynList
     * @param synList
     */
    private void addListenerToMap(Class<?> trigger, Method triggerMethod, List<Object> asynList, List<Object> synList) {
        Map<Method,Set<Object>> asynListenerMap = PROVIDER_LISTENER_ASYNC_MAP.get(trigger);
        Map<Method,Set<Object>> synListenerMap = PROVIDER_LISTENER_SYNC_MAP.get(trigger);
        if (asynListenerMap == null){
            asynListenerMap = new HashMap<>();
            PROVIDER_LISTENER_ASYNC_MAP.put(trigger,asynListenerMap);
        }
        if (synListenerMap == null){
            synListenerMap = new HashMap<>();
            PROVIDER_LISTENER_SYNC_MAP.put(trigger,synListenerMap);
        }

        Set<Object> asynListeners = asynListenerMap.get(triggerMethod);
        Set<Object> synListeners = synListenerMap.get(triggerMethod);
        if (asynListeners == null){
            asynListeners = new HashSet<>();
            asynListenerMap.put(triggerMethod,asynListeners);
        }
        if (synListeners == null){
            synListeners = new HashSet<>();
            synListenerMap.put(triggerMethod,synListeners);
        }

        asynListeners.addAll(asynList);
        synListeners.addAll(synList);
    }

    /**
     * provider 统一触发的方法
     * @param triggerClass
     * @param param
     */
    public void execute(Class<?> triggerClass,Method providerTriggerMethod, Object... param) throws NoSuchMethodException {
        String methodName = providerTriggerMethod.getAnnotation(EventTrigger.class).method();
        Method method = triggerClass.getMethod(methodName, providerTriggerMethod.getParameterTypes());
        Map<Method,Set<Object>> asynListeners = PROVIDER_LISTENER_ASYNC_MAP.get(triggerClass);
        if (asynListeners != null && !asynListeners.isEmpty()){
            Set<Object> asynListenerList = asynListeners.get(method);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runListener(asynListenerList,method,param);
                }
            }).start();
        }

        Map<Method,Set<Object>> syncListeners = PROVIDER_LISTENER_SYNC_MAP.get(triggerClass);
        if (syncListeners != null && !syncListeners.isEmpty()){
            Set<Object> syncListenerList = syncListeners.get(method);
            runListener(syncListenerList,method,param);
        }
    }

    private void runListener(Set<Object> listeners,Method method,Object... param){
        if (listeners == null)
            return;
        for (Object listener : listeners){
            try {
                method.invoke(listener,param);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private boolean isImplOf(Class<?> listenerClass, Class<?> triggerClass) {
        Class<?>[] classes = listenerClass.getInterfaces();
        for (Class<?> c:classes){
            if (c.equals(triggerClass))
                return true;
        }
        return false;
    }

    /**
     * 创建Manager对象时赋值packages用
     * @param packages
     */
    public void setPackages(String[] packages) {
        this.packages = packages;
    }
}
