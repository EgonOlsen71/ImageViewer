package com.sixtyfour.image.tests;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestConversion {

    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("C:/Users/EgonOlsen/Desktop/1.sm.webp"));
        System.out.println(img.getWidth()+"/"+img.getHeight());
    }

}
