package com.sixtyfour.image.tests;

import com.sixtyfour.image.ImageExtractor;

import java.util.List;

public class TestImageExtractor {

    public static void main(String[] args) throws Exception {
        List<String> images = ImageExtractor.extractImages("https://spiegel.de");
        listImages(images);
        images = ImageExtractor.extractImages("https://buecher.schluetersche.de/de/veterinaermedizin/gross-nutztier/");
        listImages(images);
        images = ImageExtractor.extractImages("https://service.schluetersche.de");
        listImages(images);
        images = ImageExtractor.extractImages("https://jpct.net?farts=true&blah=blubb");
        listImages(images);
        images = ImageExtractor.extractImages("https://www.gamestar.de/artikel/c64-das-naechste-jahrtausend-die-besten-neuen-spiele-fuer-den-commodore-64,3336967.html");
        listImages(images);
        images = ImageExtractor.extractImages("https://gamerant.com/best-amiga-games/");
        listImages(images);
        images = ImageExtractor.extractImages("https://foerster-engel.de/expertise.html");
        listImages(images);
        images = ImageExtractor.extractImages("https://crpgaddict.blogspot.com");
        listImages(images);

    }

    private static void listImages(List<String> images) {
        System.out.println("----------------------------------------------------");
        images.stream().forEach(System.out::println);
    }

}
