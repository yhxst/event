package com.st.event.test.trigger;

public interface OrderFinishTrigger {
    void process(String orderId, String empId);
}
