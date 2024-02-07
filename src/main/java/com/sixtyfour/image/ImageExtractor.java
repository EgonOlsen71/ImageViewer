package com.sixtyfour.image;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Extracts images from a website
 *
 * @author EgonOlsen
 */
public class ImageExtractor {

    private static final String[] PAGES = {".html", "htm", ".php", ".jsp", ".py", ".asp", ".js", ".txt", ".xhtml", ".dhtml", ".rhtml"};
    private static final String PROTOCOL = "https://";
    private static final int MAX_TAG_LENGTH = 500;
    private static final int MAX_PAGE_SIZE = 2048 * 2048;

    public List<String> extractImages(String url) throws Exception {
        List<String> images = new ArrayList<>();
        String html;
        String lhtml;
        long start;

        try (InputStream input = new URL(url).openStream(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            input.transferTo(bos);
            if (bos.size() > MAX_PAGE_SIZE) {
                Logger.log("Page too large: " + url);
                return null;
            }
            start = System.currentTimeMillis();
            html = bos.toString(StandardCharsets.UTF_8);
            lhtml = html.toLowerCase(Locale.ENGLISH);
            if (lhtml.contains("iso-8859") || lhtml.contains("windows-1252")) {
                // Hack, that might encode slightly better in some cases...
                html = bos.toString("Windows-1252");
                lhtml = html.toLowerCase(Locale.ENGLISH);
            }
            Logger.log("HTML size: " + html.length() + " bytes");
        } catch (java.io.FileNotFoundException e) {
            Logger.log("URL not found: " + url, e);
            throw e;
        } catch (Exception e) {
            Logger.log("Failed to process url: " + url, e);
            throw e;
        }

        String protocol = PROTOCOL;
        if (url.contains("://")) {
            protocol = url.substring(0, url.indexOf("://") + 3);
        }
        int domainEnd = findDomainEnd(url, "/", "?", ";");
        int urlEnd = findDomainEnd(url, "?", ";");

        String domain = url.substring(0, domainEnd);
        String base = getBase(url, urlEnd, lhtml, html, protocol);
        if (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }
        Logger.log("Base is: " + base);
        Logger.log("Domain is: " + domain);

        extractImages(lhtml, html, protocol, domain, base, images);

        Logger.log("Page parsed in " + (System.currentTimeMillis() - start) + "ms");
        Logger.log("Images found: " + images.size());
        return images;
    }

    private void extractImages(String lhtml, String html, String protocol, String domain, String base, List<String> images) {
        int pos = -1;
        do {
            pos = lhtml.indexOf("<img ", pos + 1);
            if (pos != -1) {
                String imgSrc = getSourceAttribute(lhtml, html, pos, protocol, "src");
                if (imgSrc != null) {
                    if (imgSrc.startsWith("./")) {
                        imgSrc = imgSrc.substring(1);
                    }
                    if (!imgSrc.startsWith("http")) {
                        if (imgSrc.startsWith("/")) {
                            imgSrc = domain + imgSrc;
                        } else {
                            imgSrc = base + imgSrc;
                        }
                    }
                    String iimgSrc = imgSrc.toLowerCase();
                    if (iimgSrc.contains(".jpg") || iimgSrc.contains(".jpeg") || iimgSrc.contains(".png") || iimgSrc.contains(".webp")) {
                        imgSrc = UrlUtils.encode(imgSrc);
                        if (!images.contains(imgSrc)) {
                            images.add(imgSrc);
                        }
                    }
                }
            }
        } while (pos != -1);
    }

    private String getBase(String url, int urlEnd, String lhtml, String html, String protocol) {
        int pos = lhtml.indexOf("<base ");
        String base = url.substring(0, urlEnd);
        if (pos != -1) {
            String sbase = getSourceAttribute(lhtml, html, pos, protocol, "href");
            if (sbase != null) {
                base = sbase;
            }
        }
        String lbase = base.toLowerCase();
        for (String page : PAGES) {
            if (lbase.contains(page)) {
                pos = lbase.lastIndexOf(page);
                int cutOff = lbase.lastIndexOf("/", pos);
                if (cutOff != -1) {
                    base = base.substring(0, cutOff);
                }
            }
        }
        if (!base.endsWith("/")) {
            base += "/";
        }
        return base;
    }

    private int findDomainEnd(String url, String... endMarker) {
        int domainEnd = url.length();
        int pos = url.indexOf("://") + 3;
        for (String endy : endMarker) {
            int domainEndTmp = url.indexOf(endy, pos);
            if (domainEndTmp == -1) {
                domainEndTmp = url.length();
            }
            domainEnd = Math.min(domainEndTmp, domainEnd);
        }
        return domainEnd;
    }

    private String getSourceAttribute(String lhtml, String html, int pos, String protocol, String attribute) {
        String src = null;
        int endPos0 = lhtml.indexOf("/>", pos);
        int endPos1 = lhtml.indexOf(">", pos);
        int endPos = endPos1;
        if (endPos0 != -1 && endPos0 < endPos1) {
            endPos = endPos0;
        }
        int srcPos = lhtml.indexOf(attribute + "=", pos);
        if (srcPos != -1 && srcPos < endPos && endPos < pos + MAX_TAG_LENGTH) {
            int srcEndPos = lhtml.indexOf(" ", srcPos);
            if (srcEndPos == -1 || srcEndPos > endPos) {
                srcEndPos = endPos;
            }
            src = html.substring(srcPos + attribute.length() + 1, srcEndPos);
            src = src.replace("\"", "").replace("'", "");
            src = src.trim();
        }

        if (src != null) {
            if (src.startsWith("//")) {
                src = protocol + src.substring(2);
            }
        }

        return src;
    }

}
