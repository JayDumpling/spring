package com.dum.spring.demo.action;

import com.dum.spring.demo.service.IModifyService;
import com.dum.spring.demo.service.IQueryService;
import dum.springframework.annotation.DumAutowired;
import dum.springframework.annotation.DumController;
import dum.springframework.annotation.DumRequestMapping;
import dum.springframework.annotation.DumRequestParam;
import dum.springframework.web.servlet.DumModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther : Dumpling
 * @Description
 **/
@DumController
@DumRequestMapping("/web")
public class MyAction {

    @DumAutowired
    IModifyService modifyService;
    @DumAutowired
    IQueryService queryService;

    @DumRequestMapping("/query.json")
    public DumModelAndView query(HttpServletRequest request, HttpServletResponse response,
                                @DumRequestParam("name") String name){
        String result = queryService.query(name);
        return out(response,result);
    }

    @DumRequestMapping("/add*.json")
    public DumModelAndView add(HttpServletRequest request, HttpServletResponse response,
                               @DumRequestParam("name") String name, @DumRequestParam("addr") String addr){
        String result = null;
        try {
            result = modifyService.add(name,addr);
            return out(response,result);
        } catch (Exception e) {
//			e.printStackTrace();
            Map<String,Object> model = new HashMap<String,Object>();
            model.put("detail",e.getMessage());
//			System.out.println(Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));
            model.put("stackTrace", Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]",""));
            return new DumModelAndView("500",model);
        }

    }


    private DumModelAndView out(HttpServletResponse resp, String str){
        try {
            resp.getWriter().write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
