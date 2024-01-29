package com.sixtyfour.image;

import java.io.*;

/**
 * Generates a simple example page out of the images in some directory
 *
 * @author EgonOlsen
 */
public class IndexGenerator {

    public static void main(String[] args) {

        String dir ="C:/Users/EgonOlsen/Desktop/images";
        String html;
        try (BufferedInputStream bos = new BufferedInputStream(IndexGenerator.class.getResourceAsStream("/template.html")); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
           bos.transferTo(baos);
           html = baos.toString("UTF-8");
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        File[] files = new File(dir).listFiles((dir1, name) -> {
            name = name.toLowerCase();
            return (name.endsWith(".jpg") || name.endsWith("webp") || name.endsWith("png")|| name.endsWith("jpeg"));
        });

        for (File file:files) {
            String name = file.getName();
            int pos = html.indexOf("<!-- imgs -->");
            html = html.substring(0,pos)+"<img src=\""+name+"\" alt=\""+name+"\"/>"+html.substring(pos);
        }

        File target = new File(dir, "index.html");
        target.delete();
        try (PrintWriter pw = new PrintWriter(target, "UTF-8")) {
            pw.print(html);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
