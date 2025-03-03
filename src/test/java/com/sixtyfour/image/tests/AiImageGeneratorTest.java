package com.sixtyfour.image.tests;

import com.sixtyfour.image.DalleImageGenerator;
import com.sixtyfour.image.IdeogramImageGenerator;
import com.sixtyfour.image.ImageDimensions;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class AiImageGeneratorTest {

    public static void main(String[] args) throws Exception {

        testImageGenerator();
        // testImageRemix();

/*
        List<String> images = AiImageGenerator.createImages("ai:(random)", false);
        images.forEach(System.out::println);
        */

    }

    private static void testImageGenerator() throws Exception {
        //List<String> images = new DalleImageGenerator().createImages("ai:princess zelda get captured by an evil wizard, b/w, comic panel,
        // line art, sketch drawing with black ink, think lines, anime, very low detail, high contrast, pixel art, white background!", ImageDimensions.SCREEN);
        //images.forEach(System.out::println);

        List<String> images = new IdeogramImageGenerator().createImages("ai:A woman in a tank top.", ImageDimensions.SCREEN);
        images.forEach(System.out::println);
    }

    private static void testImageRemix() throws Exception {
        //List<String> images = new DalleImageGenerator().createImages("ai:princess zelda get captured by an evil wizard, b/w, comic panel,
        // line art, sketch drawing with black ink, think lines, anime, very low detail, high contrast, pixel art, white background!", ImageDimensions.SCREEN);
        //images.forEach(System.out::println);
        byte[] image = Files.readAllBytes(new File("E:\\tmp\\Title.jpg").toPath());
        List<String> images = new IdeogramImageGenerator().createImages("ai:A 3d rendered, Mario like video game character looks out of a console's cartridge slot!", image, 80, ImageDimensions.SCREEN);
        images.forEach(System.out::println);
    }

}
