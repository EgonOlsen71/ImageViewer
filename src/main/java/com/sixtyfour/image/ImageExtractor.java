package com.sixtyfour.image;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 *
 */
public class ImageExtractor {

    public static List<String> extractImages(String url) throws Exception {
        List<String> images = new ArrayList<>();
        String html;
        String lhtml;
        long start =0;

        try (InputStream input = new URL(url).openStream(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            input.transferTo(bos);
            if (bos.size()>2048*1024) {
                Logger.log("Page too large: "+url);
                return null;
            }
            start = System.currentTimeMillis();
            html = bos.toString("UTF-8");
            lhtml = html.toLowerCase(Locale.ENGLISH);
            if (lhtml.contains("iso-8859") || lhtml.contains("windows-1252")) {
                // Hack, that might encode slightly better in some cases...
                html = bos.toString("Windows-1252");
                lhtml = html.toLowerCase(Locale.ENGLISH);
            }
        } catch(java.io.FileNotFoundException e) {
            Logger.log("URL not found: "+url, e);
            throw e;
        } catch(Exception e) {
            Logger.log("Failed to process url: "+url, e);
            throw e;
        }

        int pos = lhtml.indexOf("<base ");

        String protocol = "https://";
        if (url.indexOf("://")!=-1) {
            protocol = url.substring(0, url.indexOf("://")+3);
        }
        int protoPos = url.indexOf("://")+3;
        int domainEnd0 = findDomainEnd("/", url, protoPos);
        int domainEnd1 = findDomainEnd("?", url, protoPos);
        int domainEnd2 = findDomainEnd(";", url, protoPos);
        int domainEnd = Math.min(url.length(), Math.min(domainEnd0, Math.min(domainEnd1, domainEnd2)));
        int urlEnd = Math.min(url.length(), Math.min(domainEnd1, domainEnd2));
        String domain =  url.substring(0, domainEnd);
        String base = url.substring(0, urlEnd);
        if (domainEnd1!=-1)
        if (pos!=-1) {
            String sbase = getSourceAttribute(lhtml, html, pos, protocol, "href");
            if (sbase!=null) {
                base = sbase;
            }
        }
        if (!base.endsWith("/")) {
            base+="/";
        }
        if (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length()-1);
        }
        Logger.log("Base is: "+base);
        Logger.log("Domain is: "+domain);

        pos = -1;
        do {
            pos = lhtml.indexOf("<img ", pos+1);
            if (pos!=-1) {
                String imgSrc = getSourceAttribute(lhtml, html, pos, protocol, "src");
                if (imgSrc!=null) {
                    if (!imgSrc.startsWith("http")) {
                        if (imgSrc.startsWith("/")) {
                            imgSrc = domain+imgSrc;
                        } else {
                            imgSrc = base+imgSrc;
                        }
                    }
                    String iimgSrc = imgSrc.toLowerCase();
                    if (iimgSrc.endsWith(".jpg") || iimgSrc.endsWith(".jpeg") || iimgSrc.endsWith(".png") || iimgSrc.endsWith(".webp")) {
                        imgSrc = encode(imgSrc);
                        if (!images.contains(imgSrc)) {
                            images.add(imgSrc);
                        }
                    }
                }
            }
        } while(pos!=-1);

        Logger.log("Page parsed in "+(System.currentTimeMillis()-start)+"ms");
        Logger.log("Images found: "+ images.size());
        return images;
    }

    private static String encode(String imgSrc) {
        try {
            //@todo improve this hack...
            return URLEncoder.encode(imgSrc, "UTF-8").replace("%3A", ":").replace("%2F", "/").replace("%3B", ";").replace("%26", "&").replace("%3F", "?");
        } catch(Exception e) {
            return imgSrc;
        }
    }

    private static int findDomainEnd(String endMarker, String url, int pos) {
        int domainEnd = url.indexOf(endMarker, pos);
        if (domainEnd==-1) {
            domainEnd=url.length();
        }
        return domainEnd;
    }

    private static String getSourceAttribute(String lhtml, String html, int pos, String protocol, String attribute) {
        String src=null;
        int endPos0 = lhtml.indexOf("/>", pos);
        int endPos1 = lhtml.indexOf(">", pos);
        int endPos = endPos1;
        if (endPos0!=-1 && endPos0<endPos1) {
            endPos = endPos0;
        }
        int srcPos = lhtml.indexOf(attribute+"=", pos);
        if (srcPos!=-1 && srcPos<endPos && endPos< pos+300) {
            int srcEndPos = lhtml.indexOf(" ", srcPos);
            if (srcEndPos==-1 || srcEndPos>endPos) {
                srcEndPos = endPos;
            }
            src = html.substring(srcPos+attribute.length()+1, srcEndPos);
            src = src.replace("\"", "").replace("'", "");
            src = src.trim();
        }

        if (src!=null) {
            if (src.startsWith("//")) {
                src = protocol+src.substring(2);
            }
        }

        return src;
    }

}
