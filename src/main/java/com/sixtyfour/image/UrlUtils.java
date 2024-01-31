package com.sixtyfour.image;

import java.net.URLEncoder;

/**
 * Simple utils for URLs...not much in here for now.
 *
 * @author EgonOlsen
 */
public class UrlUtils {

    public static String encode(String imgSrc) {
        try {
            //@todo improve this hack...
            return URLEncoder.encode(imgSrc, "UTF-8").replace("%3A", ":").replace("%2F", "/").replace("%3B", ";").replace("%26", "&").replace("%3F", "?");
        } catch (Exception e) {
            return imgSrc;
        }
    }

}
