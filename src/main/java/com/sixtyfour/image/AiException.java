package com.sixtyfour.image;

/**
 * Exception to indicate that something went wrong with OpenAI...
 */
public class AiException extends Exception {

    public AiException(String msg) {
        super(msg);
    }

}
