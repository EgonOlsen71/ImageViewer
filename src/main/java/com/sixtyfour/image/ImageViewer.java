package com.sixtyfour.image;

import com.sixtyfour.petscii.KoalaConverter;
import com.sixtyfour.petscii.TargetDimensions;
import com.sixtyfour.petscii.Vic2Colors;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to download an image
 * 
 * @author EgonOlsen
 */
@WebServlet(name = "ImageViewer", urlPatterns = { "/ImageViewer" }, initParams = {
		@WebInitParam(name = "imagepath", value = "/imagedata/") })
public class ImageViewer extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static LinkedHashMap<String, String> urlShortener = new LinkedHashMap<>() {
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return this.size()>2000;
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
		ServletOutputStream os = response.getOutputStream();
		String file = URLDecoder.decode(request.getParameter("file"), "UTF-8");

		if (urlShortener.containsKey(file)) {
			Logger.log("Replacing URL "+file+ "with "+urlShortener.get(file));
			file = urlShortener.get(file);
		}

		String dither = request.getParameter("dither");
		boolean keepRatio = Boolean.parseBoolean(request.getParameter("ar"));
		if (file.contains("..") || file.contains("\\") || file.startsWith("/")) {
			Logger.log("Invalid file name: " + file);
			printError(os, "Invalid file name!");
			return;
		}
		float dithy = 1;
		if (dither!=null) {
			try {
				dithy = Float.parseFloat(dither)/100f;
			} catch(Exception e) {
				//
			}
		}
		dithy = Math.min(1, Math.max(0, dithy));

		if (!file.toLowerCase().startsWith("http")) {
			file = "https://"+file;
		}

		Logger.log("Dithering is set to "+dithy);
		if (!file.endsWith(".png") && !file.endsWith(".jpg") && !file.endsWith(".jpeg") && !file.endsWith(".webp")) {
			Logger.log("Unsupported file type: " + file);
			Logger.log("Trying to extract images from page...");
			extractImages(file, os);
			return;
		}

		Logger.log("Downloading image: " + file);
		ServletConfig sc = getServletConfig();
		String path = sc.getInitParameter("imagepath");

		String ext = file.substring(file.lastIndexOf("."));

		String targetFile = UUID.randomUUID()+ext;
		File pathy = new File(path);
		pathy.mkdirs();
		File bin = new File(pathy, targetFile);

		try (InputStream input = new URL(file).openStream();FileOutputStream fos = new FileOutputStream(bin)) {
			input.transferTo(fos);
		} catch(java.io.FileNotFoundException e) {
			Logger.log("File not found: "+file, e);
			printError(os, "Image not found!");
			delete(bin);
			return;
		} catch(Exception e) {
			Logger.log("Failed to load image: "+file, e);
			printError(os, "Failed to load image!");
			delete(bin);
			return;
		}

		String fileName = bin.toString();
		String targetFileName = fileName+".koa";
		File targetBin = new File(targetFileName);
		try {
			KoalaConverter.convert(fileName, targetFileName, new Vic2Colors(), 1, dithy, keepRatio, false);
		} catch(Exception e) {
			delete(targetBin);
			delete(bin);
			Logger.log("Failed to convert image: "+file, e);
			if (e.getMessage()!=null) {
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

	private void extractImages(String file, ServletOutputStream os) {
		List<String> images;
		try {
			images = ImageExtractor.extractImages(file);
		}  catch(FileNotFoundException e) {
			Logger.log("URL not found: "+file, e);
			printError(os, "URL not found!");
			return;
		} catch(UnknownHostException e) {
			Logger.log("Unknown host: "+file, e);
			printError(os, "Unknown host!");
			return;
		} catch(Exception e) {
			Logger.log("Failed to extract images from "+file, e);
			printError(os, "No valid images found!");
			return;
		}

		if (images==null) {
			printError(os, "No valid images found!");
			return;
		}

		for (int i=0; i<images.size(); i++) {
			String image = images.get(i);
			if (image.length()<170) {
				continue;
			}
			String newImage = "https://jpct.de/"+UUID.randomUUID()+".short";
			Logger.log("URL too long, transmitting a short form instead!");
			urlShortener.put(newImage, image);
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
		if (images!=null && images.size()>22) {
			images = images.subList(0, 22);
			Logger.log("Limited image list to "+images.size());
		}
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(new byte[]{1, 1}); // Flag image list to C64

			byte[] len = new byte[1];
			for (String img:images) {
				byte[] txt = img.getBytes(StandardCharsets.US_ASCII);
				len[0] = (byte) (txt.length & 0xff);
				bos.write(len);
				bos.write(txt);
			}
			len[0]=0;
			bos.write(len);
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			bis.transferTo(os);
		} catch(Exception e) {
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
		} catch(Exception e) {
			Logger.log("Failed to write error into stream!", e);
		}
	}
}