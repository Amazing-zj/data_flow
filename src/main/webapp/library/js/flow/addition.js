/---------------test-------------------/
var labelMap = new Map();

/**
 * DESC : delete relative targetMap data
 * DATE : 2020/2/6 14:08
 * AUTHOR : UDEAN
 */
function delTarget(id){
    targetMap.remove();
}

/**
 * DESC : delete reative connector and label when remove node
 * DATE : 2019/2/5 19:33
 * AUTHOR : UDEAN
 */
var tid;
 function removeRelativeConnector(id){
    var list = targetMap.get(id);
    tid = id;
    if(list != undefined && list.length !=0) {
        targetMap.remove(id);
        for (var i = 0; i < list.length; i++) {
            // baseMap.get("instance").detach(instanceMap.get(id + list[i]));
            // delConnectorLabel(id + list[i]);
            instanceMap.remove(list[i].getId() + id);
        }
    }
    list = sourceMap.get(id);
    if(list != undefined && list.length !=0) {
        sourceMap.remove(id);
        for (var i = 0; i < list.length; i++) {
            // baseMap.get("instance").detach(instanceMap.get(id + list[i])); // remove connector line, done in function repaint
            // delConnectorLabel(id + list[i]); // remove label data, when add a new node it done in function resetLabel
            instanceMap.remove(id + list[i].getId() );
        }
    }
 }

/**
 * DESC : repaint connector label when add new node
 * DATE : 2019/12/25 16:33
 * AUTHOR : UDEAN
 */
function setConnectorLabel() {
    var keySet = instanceMap.keySet();
    var tempMap = new Map();
    if(keySet != undefined && keySet.length != 0){
        var value, key;
        for(var i = 0 ; i< keySet.length ; i++){
            key = keySet[i];
            value = instanceMap.get(key);
            if(value != undefined && labelMap.get(key) != undefined ) {
                value.connection.setLabel(labelMap.get(key));
                tempMap.put(key, labelMap.get(key));
            }
        }
    }
    labelMap = tempMap;
}

/**
 * DESC : add label data
 * DATE : 2019/12/25 16:39
 * AUTHOR : UDEAN
 */
function addConnectorLabel(id, label) {
    labelMap.put(id, label);
}

/**
 * DESC : reset label value after repaint nddes and connectors
 * DATE : 2020/1/1 13:26
 * AUTHOR : UDEAN
 */
function resetLabel() {
    setConnectorLabel();
    // real time: invoke setConnectorLabel function
    // test time: use instanceMap keySet and set self defined values
    // var keys = instanceMap.keySet();
    // for (var i = 0; i < keys.length ; i++){
    //     instanceMap.get(keys[i]).connection.setLabel("test");
    // }
}

/**
 * DESC : transform input data array to label string
 * DATE : 2019/12/25 17:18
 * AUTHOR : UDEAN
 */
function transArray2Str(label) {
    if(label == undefined || label == NaN || label.length == 0)
        return "";
    var str = "";
    for(var i = 0 ; i < label.length ; i++){
        str += label[i]+ ",";
    }
    str = str.substr(0,str.length-1);
    return str;
}

/**
 * DESC : delete label data when connector delete
 * DATE : 2019/12/25 16:40
 * AUTHOR : UDEAN
 */
function delConnectorLabel(id) {
    if(labelMap.get(id) != undefined){
        labelMap.remove(id);
    }
}

/**
 * DESC : search file fake position (cache position)
 * DATE : 2019/12/17 11:00
 * AUTHOR : UDEAN
 */
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

function showAllDiv(){
    $("#flowarea").children("div[data-toggle='popover']").each(function (){
        $(this).css("display","inline");
    })
}

function removeConnectDiv(){
    $("#flowarea").children("div[data-toggle='popover']").each(function(){
        $(this).remove();
    })
}

function hideAllDiv(){
    $("#flowarea").children("div[data-toggle='popover']").each(function (){
        $(this).css("display","none");
    })
}

var tempId ;

function setTempId(){
    var keys = elementMap.keySet();
    for(var i = 0 ; i < keys.length ; i++){
        tempId = elementMap.get(keys[i]).getId();
    }
}

function showPopover(id){
    $("#"+id).popover("show");
}

/**
 * DESC : for node tool bar click function
 * DATE : 2020/2/11 12:31
 * AUTHOR : UDEAN
 */
