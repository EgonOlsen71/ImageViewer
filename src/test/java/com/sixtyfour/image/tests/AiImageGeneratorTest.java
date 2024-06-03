package com.sixtyfour.image.tests;

import com.sixtyfour.image.AiImageGenerator;

import java.util.List;

public class AiImageGeneratorTest {

    public static void main(String[] args) throws Exception {
        List<String> images = AiImageGenerator.createImages("wet cunts");
        images.forEach(System.out::println);
    }

}
