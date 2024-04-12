package com.sixtyfour.image;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A list of english words...
 */
public class WordList {

    private static List<String> words = new ArrayList<>();

    static {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(WordList.class.getResourceAsStream("/words.txt"), StandardCharsets.UTF_8));) {
            Logger.log("Reading word list...");
            while(br.ready()) {
                String line = br.readLine();
                if (!line.isBlank()) {
                    words.add(line);
                }
            }
            Logger.log(words.size()+" words read!");
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @return
     */
    public static String getRandomWord() {
        return words.get((int) (Math.random()*words.size()));
    }

}