function suspendNode(operate) {
    var id = baseMap.get("selector");
    if(operate == "edit"){
        createNodeHtmlModal(id,null);
    }else if(operate == "delete"){
        removePopover();
        deleteNode(id);
        removeElement(id);
    }
}

/**
 * DESC : delete node cache
 * DATE : 2020/2/11 13:06
 * AUTHOR : UDEAN
 */
function deleteNode(id){
    common.ajax( {
        "url"  : "/common/deleteNode",
        "type" : "GET",
        "data" : "id="+id
    }
    )
}


/**
 * DESC : get backup  node message
 * DATE : 2020/2/11 12:42
 * AUTHOR : UDEAN
 */
function getNodeDataById(id, func) {
    common.ajax({
        url : "/common/getNodeById",
        type : "POST",
        data : "flowId=" + $("#flowSelect").attr("flowId") + "&id="+id
    }, func);
}

/**
 * DESC : init node info
 * DATE : 2020/2/11 12:43
 * AUTHOR : UDEAN
 */
function initNodeModal(id, editor) {
    getNodeDataById(id, function(data) {
        if(data != null && data != "" && data != undefined && data.textProperty != undefined) {
            common.formUtils.setValues(id + "_textForm", data);
        }
        if(data != null && data != undefined && data.textCore != undefined) {
            editor.setValue(data.textCore.source);
        }
    });
}


/-----------finished----------/

function suspendConnector(operate) {
    var id = baseMap.get("selector");
    if (operate == "output") {
        createTextOutputModal(id, null);
    }else if(operate == "input"){
        createConnectorHtmlModal(id, null);
    }else if (operate == "delete") {
        baseMap.get("instance").detach(instanceMap.get(id)); //remove connector
        deleteInput(id); //remove input map data and backup data
        removePopover();
        removeElement(id);//remove binding popover
        removeConnector(id.substr(0,36),id); // remove connector cache data
        delConnectorLabel(id);//remove connector label data
    }
}

/* 在body中创建Modal的html元素
 * id 流程节点id
 * param json对象参数
 * */
function createTextOutputModal(id, param) {
    removePopover();
    if(param == null)
        param = {};
    var option = {};
    var url = "/html/TextOutput";
    option["title"] = "输出数据";
    option["width"] = 1100;

    param["modalId"] = id;
    param["eletype"] ="output";
    param["option"] = option;
    common.showAjaxDialog(url, setDialogBtns(param), createModalCallBack, null, param);
}

function createNodeHtmlModal(id, param){
    removePopover();
    if(param == null)
        param = {};
    var option = {};
    var url = "/html/flowNodeModal";
    option["title"] = "输入数据";
    option["width"] = 1100;

    param["modalId"] = id;
    param["eletype"] ="node";
    param["option"] = option;
    common.showAjaxDialog(url, setDialogBtns(param), createModalCallBack, null, param);
}

function createConnectorHtmlModal(id, param) {
    removePopover();
    if(param == null)
        param = {};
    var option = {};
    var url = "/html/Connector";
    option["title"] = "输入数据";
    option["width"] = 1100;

    param["modalId"] = id;
    param["eletype"] ="connector";
    param["option"] = option;
    common.showAjaxDialog(url, setDialogBtns(param), createModalCallBack, null, param);
}

/**
 * DESC : update data when connector created
 * DATE : 2019/12/25 10:41
 * AUTHOR : UDEAN
 */
function addConnector(source, target) {
    var data = {};
    data['sourceId'] = source;
    data['targetId'] = target;
    common.ajax({
            url:"/connect/addConnector",
            type: "POST",
            data: JSON.stringify(data),
            contentType: "application/json"
        },function (data) {
        if(data.statusCode != 0){
            Messenger().post({
                message: data.statusMessage,
                type: "error",
                hideAfter: 5,
                showCloseButton: true
            })
        }
        }
    )
}


/**
 * DESC : delete input map data when remove connector
 * DATE : 2019/12/5 14:09
 * AUTHOR : UDEAN
 */
