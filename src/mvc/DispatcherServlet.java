package mvc;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;


public class DispatcherServlet extends HttpServlet {

    //来一个小弟作为属性
    private Handler handler = new Handler();


    //init方法 标识当前这个对象的创建
    public void init(ServletConfig config){
        boolean flag = handler.loadPropertiesFile();
        String packageName = null;
        //文件有可能是不存在的  或者文件中只有一个信息 告知需要扫描的包
        if(!flag){//根本没有文件
            packageName = config.getInitParameter("scanPackage");
        }else {
            packageName = handler.getScanPackageName();
        }
        handler.scanAnnotation(packageName);
    }


    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            //0 通过request获取请求的类名  uri     request.getRequestURL();
            String uri = request.getRequestURI();
            //1 找1号小弟 负责解析url
            String requestContent = handler.parseUrlName(uri);
            //  通过request获取请求的参数  method      request.getParameter();
            String methodName = request.getParameter("method");
            if(methodName==null){
                methodName = requestContent.substring(0,requestContent.indexOf("."));
            }
            //以下开始找小弟
            //2 找2号小弟 帮助找到obj对象
            Object obj = handler.findObject(requestContent);
            //3 找3号小弟 通过obj对象找到对象里面的方法
            Method method = handler.findMethod(obj,methodName);
            //4 找4号小弟 处理方法上的参数DI
            //做某一个方法的解析 将方法执行所需要的参数注入进去
            Object[] finalParamValue = handler.injectionParameters(method,request,response);
            //5 执行方法
            Object methodResult = method.invoke(obj,finalParamValue);
            //6 找5号小弟 处理方法执行完毕后的返回结果（响应 转发路径 重定向路径 返回对象JSON）
            //handler.parseResponseContent(,request,response);
            handler.finalResolver(obj,method,methodResult,request,response);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //==============================================================================================================

//    public void service(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
//
//        try {
//            //1 先找到类名
//            String uri = request.getRequestURI();
//            String urlName = uri.substring(uri.lastIndexOf("/")+1,uri.indexOf("."));
//            System.out.println(urlName);
//            //2 通过缓存来寻找 类名对应的类全名
//            String classFullName = realClassNameMap.get(urlName);
//            //2 再找到方法名
//            String methodName = request.getParameter("method");
//            System.out.println(methodName);
//            //3 反射获取方法
//            //先找类
//            Class clazz = Class.forName(classFullName);
//            //找方法
//            Method method = clazz.getMethod(methodName,HttpServletRequest.class,HttpServletResponse.class);
//            //4 反射让方法执行
//            Object obj = objectMap.get(urlName);
//            if (obj == null){
//                obj = clazz.newInstance();
//                objectMap.put(urlName,obj);
//            }
//            String result = (String)method.invoke(obj,request,response);
//            //4 处理返回值
//            request.getRequestDispatcher(result).forward(request,response);
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }


//==================================================================================================================================
//    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        try {
//            //1 接收请求名字（是一个方法名）约定优于配置
//            //StringBuffer sb = request.getRequestURL();//统一资源定位器  ip；port/项目/资源
//            String uri = request.getRequestURI();//统一资源标识符   /项目/资源
//            //System.out.println(uri);
//            uri = uri.substring(uri.lastIndexOf("/")+1);
//            System.out.println(uri);
//            //2 根据请求的方法名 寻找方法
//            Class clazz = this.getClass();
//            Method method = clazz.getMethod(uri, HttpServletRequest.class, HttpServletResponse.class);
//
//            //3 让那个方法执行
//            method.invoke(this,request,response);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


}
