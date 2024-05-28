package servlets;

import java.net.*;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.*;
import annotation.*;
import util.*;
import mapping.MyMapping;

public class FindUrlsMethod extends HttpServlet {

    private String basePackage;
    HashMap<String, MyMapping> mappings = new HashMap<String, MyMapping>();

    void initVariables() throws Exception {
        String base = this.getInitParameter("base-package");
        Util util = new Util();
        try {
            basePackage = base;
            List<Class<?>> classes = util.getClassesByAnnotation(basePackage, Controlleur.class);
            // init hashMaps with Controlleur and Get annotation
            for (int i = 0; i < classes.size(); i++) {
                util.addMethodByAnnotation(classes.get(i), Get.class, this.mappings);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void init() {
        try {
            initVariables();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String servletPath = request.getServletPath();
        if (mappings.containsKey(servletPath)) {
            // out.println("Pour l'url : " + servletPath);
            // MyMapping map = mappings.get(servletPath);
            // out.println("<br/> La classe associée : " + map.getClassName());
            // out.println("<br/> La méthode associée : " + map.getMethodName());
            MyMapping map = mappings.get(servletPath);
            try {
                out.println(map.invokeMethode());
            } catch (Exception e) {
                out.println(e.getMessage());
            }

        } else {
            out.println("*** ERROR : URL NOT FOUND");
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

}