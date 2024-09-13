package com.sixtyfour.image.tests;

import com.sixtyfour.image.DalleImageGenerator;
import com.sixtyfour.image.IdeogramImageGenerator;
import com.sixtyfour.image.ImageDimensions;

import java.util.List;

public class AiImageGeneratorTest {

    public static void main(String[] args) throws Exception {

        testDallE();

/*
        List<String> images = AiImageGenerator.createImages("ai:(random)", false);
        images.forEach(System.out::println);
        */

    }

    private static void testDallE() throws Exception {
        //List<String> images = new DalleImageGenerator().createImages("ai:princess zelda get captured by an evil wizard, b/w, comic panel,
        // line art, sketch drawing with black ink, think lines, anime, very low detail, high contrast, pixel art, white background!", ImageDimensions.SCREEN);
        //images.forEach(System.out::println);

        List<String> images = new IdeogramImageGenerator().createImages("ai:a Commodore 64 in the forest", ImageDimensions.SCREEN);
        images.forEach(System.out::println);
    }

}
