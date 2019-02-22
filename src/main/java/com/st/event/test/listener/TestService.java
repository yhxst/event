package com.st.event.test.listener;

import org.springframework.stereotype.Service;

@Service
public class TestService {
    public void showMe(String param){
        System.out.println("chuancan:"+param);
    }
}
