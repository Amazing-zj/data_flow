package com.mcg.plugin.ehcache;

import com.mcg.common.Constants;
import com.mcg.common.SpringContextHelper;
import com.mcg.entity.flow.Node.NodeText;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;


public class NodeCache {

    private static CacheManager cacheManager = ((CacheManager) SpringContextHelper.getSpringBean("cacheManager"));
    private static Logger logger = LoggerFactory.getLogger(NodeCache.class);

    public static Object get(Object key){
        Cache cache = cacheManager.getCache(Constants.NODE);
        Element element = cache.get(key);
        return  element == null? null : element.getObjectValue();
    }

    public static void put(Object keys, Object val){
        Cache cache = cacheManager.getCache(Constants.NODE);
        cache.put(new Element(keys,val));
    }

    public static void del(Object key){
         if (!cacheManager.getCache(Constants.NODE).remove(key)){
             logger.error("delete failed: "+ key);
         }
    }

    public static List<String> getKeys(){
        return cacheManager.getCache(Constants.NODE).getKeys();
    }

    public static List<NodeText> getAll(){
        List<NodeText> result = new LinkedList();
        List<String> keys = NodeCache.getKeys();
        if(keys != null && keys.size() != 0){
            for(String key : keys){
               result.add((NodeText) NodeCache.get(key));
            }
        }
        return result;
    }
}
