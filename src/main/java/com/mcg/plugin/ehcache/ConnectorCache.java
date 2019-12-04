package com.mcg.plugin.ehcache;

import com.mcg.common.Constants;
import com.mcg.common.SpringContextHelper;
import com.mcg.entity.flow.connector.ConnectorData;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectorCache {

    private static Logger logger = LoggerFactory.getLogger(ConnectorCache.class);
    private static CacheManager cacheManager = (CacheManager) SpringContextHelper.getSpringBean("cacheManager");

    public static void removeAll(){
        Cache cache = cacheManager.getCache(Constants.CONNECTOR_CACHE);
        cache.removeAll();
    }

    public static boolean isNull(){
        Cache cache = cacheManager.getCache(Constants.CONNECTOR_CACHE);
        if(cache.getSize()>0) {
            return false;
        }else{
            return true;
        }
    }

    public static boolean put(Integer key, ConnectorData value){
        Cache cache = cacheManager.getCache(Constants.CONNECTOR_CACHE);
        if(cache == null){
            logger.error("can't find cache, cache is null");
            return false;
        }
        cache.put(new Element(key,value));
        return  true;
    }

    public static ConnectorData get(Integer key){
        Cache cache =cacheManager.getCache(Constants.CONNECTOR_CACHE);
        if(cache == null){
           logger.error("cache error when getting cache");
            return null;
        }
        Element element = cache.get(key);
        if(element == null){
            logger.error("not such key-value set");
            return null;
        }
        return (ConnectorData)element.getObjectValue() ;
    }

    public static boolean forceToRemove(){
        Cache cache = cacheManager.getCache(Constants.CONNECTOR_CACHE);
        try{
            cache.removeAll();
        }catch (Exception e){
            if(e instanceof IllegalStateException){
                logger.error("cache status error " +  cache.getStatus().intValue());
            }else{
                logger.debug("unknown exception occur, try to force removing cache");
                Object [] keys = cache.getKeys().toArray();
                try {
                    for (int i = 0; i < keys.length; i++) {
                        cache.remove(keys[i]);
                    }
                }catch (Exception ex){
                    if(e instanceof IllegalStateException){
                        logger.error("cache status error " + cache.getStatus().intValue());
                    }else {
                        logger.debug("unknown exception occur and can't remove, restart server again");
                    }
                }
            }
        }
        return true;
    }

    public static boolean remove(Integer key){
        return cacheManager.getCache(Constants.CONNECTOR_CACHE).remove(key);
    }

}


