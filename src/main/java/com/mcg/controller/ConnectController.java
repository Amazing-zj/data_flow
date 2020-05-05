package com.mcg.controller;


import com.mcg.common.sysenum.LogTypeEnum;
import com.mcg.entity.common.McgResult;
import com.mcg.entity.flow.connector.ConnectorData;
import com.mcg.entity.flow.connector.OutputUpdate;
import com.mcg.entity.flow.connector.ParamData;
import com.mcg.entity.message.Message;
import com.mcg.entity.message.NotifyBody;
import com.mcg.plugin.ehcache.ConnectorCache;
import com.mcg.plugin.ehcache.DependencyCache;
import com.mcg.plugin.ehcache.InputCache;
import com.mcg.plugin.ehcache.OutputCache;
import com.mcg.plugin.websocket.MessagePlugin;
import org.apache.commons.collections.list.TreeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


import javax.servlet.http.HttpSession;
import java.io.BufferedWriter;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Controller
@RequestMapping("/connect")
public class ConnectController {

    private  AtomicBoolean canUse;
    private  CountDownLatch latch = null;
    private  List<ConnectorData> list;
    private  Map<String, Integer> indexMap;
    private  Map<Integer, String> reindexMap;
    private  int index;
    private  Logger logger = LoggerFactory.getLogger(ConnectController.class);

     {
        index = 0; //how many connectors
        indexMap = new HashMap<>(); // connector index for dependency
        reindexMap = new HashMap<>();
        list = new LinkedList<>(); // connectors list
        canUse = new AtomicBoolean(true);
    }

    @RequestMapping(value = "/delAll", method = RequestMethod.GET)
    @ResponseBody
    public McgResult delAll() {
        McgResult result = new McgResult();
        result.setStatusCode(0);
        if(index == 0) {
            return result;
        }
        for (int i = 0; i < index; i++) {
            if (!ConnectorCache.remove(i)) {
                if (ConnectorCache.hasKey(i) && list.get(i) != null) {
                    logger.error("error occur " + list.get(i).toString());
                } else {
                    if(list.get(i) != null) {
                        logger.debug("element deleted " + list.get(i).toString());
                    }
                }
                result.setStatusCode(1);
                if (removeAll()) {
                    result.setStatusCode(2);
                } else {
                    removeCache();
                }
                break;
            }
        }
        Object[] indexMapArray = indexMap.keySet().toArray();
        Object[] mapArray = list.toArray();
        try {
            int i = 0;
            while (i < indexMapArray.length) {
                indexMap.remove(indexMapArray[i++]);
            }
            i = 0;
            while (i < mapArray.length) {
                if(list.get(i) != null) {
                    list.remove(mapArray[i++]);
                }
            }
        } catch (Exception e) {
            if (e instanceof IndexOutOfBoundsException && list.size() == 0 && indexMap.size() == 0) {
                logger.warn("finish map data remove but index out of bound");
            } else {
                logger.error(e.getClass() + e.getMessage());
            }
        }
        if (list.size() != indexMap.size() || list.size() != 0) {
            logger.debug("map size: " + list.size() + " index size : " + indexMap.size());
        }
        index = 0;
        return result;
    }


    private boolean add(ConnectorData connectorData) {
        if (latch == null) {
            return false;
        }
        logger.info("saving connector map " + connectorData.toString());
        list.add(connectorData);
        return ConnectorCache.put(index++, connectorData);
    }

    @RequestMapping(value = "/amount", method = RequestMethod.GET)
    @ResponseBody
    public McgResult connectorLength(String length) {
        McgResult result = new McgResult();
        if (canUse.compareAndSet(true, false)) {
            result.setStatusCode(0);
            latch = new CountDownLatch(Integer.valueOf(length));
            logger.info("connectors number: "+length);
        } else {
            result.setStatusCode(1);
        }
        return result;
    }

    @RequestMapping("/test")
    @ResponseBody
    public McgResult test(String callback){
        int i;
        try{
            i = Integer.valueOf(callback);
        }catch (NumberFormatException e){
            i = 0;
        }
        McgResult result  = new McgResult();
        result.setStatusCode(i);
        return  result;
    }

