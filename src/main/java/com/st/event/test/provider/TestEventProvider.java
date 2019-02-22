package com.st.event.test.provider;


import com.st.event.anno.EventProvider;
import com.st.event.anno.EventTrigger;
import com.st.event.test.trigger.OrderFinishTrigger;

@EventProvider
public interface TestEventProvider {

    @EventTrigger(triggerClass = OrderFinishTrigger.class,method = "process")
    void orderFinished(String orderId, String empId);
}
