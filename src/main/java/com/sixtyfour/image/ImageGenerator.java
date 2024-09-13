package com.sixtyfour.image;

import java.util.List;

/**
 * Interface for (Ai-)Image generators
 *
 * @author EgonOlsen
 */
public interface ImageGenerator {

    List<String> createImages(String query) throws Exception;

    List<String> createImages(String query, ImageDimensions dimension) throws Exception;

}
