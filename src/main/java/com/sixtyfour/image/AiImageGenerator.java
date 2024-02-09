package com.sixtyfour.image;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates images using OpenAI/DALL-E (2, for cost reasons!)
 */
public class AiImageGenerator {

    private final static String BASE_URL = "https://api.openai.com/v1/images/generations";
    private final static String JSON = "{\"model\":\"dall-e-2\",\"prompt\":\"{0}\",\"n\": 2,\"size\":\"256x256\" }";
    private static Config config = new Config();

    public static List<String> createImages(String query) throws Exception {
        List<String> ret = new ArrayList<>();

        if (query.startsWith("ai:") || query.startsWith("ki:")) {
            query = query.substring(3).trim();
        }
        query = query.replace("\n", " ").replace("\r", " ").replace("\"", "'");
        String json = JSON.replace("{0}", query);

        Logger.log("AI query: "+query);

        HttpURLConnection con = null;
        String resp = "";
        try {
            URL url = new URL(BASE_URL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer "+config.getOpenApiKey());
            con.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
            InputStream is=null;
            try {
                is = con.getInputStream();
            } catch(IOException e) {
                is = con.getErrorStream();
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            is.transferTo(bos);
            is.close();
            resp = bos.toString(StandardCharsets.UTF_8);
            bos.close();
        } catch(Exception e) {
            Logger.log("Failed to create images!", e);
            throw e;
        } finally {
            if (con!=null) {
                con.disconnect();
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(resp, Map.class);
        if (map.containsKey("error")) {
            String msg = ((Map) map.get("error")).get("code").toString();
            Logger.log("OpenAI returned error: "+msg);
            throw new OpenAiException(msg);
        }

        List<Map> data = (List<Map>) map.get("data");
        for (Map entry:data) {
            ret.add(entry.get("url").toString());
        }

        return ret;
    }

}
