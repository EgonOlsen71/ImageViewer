package com.sixtyfour.image;

import java.util.List;

/**
 * Decides, which AI to use, uses it and returns the resuling image.
 *
 * @author EgonOlsen
 */
public class AiDecider {

    public static List<String> generateImages(String query, boolean d42Mode) throws Exception {
        ImageDimensions dimensions = ImageDimensions.SCREEN;
        ImageGenerator generator;
        if ((query.contains("(2)") || query.contains("(dalle)")) && !d42Mode) {
            query = query.replace("(dalle)", " ").trim();
            generator = new DalleImageGenerator();
            Logger.log("Decided to use Dall-E!");
        } else {
            generator = new IdeogramImageGenerator();
            Logger.log("Decided to use Ideogram!");
            if (d42Mode) {
                dimensions = ImageDimensions.CROPPED;
                Logger.log("Generating image for D42!");
            }
        }
        return generator.createImages(query, dimensions);
    }

}


