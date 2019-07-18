package com.dum.spring.demo;

import com.dum.spring.demo.action.TestAction;
import dum.springframework.context.DumApplicationContext;

/**
 * @Auther : Dumpling
 * @Description
 **/
public class Test {
    public static void main(String[] args) {
        DumApplicationContext context = new DumApplicationContext("application.properties");
        try {
            Object object = context.getBean(TestAction.class);
            System.out.println(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
