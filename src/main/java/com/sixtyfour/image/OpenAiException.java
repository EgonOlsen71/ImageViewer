package com.sixtyfour.image;

/**
 * Exception to indicate that something went wrong with OpenAI...
 */
public class OpenAiException extends Exception {

    public OpenAiException(String msg) {
        super(msg);
    }

}
