package com.sixtyfour.image;

import java.io.FileInputStream;
import java.util.Properties;

/**
 *
 */
public class Config {

    private String googleApiKey;

    private String openApiKey;

    private String cx;

    private String dalle3secret;

    public Config() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("/webdata/imageviewer/apikey.ini"));
            cx = props.getProperty("cx");
            googleApiKey = props.getProperty("key");
            openApiKey = props.getProperty("openapikey");
            dalle3secret =props.getProperty("dalle3secret");
        } catch(Exception e)  {
            Logger.log("Failed to load Properties!", e);
            throw new RuntimeException(e);
        }
    }

    public String getGoogleApiKey() {
        return googleApiKey;
    }

    public String getOpenApiKey() {
        return openApiKey;
    }

    public String getCx() {
        return cx;
    }

    public String getDalle3secret() {
        return dalle3secret;
    }
}
