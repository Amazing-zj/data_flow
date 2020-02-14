package com.mcg.plugin.ehcache;

import com.mcg.common.Constants;
import com.mcg.common.SpringContextHelper;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class NodeCache {

    private static CacheManager cacheManager = ((CacheManager) SpringContextHelper.getSpringBean("nodeCache"));
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
}
