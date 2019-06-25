package com.dum.demo.service.impl;

import com.dum.demo.service.IDemoService;
import com.dum.mvcframework.annotation.DumService;

/**
 * @Auther : Dumpling
 * @Description
 **/
@DumService
public class DemoService implements IDemoService {
    @Override
    public String get(String name) {
        return "My Name Is " + name;
    }
}
