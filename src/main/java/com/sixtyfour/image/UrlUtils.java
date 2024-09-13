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
            return URLEncoder.encode(url, StandardCharsets.UTF_8).replace("%3A", ":").replace("%2F", "/").replace("%3D", "=")
                    .replace("%3B", ";").replace("%26", "&").replace("%3F", "?").replace("+", "%20");
        } catch (Exception e) {
            return url;
        }
    }

    public static String fixFilename(String file) {
        return file.replaceAll("\\b(ai|ki)\\s+:", "ai:");
    }

    public static boolean isAiPrompt(String url) {
        if (url==null) {
            return false;
        }
        String lurl = url.toLowerCase().trim();
        return (lurl.startsWith("ai:") || lurl.startsWith("ki:")) && lurl.length()>4;
    }

    public static boolean maybeUrl(String file) {
        // Simple check...is there a letter after a dot? In that case, it might be an URL...
        // So basically, the comment above qualifies as URL. Well, except for ai: prompts
        if (isAiPrompt(file)) {
            return false;
        }
        file = file.trim();
        int pos = file.indexOf(".");
        if (pos!=-1 && pos<file.length()-2) {
            char c=file.charAt(pos+1);
            return Character.isAlphabetic(c);
        }
        return false;
    }

}
