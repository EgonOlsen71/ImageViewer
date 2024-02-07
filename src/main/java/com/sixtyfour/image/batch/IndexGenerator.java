package com.sixtyfour.image.batch;

import com.sixtyfour.image.Logger;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Generates a simple example page out of the images in some directory. This not part
 * of the server application but simply to generate a demo web page.
 *
 * @author EgonOlsen
 */
public class IndexGenerator {

    public static void main(String[] args) {

        String dir = "C:/Users/EgonOlsen/Desktop/images";
        String html;
        try (BufferedInputStream bos = new BufferedInputStream(Objects.requireNonNull(IndexGenerator.class.getResourceAsStream("/template.html"))); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            bos.transferTo(baos);
            html = baos.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        File[] files = new File(dir).listFiles((dir1, name) -> {
            name = name.toLowerCase();
            return (name.endsWith(".jpg") || name.endsWith("webp") || name.endsWith("png") || name.endsWith("jpeg"));
        });

        for (File file : files) {
            String name = file.getName();
            int pos = html.indexOf("<!-- imgs -->");
            html = html.substring(0, pos) + "<img src=\"" + name + "\" alt=\"" + name + "\"/>" + html.substring(pos);
        }

        File target = new File(dir, "index.html");
        boolean ok = target.delete();
        Logger.log("Deleted: "+ok);
        try (PrintWriter pw = new PrintWriter(target, StandardCharsets.UTF_8)) {
            pw.print(html);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
