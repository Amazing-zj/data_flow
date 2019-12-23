package com.mcg.plugin.ehcache;

import com.mcg.common.Constants;
import com.mcg.common.SpringContextHelper;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class DependencyCache {

    private static CacheManager  cacheManager = (CacheManager) SpringContextHelper.getSpringBean("cacheManager");
    public static void put(Object val){
        Cache cache = cacheManager.getCache(Constants.DEPEN);
        cache.put(new Element(cache.getSize(),val));
    }

    public static void removeAll(){
        cacheManager.getCache(Constants.DEPEN).removeAll();
    }

    public static Object get(Object key){
        return  cacheManager.getCache(Constants.DEPEN).get(key).getObjectValue();
    }

    public static int size(){
        return cacheManager.getCache(Constants.DEPEN).getSize();
    }
}
