package com.sixtyfour.image.tests;

import com.sixtyfour.image.WordList;

public class WordListTest {

    public static void main(String[] args) throws Exception {
        for (int i=0; i<10; i++) {
            System.out.println(WordList.getRandomWord());
        }
    }
}
