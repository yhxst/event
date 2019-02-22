package com.st.event;

import com.st.event.test.provider.TestEventProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EventApplicationTests {
    @Autowired
    TestEventProvider testEventProvider;

    @Test
    public void eventTest() {
        testEventProvider.orderFinished("aaaa","bbbb");
    }

}
