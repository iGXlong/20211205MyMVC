package controller;

import mvc.RequestMapping;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

public class ShoppingController {

    @RequestMapping("kindquery.do")
    public String kindquery(HashMap<String,String> map){
        System.out.println("shopping中的kindquery方法");
        System.out.println(map);
        return "welcome.jsp";
    }

    @RequestMapping("kindinsert.do")
    public String kindinsert(HttpServletRequest request,HttpServletResponse response){
        System.out.println("shopping中的kindinsert方法");
        System.out.println(request.getParameter("name"));
        System.out.println(request.getParameter("pass"));
        return "welcome.jsp";
    }

}
