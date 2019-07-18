package com.dum.spring.demo.action;

import com.dum.spring.demo.service.TestService;
import dum.springframework.annotation.DumAutowired;
import dum.springframework.annotation.DumController;

/**
 * @Auther : Dumpling
 * @Description
 **/
@DumController
public class TestAction {
    @DumAutowired
    TestService service;
}
