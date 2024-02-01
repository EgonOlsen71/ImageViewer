package com.sixtyfour.image.tests;

import com.sixtyfour.image.PdfRenderer;

import java.util.List;

public class PdfRenderingTest {

    public static void main(String[] args) throws Exception {
        PdfRenderer rendy = new PdfRenderer();
        String target = "c:/tmp";
        List<String> images = rendy.renderPages("https://www.loewe-verlag.de/mediathek/downloads/772/leseprobe_leseloewen_1_klasse_drachenpark.pdf", target);
        images.forEach(System.out::println);
    }

}
