package com.sixtyfour.image;

import com.sixtyfour.petscii.KoalaConverter;
import com.sixtyfour.petscii.Vic2Colors;

import javax.net.ssl.SSLHandshakeException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Servlet to download an image/image list
 *
 * @author EgonOlsen
 */
@WebServlet(name = "ImageViewer", urlPatterns = {"/ImageViewer"}, initParams = {
        @WebInitParam(name = "imagepath", value = "/imagedata/")})
public class ImageViewer extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final static LinkedHashMap<String, String> URL_SHORTENER = new LinkedHashMap<>() {
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return this.size() > 2000;
        }
    };

    public ImageViewer() {
        // TODO Auto-generated constructor stub
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        setUserAgent();
        ServletConfig sc = getServletConfig();
        String path = sc.getInitParameter("imagepath");

        response.setHeader("WiC64", "true");
        response.setContentType("application/octet-stream");
        ServletOutputStream os = response.getOutputStream();
        os.flush();

        String clear = request.getParameter("clear");
        if (clear != null) {
            ImageCache.clear();
        }

        String file = URLDecoder.decode(request.getParameter("file"), StandardCharsets.UTF_8).trim();
        boolean needsCropping = false;

        if (URL_SHORTENER.containsKey(file)) {
            needsCropping = file.contains("ai=1");
            Logger.log("Replacing URL " + file + "with " + URL_SHORTENER.get(file));
            file = URL_SHORTENER.get(file);
        }

        if (file.startsWith("empty:")) {
            Logger.log("Sending empty reply!");
            os.flush();
            return;
        }

        String dither = request.getParameter("dither");
        boolean keepRatio = Boolean.parseBoolean(request.getParameter("ar"));
        if (file.contains("..") || file.contains("\\") || file.startsWith("/")) {
            Logger.log("Invalid file name: " + file);
            printError(os, "Invalid file name!");
            return;
        }
        float dithy = 1;
        if (dither != null) {
            try {
                dithy = Float.parseFloat(dither) / 100f;
                dithy = Math.min(1, Math.max(0, dithy));
            } catch (Exception e) {
                //
            }
        }
        Logger.log("Dithering is set to " + dithy);

        String key = ImageCache.getKey(file, dithy, keepRatio);
        Blob blob = ImageCache.get(key);
        if (blob == null) {
            blob = convert(file, path, os, dithy, keepRatio, needsCropping);
            if (blob == null) {
                // No image but a file list...
                return;
            }
            ImageCache.put(key, blob);
            if (blob.isError()) {
                // The actual error has already been transmitted by the convert()-method
                return;
            }
        }
        if (blob.isError()) {
            // Cached error, re-transmit it...
            printError(os, blob.getError());
            return;
        }

        try (InputStream is = blob.getAsStream()) {
            response.setHeader("Content-disposition",
                    "attachment; filename=" + blob.getTarget());
            // Transfer whole blob...
            is.transferTo(os);
        } catch (Exception e) {
            Logger.log("Failed to transfer file: " + blob.getSource(), e);
            return;
        } 
        os.flush();
        Logger.log("Download and conversion finished!");
    }

    private static void setUserAgent() {
        System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
        System.setProperty("https.agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
    }

    private Blob convert(String file, String path, ServletOutputStream os, float dithy, boolean keepRatio, boolean needsCropping) {
        Blob blob;
        boolean directPdfLink = file.startsWith("page://");
        boolean maybeUrl = UrlUtils.maybeUrl(file);

        if (!directPdfLink) {
            if (file.endsWith(".")) {
                file = file.substring(0, file.length() - 1);
            }
            if (maybeUrl && !file.toLowerCase().startsWith("http")) {
                file = "https://" + file;
            }

            file = UrlUtils.fixFilename(file);

            String lfile = file.toLowerCase();
            if (!lfile.contains(".png") && !lfile.contains(".jpg") && !lfile.contains(".jpeg") && !lfile.contains(".webp")) {
                Logger.log("Unsupported image type: " + file);
                if (lfile.contains(".pdf")) {
                    Logger.log("PDF detected, rendering it...");
                    List<String> rendered = new PdfRenderer().renderPages(file, path);
                    transmitImageReferences(os, rendered, null);
                } else {
                    if (maybeUrl) {
                        Logger.log("Trying to extract images from page...");
                        extractImages(file, os, ImageMode.WEB);
                    } else {
                        if (UrlUtils.isAiPrompt(lfile)) {
                            Logger.log("Generating images with OpenAI...");
                            extractImages(file, os, ImageMode.AI);
                        } else {
                            Logger.log("Searching for images on Google...");
                            extractImages(file, os, ImageMode.SEARCH);
                        }
                    }
                }
                return null;
            }
        }

        Logger.log("Downloading image: " + file);

        String ext = getType(file);

        String targetFile = UUID.randomUUID() + ext;
        File pathy = new File(path);
        boolean ok = pathy.mkdirs();
        Logger.log("Directory state: " + ok);
        File bin = new File(pathy, targetFile);

        file = UrlUtils.encode(file); // (Re-)encode the URL..not sure, why I'm decoding it in the first place, but anyway...

        setUserAgent();
        try (InputStream input = directPdfLink ? new FileInputStream(new File(pathy, file.substring(7))) : new URL(file).openStream(); FileOutputStream fos = new FileOutputStream(bin)) {
            input.transferTo(fos);
        } catch (FileNotFoundException e) {
            Logger.log("File not found: " + file, e);
            delete(bin);
            return printError(os, "Image not found!");
        } catch (IOException e) {
            Logger.log("IO error while loading image: " + file, e);
            String code = "???";
            String msg = e.getMessage();
            if (msg.contains("code: ")) {
                int pos = msg.indexOf("code: ");
                code = msg.substring(pos + 6, pos + 9).trim();
            }
            delete(bin);
            return printError(os, "Server returned error: " + code);
        } catch (Exception e) {
            Logger.log("Failed to load image: " + file, e);
            delete(bin);
            return printError(os, "Failed to load image!");
        }

        String fileName = bin.toString();
        String targetFileName = fileName + ".koa";
        File targetBin = new File(targetFileName);
        try {
            KoalaConverter.convert(fileName, targetFileName, new Vic2Colors(), 1, dithy, keepRatio, needsCropping, false);
        } catch (Exception e) {
            delete(targetBin);
            delete(bin);
            Logger.log("Failed to convert image: " + file, e);
            if (e.getMessage() != null) {
                return printError(os, e.getMessage());
            } else {
                return printError(os, "Failed to convert image!");
            }
        }

        blob = new Blob(targetFile, file);
        try (FileInputStream fis = new FileInputStream(targetBin)) {
            // Store file data in blob...
            blob.fill(fis);
        } catch (Exception e) {
            Logger.log("Failed to fill blob: " + file, e);
            blob = printError(os, "Cache error!");
        } finally {
            delete(targetBin);
            delete(bin);
        }
        return blob;
    }

    private String getType(String file) {
        String lFile = file.toLowerCase();
        if (lFile.contains(".jpg") || lFile.contains(".jpeg")) {
            return ".jpg";
        }
        if (lFile.contains(".png")) {
            return ".png";
        }
        if (lFile.contains(".webp")) {
            return ".webp";
        }
        return lFile.substring(file.lastIndexOf("."));
    }

    private void extractImages(String query, ServletOutputStream os, ImageMode mode) {
        List<String> images = null;
        try {
            if (mode==ImageMode.WEB) {
                ImageExtractor iex = new ImageExtractor();
                try {
                    images = iex.extractImages(query);
                } catch (SSLHandshakeException e) {
                    Logger.log("https doesn't work, trying http instead...");
                    images = iex.extractImages(query.replace("https:", "http:"));
                } catch (IgnoredRedirectException ee) {
                    Logger.log("trying with/out www....");
                    if (query.contains("www.")) {
                        Logger.log("Removing www...");
                        images = iex.extractImages(query.replace("www.", ""));
                    } else {
                        Logger.log("Adding www...");
                        images = iex.extractImages(query.replace("://", "://www."));
                    }
                }
            }
            if (mode == ImageMode.SEARCH) {
                images = GoogleImageExtractor.searchImages(query);
            }
            if (mode == ImageMode.AI) {
                images = AiImageGenerator.createImages(query, false);
            }
        } catch (OpenAiException e) {
            Logger.log("Invalid query: " + query, e);
            printError(os, e.getMessage().replace("_", " "));
            return;
        } catch (FileNotFoundException e) {
            Logger.log("URL not found: " + query, e);
            printError(os, "URL not found!");
            return;
        } catch (UnknownHostException e) {
            Logger.log("Unknown host: " + query, e);
            printError(os, "Unknown host!");
            return;
        } catch (java.net.SocketException e) {
            Logger.log("Network is unreachable: " + query, e);
            printError(os, "Network is unreachable (local?)!");
            return;
        } catch (Exception e) {
            Logger.log("Failed to extract images from " + query, e);
            printError(os, "No valid images found!");
            return;
        }

        transmitImageReferences(os, images, mode);
    }

    private void transmitImageReferences(ServletOutputStream os, List<String> images, ImageMode mode) {
        if (images == null) {
            printError(os, "No valid images found!");
            return;
        }

        for (int i = 0; i < images.size(); i++) {
            String image = images.get(i);
            if (image.length() < 170) {
                continue;
            }
            String newImage = "https://jpct.de/" + UUID.randomUUID() + ".short"+(mode==ImageMode.AI?"ai=1":"");
            Logger.log("URL too long, transmitting a short form instead!");
            URL_SHORTENER.put(newImage, image);
            images.set(i, newImage);
        }

        if (images.isEmpty()) {
            printError(os, "No valid images found!");
            return;
        }

        //
		/*
			Convert image list into bytes...Format is:
			1 1
			length - bytes
			length - bytes
			...
			0
		 */
        if (images.size() > 22) {
            images = images.subList(0, 22);
            Logger.log("Limited image list to " + images.size());
        }
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            bos.write(new byte[]{1, 1}); // Flag image list to C64

            byte[] len = new byte[1];
            for (String img : images) {
                byte[] txt = img.getBytes(StandardCharsets.US_ASCII);
                len[0] = (byte) (txt.length & 0xff);
                bos.write(len);
                bos.write(txt);
            }
            len[0] = 0;
            bos.write(len);
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            bis.transferTo(os);
        } catch (Exception e) {
            Logger.log("Failed to process image list!", e);
            printError(os, "Failed to process images!");
        }
    }

    private void delete(File bin) {
        Logger.log("Deleting file: " + bin);
        boolean ok1 = bin.delete();
        Logger.log("Status: " + ok1);
    }

    public Blob printError(ServletOutputStream os, String text) {
        try {
            os.print((char) 0);
            os.print((char) 0);
            os.print("ERROR: " + text);
            return new Blob(text);
        } catch (Exception e) {
            Logger.log("Failed to write error into stream!", e);
            return null;
        }
    }
}