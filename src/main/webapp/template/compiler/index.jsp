<%--
  Created by IntelliJ IDEA.
  User: UDean
  Date: 2019/11/29
  Time: 15:33
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path;
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width">
    <link rel="shortcut icon" type="image/png" href="../../img/favicon.png">
    <title>mcg-helper研发助手</title>

    <script type="text/javascript" src="<%=basePath %>/library/js/flow/mcgfn.js?_v=${version}"></script>
    <script type="text/javascript" src="<%=basePath %>/library/js/flow/addition.js?_v=${version}"></script>
    <script>
        function getObjectURL(file){
            if(file == "" || file == undefined || file == NaN){
                alert("file path error");
                return;
            }
            if(window.createObjectURL != undefined){
                return window.createObjectURL(file);
            }
            if(window.webkitURL.createObjectURL != undefined){
                return window.webkitURL.createObjectURL(file);
            }
        }
        $(document).ready(function () {
            $("#select").change(function() {
                var file = this.files[0];
                if (file == undefined || file == " ") {
                $("#code").text("");
                }
                var reader = new FileReader();
                reader.readAsText(file);
                reader.onload = function () {
                    $("#code").text(this.result);
                };
            });
            $("#compiler").bind("click",function () {
               var target = "target.c";
            });
        });

    </script>
</head>

<body>
<!-- 主面body开始 -->
<div id="mcg_body" class="container-fluid autoHeight" >
    <div class="row autoHeight" style="width:30% ; height: 20%">
        <div>
            <input id="select"  type="file" style="float:left ;" >select
        </div>
        <%--                        output address--%>
        <div>
<%--            <input id = "output" type="file" directory >...--%>
            <button id = "compiler" style="float: left "  >compiler</button>
        </div>
    </div>
    <%--            code preview--%>
    <div style="width: 100% ;height: 80% ;background: #5bc0de; clear: both; overflow: scroll" >
        <p style="width: 100% ;height:100% " id = "code" name = "code"></p>
    </div>
</div>
<!-- 主面body结束 -->
</body>
</html>
