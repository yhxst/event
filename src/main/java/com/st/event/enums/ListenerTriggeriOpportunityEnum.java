package com.st.event.enums;

/**
 * @author yhxst
 * @date 2019-02-22
 */
public enum ListenerTriggeriOpportunityEnum {
    // TODO: 2019-02-22 注意，异步执行目前是直接new Thread方式的，请用线程池或其他异步通知方式实现后再使用异步触发
    SYNCHRONIZE, //同步
    ASYNCHRONOUS; //异步
}
