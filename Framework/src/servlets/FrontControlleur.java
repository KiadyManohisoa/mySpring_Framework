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
import com.google.gson.Gson;

import com.google.gson.Gson;

public class FrontControlleur extends HttpServlet {

    private String basePackage;
    HashMap<String, MyMapping> mappings = new HashMap<String, MyMapping>();

    // public void printHttpSession(HttpServletRequest request) {
    // HttpSession session = request.getSession();
    // System.out.println("Here are the session " + session.getId() + "key_values");
    // Enumeration<String> sessionKeys = session.getAttributeNames();
    // while (sessionKeys.hasMoreElements()) {
    // String key = sessionKeys.nextElement();
    // System.out.println("<" + key + ">" + ":" + session.getAttribute(key));
    // }
    // System.out.println();
    // }

    public Runnable checkSessionParam(MyMapping map, HttpServletRequest request) throws Exception {
        Method mConcerned = map.getMethod();
        Parameter[] mParameters = mConcerned.getParameters();
        MySession mySession = null;
        for (int i = 0; i < mParameters.length; i++) {
            if (mParameters[i].getType().equals(MySession.class)) {
                Class<?> clazz = Class.forName(mParameters[i].getType().getName());
                mySession = (MySession) clazz.getDeclaredConstructor().newInstance();
                mySession.setKeyValues(request.getSession());
                break;
            }
        }
        if (mySession != null) {
            MySession finalSession = mySession;
            return () -> {
                finalSession.updateHttpSession(request.getSession());
            };
        }

        return () -> {
        };

    }

    public Runnable checkSessionField(Object invokingObj, HttpServletRequest request) throws Exception {
        Field[] fields = invokingObj.getClass().getDeclaredFields();
        MySession mySession = null;

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getType().equals(MySession.class)) {
                Method method = invokingObj.getClass()
                        .getDeclaredMethod("get" + Syntaxe.getSetterNorm(fields[i].getName()));
                mySession = (MySession) method.invoke(invokingObj);
                mySession.setKeyValues(request.getSession());
                break;
            }
        }
        // System.out.println("here we are manipulating MySession");

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

    void checkRequest(MyMapping map, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object valueToHandle = map.invokeMethode(request);
        // case restApi annotation in class :
        // if(map.getMethod().getDeclaringClass().isAnnotationPresent(RestApi.class)) {

        // case restApi annotation in method
        if (map.getMethod().isAnnotationPresent(RestApi.class)
                || map.getMethod().getDeclaringClass().isAnnotationPresent(RestApi.class)) {
            resolveRestRequest(valueToHandle, request, response);
        } else {
            new FrontControlleur().resolveUrl(valueToHandle, request, response);
        }
    }

    @SuppressWarnings("deprecation")
    boolean hasPassedParameters(HttpServletRequest request, HttpServletResponse response,
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

                // case MySession as a field
                Runnable sessionCallbackAsField = this.checkSessionField(invokingObject, request);
                Object object = mConcerned.invoke(invokingObject, (Object[]) invokeParams[1]);
                sessionCallbackAsField.run();

                // case MySession as a parameter
                ((Runnable) invokeParams[0]).run();

                this.resolveUrl(object, request, response);
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

    void resolveRestRequest(Object valToHandle, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        response.setContentType("application/json");
        Gson gson = new Gson();
        try {
            PrintWriter out = response.getWriter();
            if (valToHandle instanceof ModelView) {
                ModelView mv = (ModelView) valToHandle;
                if (!mv.getData().isEmpty()) {
                    HashMap<String, Object> datas = mv.getData();
                    Iterator<String> keys = datas.keySet().iterator();
                    while (keys.hasNext()) {
                        out.print(gson.toJson(datas.get(keys.next())));
                    }
                }
            } else {
                out.print(gson.toJson(valToHandle));
            }
        } catch (Exception e) {
            throw e;
        }
    }

    void resolveUrl(Object valToHandle, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");

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
                util.addMethodByAnnotation(classes.get(i), Url.class, this.mappings);
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

    void checkVerbException(MyMapping map, String verb) throws Exception {
        if (!map.getVerb().equalsIgnoreCase(verb)) {
            throw new Exception("Bad request, please verify your HTTP correspondance");
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response, String clientVerb)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        if (this.checkBuildError()) {
            return;
        }

        String servletPath = request.getServletPath();
        if (mappings.containsKey(servletPath)) {
            MyMapping map = mappings.get(servletPath);
            try {
                checkVerbException(map, clientVerb);

                // sending data to controller's method (url/request body)
                if (this.hasPassedParameters(request, response, map)) {
                    return;
                }
                // no data
                this.checkRequest(map, request, response);
            } catch (Exception e) {
                RequestDispatcher dispatcher = request
                        .getRequestDispatcher("/WEB-INF/lib/error.jsp");
                request.setAttribute("error", "ETU2375 : " + e.getLocalizedMessage());
                // e.printStackTrace();
                dispatcher.forward(request, response);
            }

        } else {
            out.println("*** ERROR : 404 NOT FOUND");
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res, "get");
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res, "post");
    }

}