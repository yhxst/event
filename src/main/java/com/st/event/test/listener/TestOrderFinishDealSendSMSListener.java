package com.st.event.test.listener;


import com.st.event.anno.EventListener;
import com.st.event.test.trigger.OrderFinishTrigger;
import org.springframework.beans.factory.annotation.Autowired;

@EventListener
public class TestOrderFinishDealSendSMSListener implements OrderFinishTrigger {
    @Autowired()
    private TestService testService;

    @Override
    public void process(String orderId, String empId) {
        System.out.println("订单完成发短信啦"+orderId+";empId:"+empId);
        testService.showMe(orderId);
    }
}
