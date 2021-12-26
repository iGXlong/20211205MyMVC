<%--
  Created by IntelliJ IDEA.
  User: hp
  Date: 2021/12/5
  Time: 20:55
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>$Title$</title>
  </head>
  <body>
    <a href="AtmController.do?method=login&name=zzt&pass=123">模拟ATM登录功能</a><br>
    <a href="AtmController.do?method=query&name=zzt&pass=123">模拟ATM查询功能</a><br>
    <a href="kindquery.do?name=zzt&pass=123">模拟shopping的功能点1（查询种类）</a><br>
    <a href="kindinsert.do?name=zzt&pass=123">模拟shopping的功能点2（种类添加）</a><br>

    <hr>
    <form action="login.do" method="post">
      account:<input type="text" name="name" value=""><br>
      password:<input type="password" name="pass" value=""><br>
      <input type="submit" value="login">
    </form>
  </body>
</html>
