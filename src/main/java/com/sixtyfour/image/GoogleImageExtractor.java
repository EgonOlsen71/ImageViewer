package com.sixtyfour.image;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * Performs a Google image source and extracts the results. Without payment, this is limited to 100 request/day.
 * Results are cached (to the end of time, but limited to 100 entries)
 *
 * @author EgonOlsen
 */
public class GoogleImageExtractor {

    // These never work...
    private static String[] TO_IGNORE = {"redd.it"};

    private final static LinkedHashMap<String, String> SEARCH_CACHE = new LinkedHashMap<>() {
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return this.size() > 100;
        }
    };

    private final static String BASE_URL = "https://www.googleapis.com/customsearch/v1?safe=off&cx={0}&key={1}&searchType=image&hq=1&filter=1&q=";
    private static String cx;
    private static String apiKey;

    static {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("/webdata/imageviewer/apikey.ini"));
            cx = props.getProperty("cx");
            apiKey = props.getProperty("key");
        } catch (Exception e) {
            Logger.log("Failed to load Google-API-Properties!", e);
        }
    }

    public static List<String> searchImages(String query) throws Exception {

        List<String> images = new ArrayList<>();
        String html;
        String lhtml;
        String url = BASE_URL.replace("{0}", cx).replace("{1}", apiKey) + URLEncoder.encode(query, "UTF-8");
        long start = System.currentTimeMillis();

        if (!SEARCH_CACHE.containsKey(query)) {
            Logger.log("Searching for: " + query);
            try (InputStream input = new URL(url).openStream(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                input.transferTo(bos);
                start = System.currentTimeMillis();
                html = bos.toString("UTF-8");
                SEARCH_CACHE.put(query, html);
                Logger.log("JSON size: " + html.length() + " bytes");
            } catch (java.io.FileNotFoundException e) {
                Logger.log("URL not found: " + url, e);
                throw e;
            } catch (Exception e) {
                Logger.log("Failed to process url: " + url, e);
                throw e;
            }
        } else {
            Logger.log("Getting results from cache for: " + query);
            Logger.log("Cache size is: " + SEARCH_CACHE.size());
            html = SEARCH_CACHE.get(query);
        }
        lhtml = html.toLowerCase(Locale.ENGLISH);

        // We are treating the JSON as simple plain text here...not nice, but works for this basic case...
        int pos = 0;
        do {
            pos = lhtml.indexOf("\"link\"", pos);
            if (pos != -1) {
                int endPos = lhtml.indexOf("\",", pos);
                if (endPos != -1) {
                    String imgSrc = html.substring(html.indexOf("\"", pos + 7) + 1, endPos);
                    imgSrc = URLDecoder.decode(imgSrc, "UTF-8");
                    String iimgSrc = imgSrc.toLowerCase();
                    if (iimgSrc.contains(".jpg") || iimgSrc.contains(".jpeg") || iimgSrc.contains(".png") || iimgSrc.contains(".webp")) {
                        imgSrc = UrlUtils.encode(imgSrc);
                        if (!images.contains(imgSrc)) {
                            for (String never : TO_IGNORE) {
                                if (!imgSrc.toLowerCase().contains(never)) {
                                    images.add(imgSrc);
                                } else {
                                    Logger.log("Ignoring URL: " + imgSrc);
                                }
                            }
                        }
                    }
                }
                pos = endPos;
            }
        } while (pos != -1);

        Logger.log("Page parsed in " + (System.currentTimeMillis() - start) + "ms");
        Logger.log("Images found: " + images.size());
        return images;
    }
}
