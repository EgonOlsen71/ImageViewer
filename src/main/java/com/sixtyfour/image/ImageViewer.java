package com.sixtyfour.image;

import com.sixtyfour.petscii.KoalaConverter;
import com.sixtyfour.petscii.Vic2Colors;

import java.io.*;
import java.net.URL;
import java.util.UUID;

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
		String file = request.getParameter("file");
		String dither = request.getParameter("dither");
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

		Logger.log("Dithering is set to "+dithy);
		if (!file.endsWith(".png") && !file.endsWith(".jpg") && !file.endsWith(".koa") && !file.endsWith(".jpeg")) {
			Logger.log("Unsupported file type: " + file);
			printError(os, "Unsupported file type!");
			return;
		}

		if (!file.toLowerCase().startsWith("http")) {
			file = "https://"+file;
		}

		Logger.log("Downloading image: " + file);
		ServletConfig sc = getServletConfig();
		String path = sc.getInitParameter("imagepath");

		String targetFile = UUID.randomUUID()+".koa";
		File pathy = new File(path);
		pathy.mkdirs();
		File bin = new File(pathy, targetFile);

		try (InputStream input = new URL(file).openStream();FileOutputStream fos = new FileOutputStream(bin)) {
			input.transferTo(fos);
		} catch(java.io.FileNotFoundException e) {
			Logger.log("File not found: "+file, e);
			printError(os, "Image not found!");
			return;
		} catch(Exception e) {
			Logger.log("Failed to load image: "+file, e);
			printError(os, "Failed to load image!");
			return;
		}

		String fileName = bin.toString();
		String targetFileName = fileName+".koa";
		KoalaConverter.convert(fileName, targetFileName, new Vic2Colors(), 1, dithy, false);
		File targetBin = new File(targetFileName);

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