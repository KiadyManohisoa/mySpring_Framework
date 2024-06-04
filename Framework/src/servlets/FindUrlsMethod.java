package servlets;

import java.net.*;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.*;
import annotation.*;
import util.*;
import mapping.*;

public class FindUrlsMethod extends HttpServlet {

    private String basePackage;
    HashMap<String, MyMapping> mappings = new HashMap<String, MyMapping>();

    void resolveUrl(Object valToHandle, PrintWriter out, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (valToHandle instanceof String) {
            out.println((String) valToHandle);
        } else if (valToHandle instanceof ModelView) {
            ModelView mv = (ModelView) valToHandle;
            try {
                RequestDispatcher dispatcher = request.getRequestDispatcher(mv.getUrl());
                if (!mv.getData().isEmpty()) {
                    HashMap<String, Object> datas = mv.getData();
                    Iterator<String> keys = datas.keySet().iterator();
                    while (keys.hasNext()) {
                        String dataKey = keys.next();
                        request.setAttribute(dataKey, datas.get(dataKey));
                    }
                }
                dispatcher.forward(request, response);
            } catch (Exception e) {
                throw e;
            }
        } else {
            throw new Exception("Le type de retour " + valToHandle.getClass().getName()
                    + " n'est pas pris en charge pour l'assignation d'url");
        }
    }

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
            if (e instanceof RuntimeException) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
            e.printStackTrace();
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String servletPath = request.getServletPath();
        if (mappings.containsKey(servletPath)) {
            MyMapping map = mappings.get(servletPath);
            try {
                Object valueToHandle = map.invokeMethode();
                this.resolveUrl(valueToHandle, out, request, response);
            } catch (Exception e) {
                out.println("*** ERROR : " + e.getMessage());
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