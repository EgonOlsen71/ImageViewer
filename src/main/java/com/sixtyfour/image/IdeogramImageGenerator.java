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
public class IdeogramImageGenerator implements ImageGenerator {

    private final static String BASE_URL = "https://api.ideogram.ai/generate";
    private final static String JSON = "{\n" +
            "  \"image_request\": {\n" +
            "    \"prompt\": \"{0}\",\n" +
            "    \"resolution\": \"{1}\",\n" +
            "    \"model\": \"V_2_TURBO\",\n" +
            "    \"magic_prompt_option\": \"AUTO\"\n" +
            "  }\n" +
            "}";
    private static Config config = new Config();

    public List<String> createImages(String query) throws Exception {
        return createImages(query, ImageDimensions.SQUARE);
    }

    public List<String> createImages(String query, ImageDimensions dimension) throws Exception {
        Logger.log("Using Ideogram for image generation...");
        List<String> ret = new ArrayList<>();

        if (UrlUtils.isAiPrompt(query)) {
            query = query.substring(3).trim();
        }
        query = query.replace("\n", " ").replace("\r", " ").replace("\"", "'");
        String json = JSON;

        if (query.contains("(random)")) {
            query = WordList.generateWordSoup(WordList.getRandomWord());
            Logger.log("Generated ai query is: "+query);
        }

        json = json.replace("{0}", query);

        switch(dimension) {
            case SQUARE:
                Logger.log("Generating square image!");
                json = json.replace("{1}", "RESOLUTION_1024_1024");
                break;
            case SCREEN:
                Logger.log("Generating screen fitting image!");
                json = json.replace("{1}", "RESOLUTION_1152_704");
                break;
            case CROPPED:
                Logger.log("Generating 'cropped' image!");
                json = json.replace("{1}", "RESOLUTION_1536_512");
                break;
        }
        Logger.log(json);
        Logger.log("AI query: "+query);

        HttpURLConnection con = null;
        String resp = "";
        try {
            Logger.log("Calling Ideogram-API...");
            URL url = new URL(BASE_URL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Api-Key", config.getIdeogramApiKey());
            con.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
            InputStream is;
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
            throw new AiException("AI timeout!");
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(resp, Map.class);

        if (map.containsKey("error")) {
            try {
                String msg = map.get("error").toString();
                Logger.log("Ideogram returned error: " + msg);
                throw new AiException(msg);
            } catch (AiException oe) {
                throw oe;
            } catch(Exception e) {
                Logger.log("Ideogram returned: " + resp);
                throw new AiException("Failed to access Ideogram!");
            }
        }

        if (!map.containsKey("data")) {
            Logger.log("Ideogram returned: " + resp);
            throw new AiException("Failed to access Ideogram!");
        }

        List<Map> data = (List<Map>) map.get("data");
        for (Map entry:data) {
            String link = entry.get("url").toString();
            Logger.log("Image link: "+link);
            ret.add(link);
        }

        return ret;
    }

}
