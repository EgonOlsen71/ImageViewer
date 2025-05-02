package com.sixtyfour.image;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
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

    private final static String BASE_URL = "https://api.ideogram.ai/v1/ideogram-v3/generate";
    private final static String BASE_URL_REMIX = "https://api.ideogram.ai/v1/ideogram-v3/remix";

    private static Config config = new Config();

    public List<String> createImages(String query) throws Exception {
        return createImages(query, ImageDimensions.SQUARE);
    }

    public List<String> createImages(String query, ImageDimensions dimension) throws Exception {
        return createImages(query, null, null, dimension);
    }

    public List<String> createImages(String query, byte[] image, Integer weight, ImageDimensions dimension) throws Exception {
        Logger.log("Using Ideogram for image generation...");
        List<String> ret = new ArrayList<>();

        if (UrlUtils.isAiPrompt(query)) {
            query = query.substring(3).trim();
        }
        query = query.replace("\n", " ").replace("\r", " ").replace("\"", "'");
        boolean remix = false;

        if (image != null) {
            Logger.log("Image remix mode!");
            remix = true;
        }

        if (query.contains("(random)")) {
            query = WordList.generateWordSoup(WordList.getRandomWord());
            Logger.log("Generated ai query is: " + query);
        }

        //AUTO""
        boolean magicPrompt = !query.contains("(mpoff)");
        query = query.replace("(mpoff)", " ").trim();

        Resolution resolution = null;

        switch (dimension) {
            case SQUARE:
                Logger.log("Generating square image!");
                resolution = Resolution.R_1024x1024;
                break;
            case SCREEN:
                Logger.log("Generating screen fitting image!");
                resolution = Resolution.R_1152x704;
                break;
            case CROPPED:
                Logger.log("Generating 'cropped' image!");
                resolution = Resolution.R_1536x576;
                break;
            }
            Logger.log("AI query: " + query);

            HttpURLConnection con = null;
            String resp = "";
            try {
                Logger.log("Calling Ideogram-API...");
                URL url = new URL(remix ? BASE_URL_REMIX : BASE_URL);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);
                doMultipartRequest(con, query, magicPrompt, resolution, weight, image);
                InputStream is;
                try {
                    is = con.getInputStream();
                } catch (IOException e) {
                    is = con.getErrorStream();
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                is.transferTo(bos);
                is.close();
                resp = bos.toString(StandardCharsets.UTF_8);
                bos.close();
            } catch (Exception e) {
                Logger.log("Failed to create images!", e);
                throw e;
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }

            if (resp == null || resp.length() == 0) {
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
                } catch (Exception e) {
                    Logger.log("Ideogram returned: " + resp);
                    throw new AiException("Failed to access Ideogram!");
                }
            }

            if (!map.containsKey("data")) {
                Logger.log("Ideogram returned: " + resp);
                throw new AiException("Failed to access Ideogram!");
            }

            List<Map> data = (List<Map>) map.get("data");
            for (Map entry : data) {
                String link = entry.get("url").toString();
                String prompt = entry.get("prompt").toString();
                Logger.log("Final prompt: " + prompt);
                Logger.log("Image link: " + link);
                if (link == null || link.isEmpty()) {
                    Logger.log("Empty image link returned!");
                    throw new AiException("No image generated, most likely a content policy violation!");
                }
                ret.add(link);
            }

            return ret;
        }

        private void doMultipartRequest(HttpURLConnection con, String query, boolean magicPrompt, Resolution dimension, Integer weight, byte[] image) throws IOException {
            String boundary = "------WebKitFormBoundary" + Long.toHexString(System.currentTimeMillis());
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setRequestProperty("Api-Key", config.getIdeogramApiKey());

            OutputStream outputStream = con.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

            // prompt field
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"prompt\"").append("\r\n\r\n");
            writer.append(query).append("\r\n");
            writer.flush();

            // rendering_speed field
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"rendering_speed\"").append("\r\n\r\n");
            writer.append("TURBO").append("\r\n");
            writer.flush();

            // magic_prompt field
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"magic_prompt\"").append("\r\n\r\n");
            writer.append(magicPrompt?"ON":"OFF").append("\r\n");
            writer.flush();

            if (image!=null && image.length>0) {
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"image_weight\"").append("\r\n\r\n");
                writer.append(weight.toString()).append("\r\n");
                writer.flush();
            }

            // resolution
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"resolution\"").append("\r\n\r\n");
            writer.append(dimension.getValue());//.append("\r\n");
            writer.flush();

            if (image!=null && image.length>0) {
                writer.append("\r\n");
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"remixme.png\"").append("\r\n");
                writer.append("Content-Type: application/octet-stream").append("\r\n\r\n");
                writer.flush();

                outputStream.write(image);
                outputStream.flush();
            }

            writer.append("\r\n");
            writer.append("--").append(boundary).append("--").append("\r\n");
            writer.flush();
            writer.close();
        }

}
