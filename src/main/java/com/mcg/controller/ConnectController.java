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
import org.hibernate.result.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


import javax.servlet.http.HttpSession;
import java.awt.*;
import java.io.BufferedWriter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Controller
@RequestMapping("/connect")
public class ConnectController {

    private static AtomicBoolean canUse;
    private static CountDownLatch latch = null;
    private static List<ConnectorData> list;
    private static Map<String, Integer> indexMap;
    private static Map<Integer, String> reindexMap;
    private static int index;
    private static Logger logger = LoggerFactory.getLogger(ConnectController.class);

    static {
        index = 0;
        indexMap = new HashMap<>();
        reindexMap = new HashMap<>();
        list = new LinkedList<>();
        canUse = new AtomicBoolean(true);
    }

    public static void cachePersistent(BufferedWriter bw) throws Exception {
        ConnectorData keySet[] = new ConnectorData[list.size()];
        int i = 0;
        for (ConnectorData temp : list) {
            keySet[i++] = temp;
        }
        ToolController.cachePersistent(keySet, bw);
    }

    @RequestMapping(value = "/delAll", method = RequestMethod.GET)
    @ResponseBody
    public McgResult delAll() {
        McgResult result = new McgResult();
        result.setStatusCode(0);
        for (int i = 0; i < index; i++) {
            if (!ConnectorCache.remove(i)) {
                if (ConnectorCache.hasKey(i)) {
                    logger.error("error occur " + list.get(i).toString());
                } else {
                    logger.debug("element deleted " + list.get(i).toString());
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
                list.remove(mapArray[i++]);
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
            if( add( new ConnectorData( sourceId[i], targetId[i]))){
                latch.countDown();
                logger.info("count down once, left: "+latch.getCount());
               continue;
            }else{
                result.setStatusCode(1);
                break;
            }
        }
        return result;
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    @ResponseBody
    public McgResult add(String sourceId, String targetId, HttpSession session) {
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
        result.addAttribute(session.getId(), message);
        return result;
    }

    private void removeCache() {
        logger.info("start thread to remove connector cache");
        new Thread(new RemoveCacheThread()).start();
    }

    private static class RemoveCacheThread implements Runnable {
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
                if(!latch.await(10, TimeUnit.SECONDS)){
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
            return result;
        }
        if (DependencyCache.size() != 0) {
            DependencyCache.removeAll();
        }
        if (list.size() > 1 && isIllegal()) {
            logger.error("illegal dependency loop");
            result.setStatusCode(1);
        } else {
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
        McgResult result = new McgResult();
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
                        if (d_list.indexOf(temp_data) == -1) {
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
        }
        return result;
    }

    @RequestMapping(value = "/output", method = RequestMethod.POST)
    @ResponseBody
    public McgResult getOutputData(String source, String[] Type, String[] Name) {
        McgResult result = new McgResult();
        result.setStatusCode(0);
        OutputCache.del(source);
        if (OutputCache.get(source) != null) {
            result.setStatusCode(1);
        } else {
            String[] type = Type;
            String[] name = Name;
            if (type.length != name.length) {
                result.setStatusCode(1);
                logger.error("type length " + type.length + " is not equal to name length " + name.length);
            } else {
                Set<ParamData> list = new TreeSet();
                for (int i = 0; i < name.length; i++) {
                    list.add(new ParamData(source, type[i], name[i]));
                }
                logger.info("save output cache with key " + source);
                OutputCache.put(source, list);
            }
        }
        return result;
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
        McgResult result = new McgResult();
        List<String>target = data.getTargetList();
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
