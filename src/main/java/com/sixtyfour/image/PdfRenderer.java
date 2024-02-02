package com.sixtyfour.image;

import com.sixtyfour.petscii.Bitmap;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Renders a PDF into images (one per page)
 */
public class PdfRenderer {

    public static final String PDF_PNG = ".pdf.png";

    public List<String> renderPages(String sourcePdf, String targetDir) {
        String td = targetDir;
        new Thread(() -> cleanUp(td)).start();
        if (!targetDir.endsWith("/")) {
            targetDir +="/";
        }
        long start = System.currentTimeMillis();
        String name = UUID.randomUUID().toString();
        List<String> ret = new ArrayList<>();
        File targetFile = null;
        if (sourcePdf.toLowerCase().startsWith("http")) {
            // This was supposed to fix some issue with remote PDF...it didn't, but I'll leave it this way anyway now...
            Logger.log("Copying PDF from remote to local...");
            targetFile = new File(targetDir, name+".pdf");
            try (InputStream is = new URL(sourcePdf).openStream(); OutputStream os=new FileOutputStream(targetFile)) {
                is.transferTo(os);
                sourcePdf = targetFile.toString();
            } catch(Exception e) {
                Logger.log("Failed to copy PDF!", e);
                if (targetFile!=null) {
                    targetFile.delete();
                }
                return ret;
            }
        }
        try(InputStream is = new FileInputStream(sourcePdf)) {
            Logger.log("Loading PDF: "+sourcePdf);
            PDDocument document = PDDocument.load(is);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int i=0; i<document.getNumberOfPages() && i<10; i++) {
                Logger.log("Rendering page: "+i);
                BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 150, ImageType.RGB);
                Bitmap bmp = new Bitmap(bim);
                String pdfImageName = name+"_"+i+PDF_PNG;
                bmp.save(targetDir + pdfImageName);
                ret.add("page://"+pdfImageName);
            }
            document.close();
        } catch(Exception e) {
            Logger.log("Failed to render PDF!", e);
            return ret;
        }
        finally {
            if (targetFile!=null) {
                Logger.log("Deleting original PDF: "+targetFile);
                targetFile.delete();
            }
        }
        Logger.log(ret.size()+" pages rendered in "+(System.currentTimeMillis()-start)+"ms");
        return ret;
    }

    private void cleanUp(String targetDir) {
        File[] oldFiles = new File(targetDir).listFiles(file -> {
            try {
                String name = file.getName();
                BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                return name.endsWith(PDF_PNG) && attr.creationTime().toMillis()<=System.currentTimeMillis()-1200000;
            } catch (IOException e) {
                Logger.log("Failed to process old PDFs...");
                return false;
            }
        });

        if (oldFiles.length>0) {
            Logger.log("Old PDFs to cleanup: "+oldFiles.length);
            for (File file : oldFiles) {
                Logger.log("Deleting old PDF: " + file);
                boolean ok = file.delete();
                if (!ok) {
                    file.deleteOnExit();
                }
            }
        } else {
            Logger.log("No old PDFs to cleanup!");
        }
    }

}
