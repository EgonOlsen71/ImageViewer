package com.sixtyfour.image;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple cache for converted images.
 *
 * @author EgonOlsen
 */
public class ImageCache {

    private final static LinkedHashMap<String, Blob> IMAGE_CACHE = new LinkedHashMap<>() {
        protected boolean removeEldestEntry(Map.Entry eldest) {
            Map.Entry<String, Blob> entry = eldest;
            return System.currentTimeMillis() - entry.getValue().getTime() > 1000 * 60 * 20 || this.size() > 1000;
        }
    };

    public static synchronized void put(String key, Blob data) {
        data.setTime(System.currentTimeMillis());
        IMAGE_CACHE.put(key, data);
        Logger.log(key+" stored in cache!");
    }

    public static synchronized Blob get(String key) {
        Logger.log("Image cache contains "+IMAGE_CACHE.size()+" entries!");
        Blob data = IMAGE_CACHE.get(key);
        if (data == null) {
            Logger.log(key+" not in cache!");
            return null;
        }
        data.setTime(System.currentTimeMillis());
        Logger.log(key+" cached (Error: "+ data.isError()+")");
        return data;
    }

    public static String getKey(String url, float dither, boolean keepRatio) {
        return url+"_"+dither+"_"+keepRatio;
    }

}
