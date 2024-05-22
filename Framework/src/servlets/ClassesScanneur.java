package servlets;

import java.net.URL;
import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.*;
import annotation.Controlleur;
import util.Util;

public class ClassesScanneur extends HttpServlet {

    private String basePackage;
    boolean checked = false;
    List<Class<?>> founded;

    public List<Class<?>> getFounded() {
        return founded;
    }

    public void setFounded(List<Class<?>> founded) {
        this.founded = founded;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    void initVariables() throws Exception {
        String base = this.getInitParameter("base-package");
        try {
            basePackage = base;
            this.setFounded(new Util().getClassesByAnnotation(this.basePackage, Controlleur.class));
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
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        if (!this.isChecked()) { // haven't initalized variables yet
            // out.print("first time called this servlet");
            try {
                initVariables();
                this.setChecked(true);
            } catch (Exception err) {
                out.println(err.getMessage());
            }
        }
        try {
            out.println("Liste de  tous les controlleurs du projet actuel : ");
            List<Class<?>> classes = this.getFounded();
            for (int i = 0; i < classes.size(); i++) {
                out.println(classes.get(i).getName());
            }
        } catch (Exception e) {
            out.println(e.getMessage());
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

}
