package mvc;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

/**
 * 这是一个新的类
 * 这个类没有其他含义
 * 只是为了让DispatcherServlet类更简洁一些
 */

public class Handler {

    //属性 用来存储 请求名--真实类全名的对应关系  读取文件的缓存机制
    private Map<String,String> realClassNameMap = new HashMap<>();
    //属性 用来存储 controller类和类对象 因为当前类对象是单例的 这个集合只要不new新的肯定是单例
    private Map<String,Object> objectMap = new HashMap<>();//类名 类对象
    //属性 用来存储 某一个controller类和里面的全部方法
    private Map<Object,Map<String,Method>> objectMethodMap = new HashMap<>();

    //属性---用来存放 请求方法名+类名对应关系
    private Map<String,String> methodandRealClassNameMap = new HashMap<>();

    String getScanPackageName(){
        return this.realClassNameMap.get("scanPackage");
    }


    //0号小弟 读取文件的方法
    boolean loadPropertiesFile(){
        boolean flag = true;//表示文件是存在的
        try {
            // 读取配置文件--缓存map
            Properties properties = new Properties();
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ApplicationContext.properties");
            properties.load(inputStream);
            Enumeration en = properties.propertyNames();
            while (en.hasMoreElements()){
                String key = (String)en.nextElement();
                String value = properties.getProperty(key);
                realClassNameMap.put(key,value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            flag = false;
        }
        return flag;
    }
    //0号小弟 负责在init方法执行的时候 加载类中的注解 请求-类-方法 对应关系
    void scanAnnotation(String packageName){
        if(packageName==null){//包名字不存在
            return;
        }
        //包名字存在
        String[] packages = packageName.split(",");
        //解析数组中的每一个包名字 当然也有可能就一个名字
        for(String name : packages){
            //循环每一次获取一个包名字
            //想要找到真正的类  扫描类上面的注解
            //需要把文件拿过来 加载入内存 扫描内存中那个类上的注解
            //通过类加载器ClassLoader 读取上面包名对应的路径 获取到一个文件所在的url
            URL url = Thread.currentThread().getContextClassLoader().getResource(name.replace(".","\\"));
            if(url==null){
                continue;
            }
            //根据获取url定位一个真实文件路径
            String packagePath = url.getPath();
            File packageFile = new File(packagePath);
            //上面那个packageFile代表的是controller文件夹的真身 要的是里面所有子文件
//            File[] files = packageFile.listFiles(new FileFilter() {
//                public boolean accept(File file) {
//                    if(file.isFile() && file.getName().endsWith("class")){
//                        return true;
//                    }
//                    return false;
//                }
//            });
            //表达式
            File[] files = packageFile.listFiles(file -> {
                    if(file.isFile() && file.getName().endsWith("class")){
                        return true;
                    }
                    return false;
                }
            );
            //===========================================================
            //获取到了一个files的数组 数组中存储的每一个file对象是我们找到的Controller类
            for(File file : files){
                //每一次循环 得到一个file对象 -->反射加载类
                //利用刚才的包名 和 此时file对象的文件名
                String simpleName = file.getName();
                String fullName = packageName +"."+ simpleName.substring(0,simpleName.indexOf("."));
                try {
                    //得到类全名 就可以反射了
                    Class clazz = Class.forName(fullName);
                    //扫面类上面的注解
                    RequestMapping classAnnotation = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
                    //判断类上面的注解是否存在
                    if(classAnnotation!=null){
                        realClassNameMap.put(classAnnotation.value(),fullName);
                    }
                    //如果上面的方法没有执行 类上面没有注解 用户发送请求 直接定位到方法
                    //-->深入 解析
                    Method[] methods = clazz.getDeclaredMethods();
                    for(Method method : methods){
                        RequestMapping methodAnnotation = method.getAnnotation(RequestMapping.class);
                        //保证这个注解是存在的
                        if(methodAnnotation!=null){
                            //方法的名字和一个 类名字做对应
                            //最终用户发送请求 觉得是在请求方法 想要反射找到方法 需要找到类
                            methodandRealClassNameMap.put(methodAnnotation.value(),fullName);
                        }else{
                            //注解不存在
                            //自定义异常
                            throw new NoSuchMethodException("没有找到对应的方法 请检查注解");
                        }
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    // 1号小弟 负责解析请求名uriName
    String parseUrlName(String uri){
        return uri.substring(uri.lastIndexOf("/")+1);
    }
    //2号小弟 通过类名找到obj对象
    Object findObject(String requestContent) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        //通过请求名找 对应的类对象
        Object obj = objectMap.get(requestContent);
        if(obj == null){
            String fullClassName = realClassNameMap.get(requestContent);
            if (fullClassName == null){
                //有可能是方法名
                //去另一个集合里找
                fullClassName = methodandRealClassNameMap.get(requestContent);
                if(fullClassName == null) {
                    //请求有问题  类不存在
                    //自定义异常
                    throw new ControllerNotFoundException(requestContent + "不存在");
                }
            }
            Class clazz = Class.forName(fullClassName);
            obj = clazz.newInstance();
            objectMap.put(requestContent,obj);
            //------------------------------>>>对象懒加载之后 马上解析对象中的全部方法
            // Map<AtmController,Map<methodName,method>>
            Method[] methods = clazz.getDeclaredMethods();
            Map<String,Method> methodMap = new HashMap<>();//用来存储这个对象中的全部方法
            for (Method method : methods){
                //将一个方法名字和方法对象 存入map集合
                methodMap.put(method.getName(),method);
            }
            objectMethodMap.put(obj,methodMap);
        }
        return obj;
    }
    //3号小弟 负责通过obj对象 找到某个方法
    Method findMethod(Object obj, String methodName) throws NoSuchMethodException {
        return objectMethodMap.get(obj).get(methodName);//重载情况没有做考虑
    }
    //4号小弟 负责分析找到的method 做参数的自动注入
    //先通过request接收参数
    //再将接收的参数交给method执行
    //条件  method request
    //返回值  方法需要的具体参数值 好几个 Object[]
    Object[] injectionParameters(Method method,HttpServletRequest request,HttpServletResponse response) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, java.lang.NoSuchMethodException {
        //解析method 拿到方法参数列表的key
        //参数类型
        //基础类型 Sting int
        //对象类型 domain
        //集合类型 map

        //1 获取method方法中的所有的参数对象
        Parameter[] parameters = method.getParameters();
        //2 严谨的判断
        if (parameters == null || parameters.length == 0){
            return null;
        }
        //3 做一个返回值准备好
        Object[] finalParamValue = new Object[parameters.length];
        //4 解析参数 获取key request取值  存入返回值数组中
        for (int i=0;i<parameters.length;i++){
            //每次获取一个参数
            Parameter parameter = parameters[i];
            //先获取参数前面的注解
            RequestParam paramAnnotation = parameter.getAnnotation(RequestParam.class);
            if (paramAnnotation != null){//有注解 散装的值
                //获取注解中的key值
                String key = paramAnnotation.value();
                String value = request.getParameter(key);
                if(value!=null){
                    //获取当前参数的数据类型
                    Class paramClazz = parameter.getType();
                    //分析参数类型做分支判断
                    if(paramClazz==String.class){
                        finalParamValue[i] = value;
                    }else if(paramClazz == Integer.class || paramClazz == int.class){
                        finalParamValue[i] = new Integer(value);
                    }else if(paramClazz == Float.class || paramClazz == float.class){
                        finalParamValue[i] = new Float(value);
                    }else if(paramClazz == Double.class || paramClazz == double.class){
                        finalParamValue[i] = new Double(value);
                    }

                }
            }else{//原生request 对象 map
                //获取当前参数的数据类型
                Class paramClazz = parameter.getType();
                if (paramClazz.isArray()){
                    //数组  对不起 处理不了
                    throw new ParameterTypeException("方法内数组参数无法处理");
                }else {
                    if (paramClazz==HttpServletRequest.class){
                        finalParamValue[i]=request;continue;
                    }
                    if(paramClazz==HttpServletResponse.class){
                        finalParamValue[i]=response;continue;
                    }
                    if (paramClazz == Map.class || paramClazz == List.class){
                        //传递的是接口 处理不了
                        throw new ParameterTypeException("方法内不能传递接口 请提供具体参数");
                    }
                    //普通的具体对象
                    Object paramObj = paramClazz.newInstance();
                    if(paramObj instanceof Map){
                        //造型成map 存值
                        Map<String,Object> paramMap = (Map)paramObj;
                        //获取全部请求 用请求的key来作为最终map的key
                        Enumeration en = request.getParameterNames();
                        while (en.hasMoreElements()) {
                            String key =(String)en.nextElement();
                            String value = request.getParameter(key);
                            paramMap.put(key,value);
                        }
                        finalParamValue[i] = paramMap;
                    }else if(paramObj instanceof Object){
                        //解析对象中的全部属性  属性名key
                        Field[] fields = paramClazz.getDeclaredFields();
                        for(Field field : fields){
                            field.setAccessible(true);//操作私有属性
                            String key = field.getName();
                            String value = request.getParameter(key);

                            //将这个value放入属性值中
                            //对象类型的构造方法
                            Class fieldType = field.getType();
                            Constructor fieldConstructor = fieldType.getConstructor(String.class);
                            field.set(paramObj,fieldConstructor.newInstance(value));
                            //对象中处理不了Character类型  对象中处理不了对象属性（递归）
                        }
                        finalParamValue[i] = paramObj;
                    }else {
                        throw new ParameterTypeException("未知类型 我处理不了了···");
                    }
                }

            }
        }
        return finalParamValue;
    }

    private void parseModelAndView(Object obj,ModelAndView mv,HttpServletRequest request){
        //从mv对象中把map取出来
        Map<String,Object> mvMap = mv.getAttributeMap();
        //遍历mvMap 存入request中
        Set<String> keys = mvMap.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()){
            String key = it.next();
            Object value = mvMap.get(key);
            //存入request作用域中
            request.setAttribute(key,value);
        }
        //分析以下注解 看是否需要存入session中
        SessionAttribute sattr = obj.getClass().getAnnotation(SessionAttribute.class);
        if(sattr!=null){
            String[] attributeNames = sattr.value();
            if(attributeNames.length!=0){
                HttpSession session = request.getSession();
                for(String attributeName : attributeNames){
                    session.setAttribute(attributeName,mvMap.get(attributeName));
                }
            }
        }

    }

    //5号小弟 负责处理方法的返回值
    //参数 刚才真正controller方法执行完毕的返回值
    //转发 重定向 request response
    //返回值 void
    void parseResponseContent(String viewName,HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
        if(!"".equals(viewName) && !"null".equals(viewName)){
            //redirect:welcome.jsp
            String[] value = viewName.split(":");
            if(value.length == 1){//是一个正常的转发
                request.getRequestDispatcher(viewName).forward(request,response);
            }else {//认为是重定向
                if("redirect".equals(value[0])){
                    response.sendRedirect(value[1]);
                }
            }
        }else {
            System.out.println("不好好玩儿 我不处理");
            throw new ViewNameFormatException("controller响应的viewName不能为空");
        }
    }

    //5号小弟升级了  负责处理方法返回值 不一定是String 也有可能是ModelAndView
    //参数  方法执行的返回值先给我 object类型  request response
    void finalResolver(Object obj,Method method,Object methodResult,HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
        if (methodResult==null){
            return;//证明这个方法不需要框架帮我们做处理
        }
        if(methodResult instanceof ModelAndView){
            //强制类型转换
            ModelAndView mv = (ModelAndView)methodResult;
            //解析mv对象
            this.parseModelAndView(obj,mv,request);
            //解析mv中的viewName字符串
            this.parseResponseContent(mv.getViewName(),request,response);
        }else if (methodResult instanceof ModelAndView){
            //返回字符串  可能表示一个viewName
            //可以获取方法上的注解说明
            ResponseBody responseBody = method.getAnnotation(ResponseBody.class);
            if(responseBody!=null){//有注解 证明返回值是一个数据
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write((String)methodResult);
            }else {//没有注解 证明返回值是一个路径
                this.parseResponseContent((String)methodResult,request,response);
            }
        }else {//返回值是domain对象 List<domain>
            //AJAX+JSON
            ResponseBody responseBody = method.getAnnotation(ResponseBody.class);
            if(responseBody!=null){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("jsonObject",methodResult);
                response.getWriter().write(jsonObject.toJSONString());
            }else {
                //抛出自定义异常
                //返回值不认识 需要添加注解
            }

        }
    }

}
