package com.mcg.plugin.ehcache;

import com.mcg.common.Constants;
import com.mcg.common.SpringContextHelper;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputCache {
    private static CacheManager cacheManager = (CacheManager)SpringContextHelper.getSpringBean("cacheManager");
    private static Logger logger  = LoggerFactory.getLogger(OutputCache.class);

    public static Object get(Object key){
        Cache cache = cacheManager.getCache(Constants.OUTPUT);
        Element element = cache.get(key);
        return  element == null? null : element.getObjectValue();
    }

    public static void put(Object keys, Object val){
        Cache cache = cacheManager.getCache(Constants.OUTPUT);
        cache.put(new Element(keys,val));
    }

    public static void del(Object key){
        logger.info("delete input cache with key: "+key);
        cacheManager.getCache(Constants.OUTPUT).remove(key);
    }

}
