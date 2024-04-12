package com.sixtyfour.image.batch;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class Worder {

    public static void main(String[] args) throws Exception {
        String file = "e:/tmp/words_input.txt";
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        PrintWriter pw = new PrintWriter("e:/tmp/words.txt", StandardCharsets.UTF_8);
        while (br.ready()) {
            String line = br.readLine();
            String word = line;
            if (word.length()>2 && Character.isAlphabetic(word.charAt(0))) {
                pw.println(word.trim());
            }
        }
        pw.close();
        br.close();
    }

}
