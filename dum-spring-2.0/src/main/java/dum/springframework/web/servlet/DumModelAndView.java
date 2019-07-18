package dum.springframework.web.servlet;

import java.util.Map;

/**
 * @Auther : Dumpling
 * @Description
 **/
public class DumModelAndView {
    private String viewName;
    private Map<String,?> model;

    public DumModelAndView(String viewName) { this.viewName = viewName; }

    public DumModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

//    public void setViewName(String viewName) {
//        this.viewName = viewName;
//    }

    public Map<String, ?> getModel() {
        return model;
    }

//    public void setModel(Map<String, ?> model) {
//        this.model = model;
//    }
}
