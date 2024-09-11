package servlets;

import java.net.*;
import java.net.http.HttpRequest;
import java.io.*;
import java.lang.reflect.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.util.*;
import annotation.*;
import util.*;
import mapping.*;

public class FrontControlleur extends HttpServlet {

    private String basePackage;
    HashMap<String, MyMapping> mappings = new HashMap<String, MyMapping>();

    public void printHttpSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        System.out.println("Here are the session " + session.getId() + "key_values");
        Enumeration<String> sessionKeys = session.getAttributeNames();
        while (sessionKeys.hasMoreElements()) {
            String key = sessionKeys.nextElement();
            System.out.println("<" + key + ">" + ":" + session.getAttribute(key));
        }
        System.out.println();
    }

    public Runnable checkSession(Object invokingObj, HttpServletRequest request) throws Exception {
        Field[] fields = invokingObj.getClass().getDeclaredFields();
        MySession mySession = null;

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getType().equals(MySession.class)) {
                Method method = invokingObj.getClass()
                        .getDeclaredMethod("get" + Syntaxe.getSetterNorm(fields[i].getName()));
                mySession = (MySession) method.invoke(invokingObj);
                mySession.setKeyValues(request.getSession());
            }
        }
        System.out.println("here we are manipulating MySession");

        if (mySession != null) { // if there is a field session, we return the callback in order to update the
                                 // session
            MySession finalSession = mySession;
            return () -> {
                // System.out.println("Updating HTTP session in callback \n Status :");
                // finalSession.print();
                finalSession.updateHttpSession(request.getSession());
            };
        }

        // if there was no field session, we return an empty callback
        return () -> {
        };
    }

    // mySession.setSession(request.getSession());

    @SuppressWarnings("deprecation")
    boolean isTherePostRequest(HttpServletRequest request, HttpServletResponse response, PrintWriter out,
            MyMapping mapping)
            throws Exception {
        try {
            Map<String, String[]> parameterMap = request.getParameterMap();

            if (!parameterMap.isEmpty()) {
                Class<?> clazz = Class.forName(mapping.getClassName());
                Method mConcerned = mapping.getMethod();
                Parameter[] methodParameters = mConcerned.getParameters();
                Object invokingObject = clazz.getDeclaredConstructor().newInstance();

                Object[] invokeParams = new Reflect().prepareInvokeParams(parameterMap,
                        methodParameters, invokingObject, request);

                this.printHttpSession(request);

                Runnable sessionCallback = this.checkSession(invokingObject, request);
                Object object = mConcerned.invoke(invokingObject, invokeParams);
                sessionCallback.run();

                this.printHttpSession(request);

                this.resolveUrl(object, out, request, response);
                return true;
            }
        } catch (Exception e) {
            throw e;
        }
        return false;
    }

    boolean checkBuildError() {
        boolean result = false;
        String buildErr = (String) this.getServletContext().getAttribute("buildError");
        if (buildErr != null) {
            System.out.println("\n*** ERROR : " + buildErr + "\n");
            result = true;
        }
        return result;
    }

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
                this.getServletContext().setAttribute("buildError", e.getMessage());
            }
            System.out.println("\n*** ERROR : " + e.getMessage() + "\n");
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        if (this.checkBuildError()) {
            return;
        }

        String servletPath = request.getServletPath();
        if (mappings.containsKey(servletPath)) {
            MyMapping map = mappings.get(servletPath);
            try {
                if (this.isTherePostRequest(request, response, out, map)) {
                    return;
                }
                Object[] valueToHandle = map.invokeMethode(request);
                ((Runnable) valueToHandle[1]).run();
                this.resolveUrl(valueToHandle[0], out, request, response);
            } catch (Exception e) {
                RequestDispatcher dispatcher = request
                        .getRequestDispatcher("/WEB-INF/lib/error.jsp");
                request.setAttribute("error", "ETU2375 : " + e.getLocalizedMessage());
                dispatcher.forward(request, response);
            }

        } else {
            out.println("*** ERROR : 404 NOT FOUND");
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

}