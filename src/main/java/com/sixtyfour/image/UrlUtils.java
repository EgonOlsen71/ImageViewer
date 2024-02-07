package com.sixtyfour.image;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Simple utils for URLs...not much in here for now.
 *
 * @author EgonOlsen
 */
public class UrlUtils {

    public static String encode(String url) {
        try {
            if (url.contains("%")) {
                return url;
            }
            //@todo improve this hack...
            return URLEncoder.encode(url, StandardCharsets.UTF_8).replace("%3A", ":").replace("%2F", "/").replace("%3B", ";").replace("%26", "&").replace("%3F", "?");
        } catch (Exception e) {
            return url;
        }
    }

}
