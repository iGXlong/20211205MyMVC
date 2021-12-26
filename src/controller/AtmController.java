package controller;

import domain.User;
import mvc.*;
import service.AtmService;

import java.util.List;

@RequestMapping("AtmController.do")
@SessionAttribute(value = {"name"})
public class AtmController{

    private AtmService service = new AtmService();
    @RequestMapping("login.do")
    public ModelAndView login(User user){
        ModelAndView mv = new ModelAndView();
        System.out.println("我是login的controller");
        //1 接收请求的参数 --参数列表
        //2 调用业务层的方法
        String result = service.login(user);
        //3 根据result做响应
        if("success".equals(result)){
            mv.addObject("name",user.getName());
            mv.setViewName("welcome.jsp");
        }else {
            mv.addObject("result",result);
            mv.setViewName("index.jsp");
        }
        return mv;
        //response.getWriter().write("<html>");//直接 给响应
        //request.getRequestDispatcher("welcome.jsp").forward(request,response);//转发
        //response.sendRedirect("");//重定向
        //return "welcome.jsp";
    }
    @RequestMapping("query.do")
    @ResponseBody
    public List<User> query(User user){
        System.out.println("我是query的controller");
        System.out.println(user);
        return null;
    }


}
