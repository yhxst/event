package com.st.event.test.listener;

import com.st.event.anno.EventListener;
import com.st.event.test.trigger.OrderFinishTrigger;

@EventListener
public class TestOrderFinishDealGiveMoneyListener implements OrderFinishTrigger {
    @Override
    public void process(String orderId, String empId) {
        System.out.println("订单完成发工钱啦：orderId:"+orderId+";empId:"+empId);
    }
}
