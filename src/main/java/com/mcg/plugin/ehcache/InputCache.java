package com.mcg.plugin.ehcache;

import com.mcg.common.Constants;
import com.mcg.common.SpringContextHelper;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class InputCache {
    private static CacheManager cacheManager = (CacheManager) SpringContextHelper.getSpringBean("cacheManager");
    private static Logger logger = LoggerFactory.getLogger(InputCache.class);

    public static void putMap(Object key, Object mapKey, Object value) {
        Cache cache = cacheManager.getCache(Constants.INPUT);
        Element e = cache.get(key);
        Map map;
        if (e == null) {
            logger.info("no such map with keys: " + key);
            logger.info("create "+ key +" map");
            map = new HashMap();
            map.put(mapKey,value);
            cache.put(new Element(key,map));
        } else {
            map = (Map) e.getObjectValue();
            map.put(mapKey, value);
        }
    }

    public static void delMap(Object key, Object mapKey){
        Cache cache =cacheManager.getCache(Constants.INPUT);
        Element e = cache.get(key);
        logger.info("finding map with key: "+key);
        if(e == null){
            logger.debug("there is not map with keys: "+ key);
            return ;
        }
        Map map =(Map)e.getObjectValue();
        map.remove(mapKey);
    }

    public static boolean isContains(Object key, Object mapKey){
        Cache cache =cacheManager.getCache(Constants.INPUT);
        Element e =cache.get(key);
        if(e == null)
            return false;
        Map map = (Map) e.getObjectValue();
        return map.containsKey(mapKey);
    }

    public static boolean del(Object key, Object mapKey){
        Cache cache = cacheManager.getCache(Constants.INPUT);
        Element e = cache.get(key);
        if(e == null) {
            return true;
        }
        Map map = (Map)e.getObjectValue();
        if(map == null || map.size()==0){
            return true;
        }
        map.remove(mapKey);
        return map.get(key) == null;
    }

    public static Object get(Object key){
        Cache cache = cacheManager.getCache(Constants.INPUT);
        Element e = cache.get(key);
        return e==null ? null : e.getObjectValue();
    }
}
