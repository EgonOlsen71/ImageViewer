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

    public static String generateWordSoup(String query) {
        String org = query;
        query = "an image depicting "+ query +". ";
        query += org +" is located at "+WordList.getRandomWord();
        query += " and also "+WordList.getRandomWord()+" among "+WordList.getRandomWord()+"."+WordList.getRandomWord();

        String[] fillers = {" and in ", " seems to ", " looks like ", " and ", " or ", " but ", " except for ", " on a ", " behind a ", " in front of ", " but only ", " gazing at ", " looking at ", " on top of ", " below a "};

        for (int i=0; i<8; i++) {
            String word = WordList.getRandomWord();
            query += fillers[(int) (Math.random()*fillers.length)]+ word;
        }
        return query+"!";
    }

}
