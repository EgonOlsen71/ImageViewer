package com.sixtyfour.image.tests;

import com.sixtyfour.image.AiImageGenerator;

import java.util.List;

public class AiImageGeneratorTest {

    public static void main(String[] args) throws Exception {
        List<String> images = AiImageGenerator.createImages("ai:a dog on the beach", false);
        images.forEach(System.out::println);
    }

}
