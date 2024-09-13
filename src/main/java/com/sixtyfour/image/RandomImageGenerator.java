package com.sixtyfour.image;

import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "Generator", urlPatterns = {"/Generator"}, initParams = {
        @WebInitParam(name = "key", value = "")})
public class RandomImageGenerator extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ServletConfig sc = getServletConfig();
        String key = new Config().getGeneratorKey();
        if (!key.equals(request.getParameter("key"))) {
            response.setStatus(401);
            return;
        }
        String query = request.getParameter("query");
        query = WordList.generateWordSoup(query);

        PrintWriter pw = response.getWriter();

        try {
            Logger.log("Query is: "+query);
            List<String> images = new DalleImageGenerator().createImages(query);
            images.forEach(p -> pw.print(p));
        } catch (Exception a) {
            Logger.log("Failed to create image!", a);
            pw.println("Failed to create image: "+a);
            response.setStatus(500);
        }
    }
}