    //no use, maybe use it next week, I remember I had tested it a long time age and it work perfectly
    //anyway, there still some details needing adding or modifying
    @RequestMapping(value = "/addArray", method = RequestMethod.POST)
    @ResponseBody
    public McgResult addArray(String[] sourceId, String[] targetId) {
        McgResult result = new McgResult();
        int len = targetId.length;
        if(sourceId.length != len){
            result.setStatusCode(1);
            return result;
        }
        result.setStatusCode(0);
        for (int i = 0; i < len; i++) {
            logger.debug("source: " + sourceId[i] + " target: " + targetId[i]);
            if(indexOf(list, new ConnectorData(sourceId[i],targetId[i])) != -1){
                latch.countDown();
                logger.info("count down once, left: "+latch.getCount());
            }else{
                result.setStatusCode(1);
                break;
            }
        }
        return result;
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    @ResponseBody
    public McgResult add(String sourceId, String targetId) {
        McgResult result = new McgResult();
        Message message = MessagePlugin.getMessage();
        NotifyBody body = new NotifyBody();
        ConnectorData data = new ConnectorData(sourceId, targetId);
        if (add(data)) {
            body.setType(LogTypeEnum.SUCCESS.getValue());
            latch.countDown();
            result.setStatusCode(0);
        } else {
            body.setType(LogTypeEnum.ERROR.getValue());
            result.setStatusCode(1);
        }
        message.setBody(body);
        return result;
    }

    private void removeCache() {
        logger.info("start thread to remove connector cache");
        new Thread(new RemoveCacheThread()).start();
    }

    private  class RemoveCacheThread implements Runnable {
        @Override
        public void run() {
            int count = 5000;
            while (count-- > 0) {
                ConnectorCache.removeAll();
                if (ConnectorCache.isNull()) {
                    break;
                }
            }
            if (count == 0) {
                logger.debug("remove cache failed after 5000 times attempt");
            }
        }
    }

    private boolean removeAll() {
        ConnectorCache.removeAll();
        return ConnectorCache.isNull();
    }

    /**
     * DESC : save dependency when there are only one connector
     * DATE : 2019/12/5 16:48
     * AUTHOR : UDEAN
     */
    private void saveDependency() {
        ConnectorData connectorData = list.get(0);
        List<String> list = new LinkedList<>();
        list.add(connectorData.getSourceId());
        DependencyCache.put(list);
        list = new LinkedList<>();
        list.add(connectorData.getTargetId());
        DependencyCache.put(list);
    }

    @RequestMapping(value = "/legal", method = RequestMethod.GET)
    @ResponseBody
    public McgResult isLegal() {
        McgResult result = new McgResult();
        result.setStatusCode(0);
        if (latch != null) {
            try {
                if (!latch.await(10, TimeUnit.SECONDS)) {
                    result.setStatusCode(3);
                    return result;
                }
            } catch (InterruptedException e) {
                logger.error("latch await error: " + e.getMessage());
            } finally {
                latch = null;
                canUse.set(true);
            }
        } else {
            result.setStatusCode(2);
        }
        if (DependencyCache.size() != 0) {
            DependencyCache.removeAll();
        }
        if (list.size() > 1 && isIllegal()) {
            logger.error("illegal dependency loop");
            result.setStatusCode(1);
        } else {
            result.setStatusCode(0);
            if (list.size() == 1) {
//                logger.info("+----------------------------------------------+");
//                logger.info("|----------------single dependency-------------|");
//                logger.info("+----------------------------------------------+");
                saveDependency();
            }
        }
        logger.info(String.valueOf(DependencyCache.size()));
        return result;
    }

    private boolean TP() {
        int s;
        int t;
        int nodeIndex = 0;
        int in[] = new int[list.size() * 2];
        ArrayList<Integer> list[] = new ArrayList[this.list.size() * 2];
        for (int i = 0; i < list.length; i++) {
            list[i] = new ArrayList<>();
        }
        ArrayList<Integer> queue = new ArrayList<>();
        indexMap = new HashMap<>();
        reindexMap = new HashMap<>();
        for (ConnectorData temp : this.list) {
            if(temp == null){
                continue;
            }
            if (indexMap.containsKey(temp.getSourceId())) {
                s = indexMap.get(temp.getSourceId());
            } else {
                indexMap.put(temp.getSourceId(), nodeIndex);
                reindexMap.put(nodeIndex, temp.getSourceId());
                s = nodeIndex++;
            }
            if (indexMap.containsKey(temp.getTargetId())) {
                t = indexMap.get(temp.getTargetId());
            } else {
                reindexMap.put(nodeIndex, temp.getTargetId());
                indexMap.put(temp.getTargetId(), nodeIndex);
                t = nodeIndex++;
            }
            list[s].add(t);
            in[t]++;
        }
        Date start = new Date();
        in = Arrays.copyOf(in, nodeIndex);
        List<String> queueRank = new LinkedList<>();
        for (int i = 0; i < nodeIndex; i++) {
            if (in[i] == 0) {
                queue.add(i);
                queueRank.add(reindexMap.get(i));
            }
        }
        DependencyCache.put(queueRank);
        if (queue.isEmpty())
            return true;
        int count = queue.size();
        do {
            queueRank = new LinkedList<>();
            ArrayList<Integer> l = list[queue.get(queue.size() - 1)];
            queue.remove(queue.size() - 1);
            for (int index : l) {
                if (--in[index] == 0) {
                    queue.add(index);
                    queueRank.add(reindexMap.get(index));
                    count++;
                }
            }
            if (queueRank.size() != 0) {
                DependencyCache.put(queueRank);
            }
        } while (!queue.isEmpty());
        logger.info("dependency judge time: "+ (new Date().getTime() - start.getTime())+ " ms");
        if (count == nodeIndex)
            return false;
        else {
            DependencyCache.removeAll();
            return true;
        }
    }

    private boolean isIllegal() {
        return TP();
    }

    @RequestMapping(value = "/delInput", method = RequestMethod.GET)
    @ResponseBody
    public McgResult delInputData(String source, String target) {
        McgResult result = new McgResult();
        if (InputCache.del(target, source)) {
            result.setStatusCode(0);
        } else {
            result.setStatusCode(1);
        }
        return result;
    }

    @RequestMapping(value = "/input", method = RequestMethod.POST)
    @ResponseBody
    public McgResult getInputData(String source, String target, String[] input) {
        McgResult result = new McgResult(); // TODO: 2019/12/24 9:56 rewrite ajax call back message-----according to result message
        result.setStatusCode(0);
        InputCache.delMap(target, source);
        if (InputCache.isContains(target, source)) {
            result.setStatusCode(1);
        } else {
            String[] data = input;
            Set<ParamData> list = new TreeSet<>();
            Set<ParamData> s_list = (Set) OutputCache.get(source);
            if (data.length == 0) {
                list = null;
            } else if (s_list.size() == data.length) {
                list.addAll(s_list);
            } else {
                if (data.length * 2 <= s_list.size()) {
                    for (int i = 0; i < data.length; i++) {
                        String[] temp_Str = data[i].split("\\_", 2);
                        ParamData t = new ParamData(source, temp_Str[0], temp_Str[1]);
                        if (!s_list.contains(t)) {
                            result.setStatusCode(1);
                            logger.error("can't find element from source" + t.toString());
                            return result;
                        } else {
                            for (ParamData temp_Data : s_list) {
                                if (t.compareTo(temp_Data) == 0) {
                                    list.add(temp_Data);
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    List<ParamData> d_list = new TreeList();
                    for (int i = 0; i < data.length; i++) {
                        String[] temp_Str = data[i].split("\\_", 2);
                        d_list.add(new ParamData(source, temp_Str[0], temp_Str[1]));
                    }
                    list.addAll(s_list);
                    List<ParamData> deleteArray = new ArrayList<>(list.size() - data.length);
                    for (ParamData temp_data : list) {
                        if (indexOf(d_list, temp_data) == -1) {
                            deleteArray.add(temp_data);
                            if (deleteArray.size() + data.length == list.size()) {
                                break;
                            }
                        }
                    }
                    list.removeAll(deleteArray);
                }
            }
            logger.info("save input cache with key " + target);
            InputCache.putMap(target, source, list);
        }
        return result;
    }

    @RequestMapping(value = "/delOutput", method = RequestMethod.GET)
    @ResponseBody
    public McgResult delOutputData(String source) {
        McgResult result = new McgResult();
        OutputCache.del(source);
        if (OutputCache.get(source) != null) {
            result.setStatusCode(1);
        } else {
            result.setStatusCode(0);
            delInput(source);
        }
        return result;
    }

    /**
     * DESC : invoke when create connector and init input data
     * DATE : 2019/12/24 14:51
     * AUTHOR : UDEAN
     */
    @RequestMapping(value = "/addConnector", method = RequestMethod.POST)
    @ResponseBody
    public McgResult addConnector(@RequestBody ConnectorData data){
        McgResult result = new McgResult();
        if(ConnectorCache.put(index++,data)){
            list.add(data);
            initInput(data.getSourceId(),data.getTargetId());
            result.setStatusCode(0);
        }else {
            result.setStatusCode(1);
            result.setStatusMes("");
        }
        return result;
    }

    /**
     * DESC : invoke when connector deleted and delete input data
     * DATE : 2019/12/24 14:51
     * AUTHOR : UDEAN
     */
    @RequestMapping(value = "/delConnector", method = RequestMethod.POST)
    @ResponseBody
    public McgResult delConnector(@RequestBody ConnectorData data){
        McgResult result = new McgResult();
        int index = indexOf(list, data);
        if(index == -1) {
            result.setStatusCode(1);
            result.setStatusMes("not such connector data");
        }else {
            ConnectorCache.remove(index);
            list.remove(index);
            result.setStatusCode(0);
            delInput(data.getSourceId(), data.getTargetId());
        }
        return result;
    }

    /**
     * DESC : turn delete-add way to delete-deleted and add-increased
     * DATE : 2019/12/24 14:52
     * AUTHOR : UDEAN
     */
    @RequestMapping(value = "/output", method = RequestMethod.POST)
    @ResponseBody
    public McgResult getOutputData(String source, String[] Type, String[] Name) {
        McgResult result = new McgResult();
        result.setStatusCode(0);
        Set<ParamData>set =(TreeSet) OutputCache.get(source);
        if(set == null || set.size() == 0){
            firstTimeEdit(source, Type, Name, result);
        }else{
            updateOutput(source, Type, Name, set, result);
        }
        return result;
    }

    /**
     * DESC : delete input cache data
     * DATE : 2019/12/26 14:18
     * AUTHOR : UDEAN
     */
    private void updateInput(ConnectorData data, List<ParamData> deleteList){
        Map<String,Set>map =(Map) InputCache.get(data.getTargetId().substring(0,18));
        Set<ParamData> set = null;
        if(map != null){
            set = map.get(data.getSourceId().substring(0,18));
            if(set != null && set.size() != 0){
                for(ParamData paramData : deleteList){
                    logger.debug(String.valueOf(set.remove(paramData)));
                }
            }
        }
        InputCache.putMap(data.getTargetId().substring(0,18), data.getSourceId().substring(0,18), set);
    }

    private void initInput(String source, String target){
        Set<ParamData> sourceSet =(TreeSet) OutputCache.get(source.substring(0,18));
        InputCache.putMap(target.substring(0, 18), source.substring(0, 18), sourceSet);
    }

    private void delInput(String source, String target){
        InputCache.delMap(target.substring(0, 18), source.substring(0, 18));
    }

    private void delInput(String source){
        List<ConnectorData> targetList = ConnectorCache.getTargetListBySource(source);
        if(targetList != null && targetList.size() != 0){
            for(ConnectorData temp : targetList){
                delInput(source,temp.getTargetId());
            }
        }
    }

    /**
     * DESC : call when first time (or output was null) edit
     * DATE : 2019/12/26 10:03
     * AUTHOR : UDEAN
     */
    private void firstTimeEdit(String source, String []type, String []name, McgResult result){
        List<ConnectorData> target = ConnectorCache.getTargetListBySource(source);
        if(type.length != name.length || type.length == 0){
            result.setStatusCode(1);
            result.setStatusMes("");
            logger.error("length error");
        }else{
            Set<ParamData> set = new TreeSet<>();
            for(int i = 0 ; i < type.length ; i++){
                set.add(new ParamData(source, type[i], name[i]));
            }
            OutputCache.put(source, set);
            if(target != null && target.size() != 0) {
                for (ConnectorData data : target){
                    Set<ParamData> temp_set = new TreeSet<>();
                    temp_set.addAll(set);
                    InputCache.putMap(data.getTargetId().substring(0,18), source, temp_set);
                }
            }
            result.setStatusCode(0);
        }
    }

    /**
     * DESC : call when update output
     * DATE : 2019/12/26 14:04
     * AUTHOR : UDEAN
     */
    private void updateOutput(String source, String []type, String []name, Set<ParamData>set, McgResult result){
        List<ConnectorData> target = ConnectorCache.getTargetListBySource(source);
        if (type.length != name.length) {
            result.setStatusCode(1);
            logger.error("type length " + type.length + " is not equal to name length " + name.length);
        } else {
            Set<ParamData> list = new TreeSet();
            List<ParamData> t_list =new LinkedList<>(); // set not support remove object by index
            t_list.addAll(set);
            ParamData temp = null;
            for (int i = 0; i < name.length; i++) {
                temp = new ParamData(source, type[i], name[i]);
                if ( set.contains(temp)) {
                    int index = indexOf(t_list, temp);
                    if(index == -1){
                        result.setStatusCode(1);
                        logger.error("can't find this object in list");
                        break;
                    }
                    list.add(t_list.remove(index));
                } else {
                    list.add(new ParamData(source, type[i], name[i]));
                }
            }
            OutputCache.put(source, list);
            if(temp != null) {
                logger.info("save output cache with key " + source);
                set.removeAll(list);
                t_list = new LinkedList<>();
                t_list.addAll(set);
                if (set.size() != 0 && target != null && target.size() != 0) {
                    for (ConnectorData data : target) {
                        updateInput(data, t_list);
                    }
                }
            }else{
                logger.debug("can't find: "+ temp == null ? "null" : temp.toString());
                saveErrorToFile();
            }
        }
        result.setStatusCode(0);
    }

    private int indexOf(List<ConnectorData>list, ConnectorData target){
        int len = list.size();
        if(len != 0) {
            for (int index = 0; index < len; index++) {
                if ( list.get(index) != null && target.compareTo(list.get(index)) == 0)
                    return index;
            }
        }
        return -1;
    }

    private int indexOf(List<ParamData>list, ParamData target){
        int len = list.size();
        if(len != 0) {
            for (int index = 0; index < len; index++) {
                if ( list.get(index) != null && target.compareTo(list.get(index)) == 0)
                    return index;
            }
        }
        return -1;
    }

    private void saveErrorToFile(){

    }

    /**
     * DESC : update output data by using adding increasing items and remove relative deleted items
     * in output cache and input cache
     * DATE : 2019/12/23 14:09
     * AUTHOR : UDEAN
     */
    @RequestMapping(value= "/updateOutput", method = RequestMethod.POST)
    @ResponseBody
    public McgResult updateOutput(@RequestBody OutputUpdate data, String source){// TODO: 2019/12/23 14:11 js call function not finish
        McgResult result = new McgResult();         // TODO: 2019/12/24 9:46 to use this function must change the way to construct output form
        List<String> target = data.getTargetList();  // TODO: 2019/12/24 16:50 delete
        List<ParamData> delete = data.getDeleteList();
        List<ParamData> increase = data.getIncreaseList();
        if(delete.size() != 0 ){
            List <ParamData> list = (List)OutputCache.get(source);
            for(ParamData delete_temp : delete){
                list.remove(delete_temp);
            }
        }
        if(increase.size() != 0 ){
            List <ParamData> list = (List)OutputCache.get(source);
            for(ParamData delete_temp : delete){
                list.add(delete_temp);
            }
        }
        if(target.size() != 0){
            for(String temp : target) {
                Map<String, List> map = (Map) InputCache.get(temp);
                List<ParamData> list = map.get(source);
                for (ParamData delete_temp : delete) {
                    list.remove(delete_temp);
                }
            }
        }
        return result;
    }
}
