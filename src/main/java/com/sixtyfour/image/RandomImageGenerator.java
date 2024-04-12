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
        String org = query;
        query = "an image depicting "+query+". ";
        query += org+" is located at "+WordList.getRandomWord();
        query += " and also "+WordList.getRandomWord()+" among "+WordList.getRandomWord()+"."+WordList.getRandomWord();

        String[] fillers = {" and in ", " seems to ", " may ", " and ", " or ", " but ", " except for ", " on a ", " behind a ", " in front of ", " but not ", " gazing at "};

        for (int i=0; i<8; i++) {
            String word = WordList.getRandomWord();
            query += fillers[(int) (Math.random()*fillers.length)]+ word;
        }

        PrintWriter pw = response.getWriter();

        query+=".(3)";
        try {
            Logger.log("Query is: "+query);
            List<String> images = AiImageGenerator.createImages(query);
            images.forEach(p -> pw.println(p));
        } catch (Exception a) {
            Logger.log("Failed to create image!", a);
            pw.println("Failed to create image: "+a);
            response.setStatus(500);
        }
    }
}
