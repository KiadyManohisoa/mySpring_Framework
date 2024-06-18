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

    @SuppressWarnings("deprecation")
    boolean isTherePostRequest(HttpServletRequest request, HttpServletResponse response, PrintWriter out,
            MyMapping mapping)
            throws Exception {
        try {
            Map<String, String[]> parameterMap = request.getParameterMap();

            if (!parameterMap.isEmpty()) {
                Class<?> clazz = Class.forName(mapping.getClassName());
                Method mConcerned = mapping.getMethod();
                Parameter[] mParameters = mConcerned.getParameters();
                Object[] invokeParams = new Object[mParameters.length];
                boolean[] paramsAssignation = new boolean[mParameters.length];
                Convertor convertor = new Convertor();
                for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                    String inputName = entry.getKey();
                    String[] inputValue = entry.getValue();

                    for (int i = 0; i < mParameters.length; i++) {
                        if (!paramsAssignation[i]) {
                            RequestParameter annotation = mParameters[i].getAnnotation(RequestParameter.class);
                            if (annotation != null && inputName.equals(annotation.value())) {
                                invokeParams[i] = convertor.convertInputToParam(inputValue[0],
                                        mParameters[i].getType());
                                paramsAssignation[i] = true;
                                break;
                            } else if (inputName.equals(mParameters[i].getName())) {
                                invokeParams[i] = convertor.convertInputToParam(inputValue[0],
                                        mParameters[i].getType());
                                paramsAssignation[i] = true;
                                break;
                            } else {
                                invokeParams[i] = convertor.getDefaultValue(mParameters[i].getType());
                                paramsAssignation[i] = true;
                                break;
                            }
                        }
                    }
                }

                Object object = mConcerned.invoke(clazz.newInstance(), invokeParams);
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
                Object valueToHandle = map.invokeMethode();
                this.resolveUrl(valueToHandle, out, request, response);
            } catch (Exception e) {
                e.printStackTrace();
                // out.println("*** ERROR : " + e.printStackTrace(););
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