package com.dum.demo.Controller;

import com.dum.demo.service.IDemoService;
import com.dum.mvcframework.annotation.DumAutowired;
import com.dum.mvcframework.annotation.DumController;
import com.dum.mvcframework.annotation.DumRequestMapping;
import com.dum.mvcframework.annotation.DumRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Auther : Dumpling
 * @Description
 **/
@DumController
@DumRequestMapping("glaway/demo")
public class DemoController {
    @DumAutowired
    private IDemoService demoService;
    @DumRequestMapping("query")
    public void query(HttpServletRequest req, HttpServletResponse resp,
                      @DumRequestParam("name") String name)
    {
        String result = demoService.get(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @DumRequestMapping("add")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @DumRequestParam("a") Integer a,@DumRequestParam("b") Integer b)
    {
        try {
            resp.getWriter().write(a.toString()+ "+" + b.toString() + "=" + (a+b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
