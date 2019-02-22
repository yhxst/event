package com.st.event;

import com.st.event.anno.EventScan;
import com.st.event.test.provider.TestEventProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EventScan({"com.st.event.test"})
public class EventApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(EventApplication.class, args);
        TestEventProvider bean = context.getBean(TestEventProvider.class);
        System.out.println(bean);
    }

}