function deleteInput(id) {
    var cid = id.substr(0,18) + id.substr(36,18);
    InputMap.remove(cid);
    var data = {};
    data['sourceId'] = id.substr(0,36);
    data['targetId'] = id.substr(36);
    common.ajax({
        url:"/connect/delConnector",
        type:"POST",
        data:JSON.stringify(data),
        contentType : "application/json"
    },function (data) {
        if(data.statusCode != 0){
            Messenger().post({
                message: data.statusMessage,
                type: "error",
                hideAfter: 5,
                showCloseButton: true
            });
        }
        }
    )
}

/**
 * DESC : delete data when node has been deleted
 * DATE : 2019/12/11 15:55
 * AUTHOR : UDEAN
 */
function deleteOutput(id) {
    var sid = id.substr(0,18);
    OutputMap.remove(sid);
    common.ajax({
        "url" : "/connect/delOutput",
        "type":"get",
        "data":"source="+sid
    })
}

/**
 * DESC : update input map when relative output save
 * DATE : 2019/12/5 11:28
 * AUTHOR : UDEAN
 */
function initInputMap(id, cons) {
    var tList = targetMap.get(id);
    if(tList == undefined){
        return;
    }
    for(var i = 0 ; i < tList.length; i++){
        var cid = id + tList[i];
        if(InputMap.get(cid) == undefined ) {
            InputMap.put(cid, cons);
        }
    }
}

/**
 * DESC : send output data
 * DATE : 2019/12/4 18:42
 * AUTHOR : UDEAN
 */
function acquireOutput(cid, cons){
    var nList = cons.getName();
    if(nList.length == 0){
        common.ajax({
            "url":"connect/delOutput",
            "type":"get",
            "data":"source="+cid
        })
    }else{
        var tList = cons.getType();
        var source = cid;
        var outName = "";
        var outType = "";
        for (var i = 0; i < tList.length; i++) {
            outName += nList[i]+",";
            outType += tList[i]+",";
        }
        outName = outName.substr(0,outName.length-1);
        outType = outType.substr(0,outType.length-1);
        common.ajax(  {
                "url" : "/connect/output",
                "type" : "post",
                "data" : "source="+source+"&Name="+outName + "&Type="+outType
            },function (data) {
                if(data.statusCode == 1){
                    Messenger().post({
                        message: "save output data failed",
                        type: "error",
                        hideAfter: 5,
                        showCloseButton: true
                    });
                }else{
                    alert("success");
                }
            }
        )
    }
}

/**
 * DESC : send program input data
 * DATE : 2019/11/27 14:20
 * AUTHOR : UDEAN
 */
function acquireInput(cid , checked){
    if(checked.length == 0){
        alert("if you don't want any output from this program\nplease delete this link line");
        alert("if there are no any date dependency from this node\nplease remove it");
    }else{
        var source = cid.substr(0,18);
        var target = cid.substr(18,18);
        var data = [];
        for (var i = 0; i < checked.length; i++) {
            data += checked[i]+",";
        }
        data = data.substr(0,data.length-1);
        common.ajax(  {
                "url" : "/connect/input",
                "type" : "post",
                "data" : "source="+source+"&target="+target + "&input="+data
            },function (data) {
                if(data.statusCode == 0){
                    alert("success");
                    // Messenger().post({
                    //     message: "saving input data",
                    //     type: "success",
                    //     hideAfter: 5,
                    //     showCloseButton: true
                    // });
                }else{
                    Messenger().post({
                        message: "save input data failed",
                        type: "error",
                        hideAfter: 5,
                        showCloseButton: true
                    });
                }
            }
        )
    }
}

//Input Output edit function
var InputMap = new Map();
var OutputMap = new Map();
var InputCount = new Map();

/**
 * DESC : data construction, synchronized with back-font data
 * DATE : 2019/12/23 10:59
 * AUTHOR : UDEAN
 */
function ParamData(id,type, name) {
    this.id = id;
    this.type = type;
    this.name = name;
}


//Output data construction
function InputData (){
    this.nList = [];
    this.tList = [];
}

InputData.prototype.getName = function(){
    return this.nList;
}

InputData.prototype.getType = function(){
    return this.tList;
}

InputData.prototype.add = function (list1,list2){
    if(list1.indexOf(undefined) != -1 ||
        list2.indexOf(undefined) != -1 ||
        list1.indexOf("") != -1 ||
        list2.indexOf("") != -1 ){
        return 1;
    }
    this.nList = [];
    this.tList = [];
    for(var i = 0 ; i < list1.length ; i++){
        var t = list1[i];
        list1[i] = undefined;
        if(list1.indexOf(t) != -1){
            return 2;
        }
        this.nList.push(t);
        this.tList.push(list2[i]);
    }
    return 0;
}

