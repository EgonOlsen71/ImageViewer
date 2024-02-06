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
        ServletConfig sc = getServletConfig();
        String path = sc.getInitParameter("imagepath");

        response.setHeader("WiC64", "true");
        ServletOutputStream os = response.getOutputStream();
        os.flush();

        String file = URLDecoder.decode(request.getParameter("file"), StandardCharsets.UTF_8);

        if (URL_SHORTENER.containsKey(file)) {
            Logger.log("Replacing URL " + file + "with " + URL_SHORTENER.get(file));
            file = URL_SHORTENER.get(file);
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
            } catch (Exception e) {
                //
            }
        }
        dithy = Math.min(1, Math.max(0, dithy));
        Logger.log("Dithering is set to " + dithy);

        boolean directPdfLink = file.startsWith("page://");

        if (!directPdfLink) {
            if (file.endsWith(".")) {
                file = file.substring(0, file.length() - 1);
            }
            if (file.contains(".") && !file.toLowerCase().startsWith("http")) {
                file = "https://" + file;
            }

            String lfile = file.toLowerCase();
            if (!lfile.contains(".png") && !lfile.contains(".jpg") && !lfile.contains(".jpeg") && !lfile.contains(".webp")) {
                Logger.log("Unsupported image type: " + file);
                if (lfile.contains(".pdf")) {
                    Logger.log("PDF detected, rendering it...");
                    List<String> rendered = new PdfRenderer().renderPages(file, path);
                    transmitImageReferences(os, rendered);
                } else {
                    if (file.contains(".")) {
                        Logger.log("Trying to extract images from page...");
                        extractImages(file, os, false);
                    } else {
                        Logger.log("Searching for images on Google...");
                        extractImages(file, os, true);
                    }
                }
                return;
            }
        }

        Logger.log("Downloading image: " + file);

        String ext = getType(file);

        String targetFile = UUID.randomUUID() + ext;
        File pathy = new File(path);
        pathy.mkdirs();
        File bin = new File(pathy, targetFile);

        file = UrlUtils.encode(file); // (Re-)encode the URL..not sure, why I'm decoding it in the first place, but anyway...

        try (InputStream input = directPdfLink?new FileInputStream(new File(pathy, file.substring(7))):new URL(file).openStream(); FileOutputStream fos = new FileOutputStream(bin)) {
            input.transferTo(fos);
        } catch (java.io.FileNotFoundException e) {
            Logger.log("File not found: " + file, e);
            printError(os, "Image not found!");
            delete(bin);
            return;
        } catch (IOException e) {
            Logger.log("IO error while loading image: " + file, e);
            String code = "???";
            String msg = e.getMessage();
            if (msg.contains("code: ")) {
                int pos = msg.indexOf("code: ");
                code = msg.substring(pos+6, pos+9).trim();
            }
            printError(os, "Server returned error code: "+code);
            delete(bin);
            return;
        } catch (Exception e) {
            Logger.log("Failed to load image: " + file, e);
            printError(os, "Failed to load image!");
            delete(bin);
            return;
        }

        String fileName = bin.toString();
        String targetFileName = fileName + ".koa";
        File targetBin = new File(targetFileName);
        try {
            KoalaConverter.convert(fileName, targetFileName, new Vic2Colors(), 1, dithy, keepRatio, false);
        } catch (Exception e) {
            delete(targetBin);
            delete(bin);
            Logger.log("Failed to convert image: " + file, e);
            if (e.getMessage() != null) {
                printError(os, e.getMessage());
            } else {
                printError(os, "Failed to convert image!");
            }
            return;
        }

        try (FileInputStream fis = new FileInputStream(targetBin)) {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition",
                    "attachment; filename=" + targetFile);
            // Transfer whole file...
            fis.transferTo(os);
        } catch (Exception e) {
            Logger.log("Failed to transfer file: " + file, e);
            return;
        } finally {
            delete(targetBin);
            delete(bin);
        }
        os.flush();
        Logger.log("Download and conversion finished!");
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

    private void extractImages(String file, ServletOutputStream os, boolean search) {
        List<String> images;
        try {
            if (!search) {
                try {
                    images = ImageExtractor.extractImages(file);
                } catch(SSLHandshakeException e) {
                    Logger.log("https doesn't work, trying http instead...");
                    images = ImageExtractor.extractImages(file.replace("https:", "http:"));
                }
            } else {
                images = GoogleImageExtractor.searchImages(file);
            }
        } catch (FileNotFoundException e) {
            Logger.log("URL not found: " + file, e);
            printError(os, "URL not found!");
            return;
        } catch (UnknownHostException e) {
            Logger.log("Unknown host: " + file, e);
            printError(os, "Unknown host!");
            return;
        } catch (java.net.SocketException e) {
            Logger.log("Network is unreachable: " + file, e);
            printError(os, "Network is unreachable (local?)!");
            return;
        } catch (Exception e) {
            Logger.log("Failed to extract images from " + file, e);
            printError(os, "No valid images found!");
            return;
        }

        transmitImageReferences(os, images);
    }

    private void transmitImageReferences(ServletOutputStream os, List<String> images) {
        if (images == null) {
            printError(os, "No valid images found!");
            return;
        }

        for (int i = 0; i < images.size(); i++) {
            String image = images.get(i);
            if (image.length() < 170) {
                continue;
            }
            String newImage = "https://jpct.de/" + UUID.randomUUID() + ".short";
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
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
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

    public void printError(ServletOutputStream os, String text) {
        try {
            os.print((char) 0);
            os.print((char) 0);
            os.print("ERROR: " + text);
        } catch (Exception e) {
            Logger.log("Failed to write error into stream!", e);
        }
    }
}