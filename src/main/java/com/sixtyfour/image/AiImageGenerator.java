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
 * Generates images using OpenAI/DALL-E 3
 */
public class AiImageGenerator {

    private final static String BASE_URL = "https://api.openai.com/v1/images/generations";
    private final static String JSON_DALLE2 = "{\"model\":\"dall-e-2\",\"prompt\":\"{0}\",\"n\": 2,\"size\":\"256x256\" }";
    private final static String JSON_DALLE3 = "{\"model\":\"dall-e-3\",\"prompt\":\"{0}\",\"n\": 1,\"size\":\"{1}\" }";
    private static Config config = new Config();

    public static List<String> createImages(String query) throws Exception {
        return createImages(query, true);
    }

    public static List<String> createImages(String query, boolean squared) throws Exception {
        List<String> ret = new ArrayList<>();

        if (UrlUtils.isAiPrompt(query)) {
            query = query.substring(3).trim();
        }
        query = query.replace("\n", " ").replace("\r", " ").replace("\"", "'");
        String json = JSON_DALLE3;
        String secret = config.getDalle2secret();
        if (query.contains(secret)) {
            query = query.replace(secret, " ").trim();
            json = JSON_DALLE2;
            Logger.log("Secret fallback to DALL-E2 activated!");
        }

        if (query.contains("(random)")) {
            query = WordList.generateWordSoup(WordList.getRandomWord());
            Logger.log("Generated ai query is: "+query);
        }

        json = json.replace("{0}", query);

        if (squared) {
            json = json.replace("{1}", "1024x1024");
        } else {
            json = json.replace("{1}", "1792x1024");
        }

        Logger.log("AI query: "+query);

        HttpURLConnection con = null;
        String resp = "";
        try {
            Logger.log("Calling OpenAI-API...");
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

        if (resp==null || resp.length()==0) {
            Logger.log("No images generated!?");
            throw new OpenAiException("AI timeout!");
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(resp, Map.class);
        if (map.containsKey("error")) {
            try {
                String msg = ((Map) map.get("error")).get("code").toString();
                Logger.log("OpenAI returned error: " + msg);
                throw new OpenAiException(msg);
            } catch (OpenAiException oe) {
                throw oe;
            } catch(Exception e) {
                Logger.log("OpenAI returned: " + resp);
                throw new OpenAiException("Failed to access OpenAI!");
            }
        }

        List<Map> data = (List<Map>) map.get("data");
        for (Map entry:data) {
            ret.add(entry.get("url").toString());
        }

        return ret;
    }

}
