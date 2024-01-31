package com.sixtyfour.image.tests;

import com.sixtyfour.image.GoogleImageExtractor;

import java.util.List;

public class TestGoogleImageExtractor {

    public static void main(String[] args) throws Exception {
        List<String> images = GoogleImageExtractor.searchImages("cat");
        listImages(images);
    }

    private static void listImages(List<String> images) {
        images.stream().forEach(System.out::println);
    }

}
