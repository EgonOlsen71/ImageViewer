package com.sixtyfour.image.tests;

import com.sixtyfour.image.UrlUtils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class TestImageLoading {

    public static void main(String[] args) {
        String bin = "c:/tmp/testfile2.jpg";
        //String file = "https://images.squarespace-cdn.com/content/v1/5f7ca9b4bb17060b028086bb/d564bd70-5ffd-465a-8af6-8f717ca6a4db/Michele Mouton Picture 1.jpg";
        String file = "https://www.frankenpfalz.de/images/com_osgallery/gal-17/original/9-nh-grotte7-p1070251-distlergrotte-fotovf8DA92A05-AF08-E7FC-0E78-CFD61721D694.jpg";
        file = UrlUtils.encode(file);
        //System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
        //System.setProperty("https.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
        try (InputStream input = new URL(file).openStream(); FileOutputStream fos = new FileOutputStream(bin)) {
            input.transferTo(fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
