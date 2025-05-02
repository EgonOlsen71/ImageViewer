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
         testImageRemix();

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
        byte[] image = Files.readAllBytes(new File("K:\\Video\\Retro\\Spiele-Sonderheft 2\\Ttitle_tmp.jpg").toPath());
        List<String> images = new IdeogramImageGenerator().createImages("ai:A comic character named 'Star Killer' broke a hole into a table and now looks through it to a magazine. Behind him are stars visible. The hole is basically a portal to another universe..", image, 70, ImageDimensions.SCREEN);
        images.forEach(System.out::println);
    }

}
