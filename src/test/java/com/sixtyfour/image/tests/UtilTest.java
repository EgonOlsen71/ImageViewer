package com.sixtyfour.image.tests;

import com.sixtyfour.image.UrlUtils;

/**
 *
 */
public class UtilTest {

    public static void main(String[] args) throws Exception {
        String url ="http://toller-test.de/ich bin ein bild.jpg";
        url = UrlUtils.encode(url);
        System.out.println(url);
    }
}