//connector tool bar
function  initConnectPopover(id) {
    $("#"+id).popover({
        visibility:"visible",
        trigger:"manual",
        placement:"bottom",
        html:true,
        container: $("#flowarea"),
        animation: false,
        content:baseMap.get("connectorPopover")
    }).on("mouseenter", function () {
        removePopover();
        var _this = this;
        $(this).popover("show");
        $(this).siblings(".popover").on("mouseleave", function () {
            $(_this).popover('hide');
        });
    }).on("mouseleave", function () {
        var _this = this;
        setTimeout(function () {
            if (!$(".popover:hover").length) {
                $(_this).popover("hide")
            }
        }, 100);
    }).on("show.bs.popover", function() {
        var _this = this;
        baseMap.put("selector", $(_this).attr("id"));
    });
}

//send connector message while saving file
function acquireConnectors(callback) {
    var array = getConnectors() ;
    common.ajax({
        "url":"/connect/amount",
        "type":"get",
        "data":"length="+array.length
    },function (data) {
            if(data.statusCode == 1){
                Messenger().post({
                    message: "synchronize error, may product incomplete code",
                    type:"error",
                    hideAfter:5,
                    showCloseButton: true
                })
            }else{
                var len = 0 ;
                while(len < array.length){
                    var s = array[len];
                    common.ajax(
                        {
                            "url": "/connect/add",
                            "type": "get",
                            "data": "sourceId="+s.getSourceId()+"&targetId="+ s.getTargetId()
                        },function (data){
                            if(data.statusCode == 0){
                                Messenger().post({
                                    message: "saving connector",
                                    type: "success",
                                    hideAfter: 5,
                                    showCloseButton: true
                                });
                            }else{
                                Messenger().post({
                                    message: "save connector failed",
                                    type: "error",
                                    hideAfter: 5,
                                    showCloseButton: true
                                });
                            }
                        });
                    len ++;
                }
                callback(success,fail);
            }
        }
    )
}

/**
 * DESC : change label accroding to input data
 * DATE : 2019/12/12 15:49
 * AUTHOR : UDEAN
 */
function setLabel(id,label) {
    instanceMap.get(id).connection.setLabel(label);
}

/**
 * DESC : check is flow runnable
 * DATE : 2019/11/11 9:49
 * AUTHOR : UDEAN
 */
function flowIsLegal(success, fail) {
    common.ajax(
        {
            "url" : "/connect/legal",
            "type" : "get"
        },
        function (data) {
            if(data.statusCode == 0){
                Messenger().post({
                    message: "flow legal",
                    type: "success",
                    hideAfter: 5,
                    showCloseButton: true
                });
                success();
            }else{
                Messenger().post({
                    message: "flow illegal",
                    type: "error",
                    hideAfter: 5,
                    showCloseButton: true
                });
                fail();
            }
        }
    )

}

/**
 * DESC : callback function for legal check
 * DATE : 2019/12/3 9:11
 * AUTHOR : UDEAN
 */
function fail() {
    alert("check is there a loop exist which would occurs error");
}

function success() {
    var form = $("<form>");
    form.attr("style", "display:none");
    form.attr("method", "post");
    form.attr("action", baseUrl + "/tool/down");
    var  mapArray = elementMap.keySet();
    if(mapArray.length > 0) {
        var index = 0 ;
        while(index < mapArray.length) {
            var value = elementMap.get(mapArray[index++]);
            var flowIdInput = $("<input>");
            flowIdInput.attr("type", "hidden");
            flowIdInput.attr("name", "flowId_" + index);
            flowIdInput.attr("value", value.getId());
            form.append(flowIdInput);
        }
    }
    var flowIdInput = $("<input>");
    flowIdInput.attr("type","hidden");
    flowIdInput.attr("name","length");
    flowIdInput.attr("value",mapArray.length);
    form.append(flowIdInput);

    var flowIdInput = $("<input>");
    flowIdInput.attr("type","hidden");
    flowIdInput.attr("name","flowId");
    flowIdInput.attr("value",$("#flowSelect").attr("flowId"));
    form.append(flowIdInput);
    var flowNameInput = $("<input>");
    flowNameInput.attr("type","hidden");
    flowNameInput.attr("name","flowName");
    flowNameInput.attr("value",$("#flowSelect").attr("flowName"));
    form.append(flowNameInput);
    $("body").append(form);

    form.submit();
    form.remove();
}

