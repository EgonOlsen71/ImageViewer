package com.sixtyfour.image.tests;

import com.sixtyfour.image.UrlUtils;
import com.sixtyfour.petscii.Bitmap;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class TestImageLoading {

    public static void main(String[] args) {
        String bin = "c:/tmp/testfile.jpg";
        String file = "https://images.squarespace-cdn.com/content/v1/5f7ca9b4bb17060b028086bb/d564bd70-5ffd-465a-8af6-8f717ca6a4db/Michele Mouton Picture 1.jpg";
        file = UrlUtils.encode(file);

        try (InputStream input = new URL(file).openStream(); FileOutputStream fos = new FileOutputStream(bin)) {
            input.transferTo(fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
