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
    <title>flow_compiler</title>

    <script type="text/javascript" src="<%=basePath %>/library/js/flow/mcgfn.js?_v=${version}"></script>
    <script type="text/javascript" src="<%=basePath %>/library/js/flow/addition.js?_v=${version}"></script>
    <script>
        var seed = Math.random();
        var text = "";
        $(document).ready(function () {
            $("#select").change(function() { // TODO: 2019/12/6 15:27 not react if select the same file
                text = "";
                var file = this.files[0];
                if (file == undefined || file == " ") {
                    $("#code").text("");
                    $("#code").children(" p ").each(function () {
                        $(this).remove();
                    })
                }
                var reader = new FileReader();
                reader.readAsText(file);
                reader.onload = function () {
                    var strList = this.result.split("\n");
                    var len = strList.length;
                    $("#code").children(" p ").each(function () {
                        $(this).remove();
                    })
                    for(var i = 0 ;i <len ; i++){
                        text += strList[i];
                         $("#code").append("<p>" +strList[i]+"</p>");
                    }
                };
            });
            $("#compiler").bind("click",function () {
                var form = $("<form>");
                form.attr("style", "display:none");
                form.attr("method", "post");
                form.attr("action", baseUrl + "/tool/compiler");
                var flowIdInput = $("<input>");
                flowIdInput.attr("type","hidden");
                flowIdInput.attr("name","text");
                flowIdInput.attr("value",text);
                alert(text);
                form.append(flowIdInput);
                flowIdInput = $("<input>");
                flowIdInput.attr("type","hidden");
                flowIdInput.attr("name","random");
                flowIdInput.attr("value",seed);
                form.append(flowIdInput)
                $("#compiler").append(form);

                form.submit();
                form.remove();
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
    <div style="width: 100% ;height: 80% ;background: #5bc0de; clear: both; overflow: scroll" id = "code" >
        <p style="width: 100% ;height:100% "  name = "code"></p>
    </div>
</div>
<!-- 主面body结束 -->
</body>
</html>