/**
 * DESC : delete out time connector cache
 * DATE : 2019/11/12 9:40
 * AUTHOR : UDEAN
 */
function delCache() {
    var array = getConnectors();
    var index = 0 ;
    for(;index<array.length;index++){
        var s = array[index];
        common.ajax({
                "url":"/connect/del",
                "type":"get",
                "data": "sourceId="+s.getSourceId()+"&targetId="+ s.getTargetId()
            }, function (data) {
                if(data.statusCode == 0){
                    Messenger().post({
                        message: "delete success",
                        type: "success",
                        hideAfter: 5,
                        showCloseButton: true
                    })
                }else{
                    Messenger().post({
                        message: "delete failed",
                        type: "success",
                        hideAfter: 5,
                        showCloseButton: true
                    })
                }
            }
        )
    }
}

/**
 * DESC : delete all connector cache without data transform
 * DATE : 2019/11/12 9:49
 * AUTHOR : UDEAN
 */
function delAll() {
    common.ajax({
            // "url":"/connect/delAll",
        "url":"/connect/test",
            "type":"get",
        "data" : "callback=0"
        },function (data) {
            if(data.statusCode == 1){
                Messenger().post({
                    message: "delete cache failed",
                    type: "error",
                    hideAfter: 5,
                    showCloseButton: true
                })
            }else{
                if(data.statusCode == 2){
                    Messenger().post({
                        message: "force delete",
                        type: "success",
                        hideAfter: 5,
                        showCloseButton: true
                    })
                }
                //send connectors data several time
                // acquireConnectors(flowIsLegal);
                //send connectors data one time
                sendAllConnectors(flowIsLegal);

            }
        }
    )
}

/**
 * DESC : load import file text position
 * DATE : 2019/11/8 18:14
 * AUTHOR : UDEAN
 */
function reloadPosition(Id , xy) {
    var element = elementMap.get(Id);
    element.setLeft(xy.x);
    element.setTop(xy.y);
}

/**
 * DESC : acquire element xy value for test
 * DATE : 2019/11/8 18:33
 * AUTHOR : UDEAN
 * PARM : element Id
 */
function acquireXY(Id) {
    var element = elementMap.get(Id);
    alert(element.getLeft() + " " + element.getTop());
}

/**
 * DESC : transform element to xy
 * DATE : 2019/11/8 18:35
 * AUTHOR : UDEAN
 */
function  transXY(element) {
    var xy = {x:element.getLeft(),y:element.getTop()};
    return xy;
}

/**
 * DESC : get XY int test
 * DATE : 2019/11/8 18:37
 * AUTHOR : UDEAN
 */
function XY(x , y) {
    return {x:x,y:y};
}

function sendAllConnectors(callback) {
    var array = getConnectors();
    if (array.length > 0) {
        common.ajax({
            "url": "/connect/test",
            "type": "get",
            // "data": "length=" + array.length
            "data" : "callback=0"
        }, function (data) {
            if (data.statusCode == 0) {
                var len = 0;
                var sList = "";
                var tList = "";
                while (len < array.length) {
                    sList += array[len].getSourceId() + ",";
                    tList += array[len].getTargetId() + ",";
                    len++;
                }
                sList = sList.substr(0,sList.length-1);
                tList = tList.substr(0,tList.length-1);
                common.ajax(
                    {
                        "url": "/connect/test",
                        "type": "POST",
                        // "data": "sourceId=" + sList + "&targetId=" + tList
                        "data" : "callback=0"
                    }, function (data) {
                        if (data.statusCode == 0) {
                            Messenger().post({
                                message: "saved connector",
                                type: "success",
                                hideAfter: 5,
                                showCloseButton: true
                            });
                            callback(success, fail);
                        } else {
                            Messenger().post({
                                message: "save connector failed",
                                type: "error",
                                hideAfter: 5,
                                showCloseButton: true
                            });
                        }
                    });
            }
        })
    }
}


