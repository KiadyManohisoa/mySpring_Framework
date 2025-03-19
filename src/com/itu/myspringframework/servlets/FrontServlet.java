package com.itu.myspringframework.servlets;

import java.io.*;
import java.lang.reflect.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;

import java.util.*;
import com.itu.myspringframework.annotation.*;
import com.itu.myspringframework.exception.ValidationException;
import com.itu.myspringframework.util.*;
import com.itu.myspringframework.mapping.*;
import com.google.gson.Gson;

@MultipartConfig
public class FrontServlet extends HttpServlet {

    private String basePackage;
    HashMap<String, MyMapping> mappings;

    String getBaseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" +
                request.getServerName() +
                ":" +
                request.getServerPort() +
                request.getContextPath();
    }

    public void goToPreviousRessource(Object errorHandler, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String clientVerb = "GET";
        String previousUrl = request.getParameter("origin");
        if (mappings.containsKey(previousUrl)) {
            MyMapping mapping = mappings.get(previousUrl);
            Method mMatched = this.checkVerbException(mapping, clientVerb);
            ModelView mV = (ModelView) mapping.invokeMethode(request, mMatched, this);
            mV.add("dataError", errorHandler);
            this.resolveRequest(mapping, mMatched, request, response, mV);
        }
    }

    public Runnable checkSessionParam(Method mConcerned, HttpServletRequest request) throws Exception {
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
                        .getDeclaredMethod("get" + Syntaxe.getSetterGetterNorm(fields[i].getName()));
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

    void resolveRequest(MyMapping map, Method mMatched, HttpServletRequest request, HttpServletResponse response,
            Object valueToHandle)
            throws Exception {
        // case restApi annotation in method
        if (mMatched.isAnnotationPresent(RestApi.class)
                || mMatched.getDeclaringClass().isAnnotationPresent(RestApi.class)) {
            resolveRestRequest(valueToHandle, request, response);
        } else {
            this.resolveUrl(valueToHandle, request, response);
        }
    }

    @SuppressWarnings("deprecation")
    boolean hasPassedParameters(HttpServletRequest request, HttpServletResponse response,
            MyMapping mapping, Method mConcerned)
            throws Exception {
        try {
            Map<String, String[]> parameterMap = request.getParameterMap();

            if (!parameterMap.isEmpty() || (request.getContentType() != null
                    && request.getContentType().toLowerCase().startsWith("multipart/"))) {
                Reflect reflect = new Reflect();
                RunnableWrapper rnw = new RunnableWrapper();
                Class<?> clazz = Class.forName(mapping.getClassName());
                Parameter[] methodParameters = mConcerned.getParameters();
                Object invokingObject = clazz.getDeclaredConstructor().newInstance();

                Object[] invokeParams = reflect.prepareInvokeParams(parameterMap,
                        methodParameters, invokingObject, request, rnw);

                // Class<?>[] parameterTypes = mConcerned.getParameterTypes();
                // System.out.println("Types attendus : " + Arrays.toString(parameterTypes));

                // for (Object param : (Object[]) invokeParams[1]) {
                //     System.out.println("Type reçu : " + (param != null ? param.getClass() : "null"));
                // }                

                // case MySession as a field
                Runnable sessionCallbackAsField = this.checkSessionField(invokingObject, request);
                Object object = mConcerned.invoke(invokingObject, (Object[]) invokeParams[1]);
                sessionCallbackAsField.run();

                // case MySession as a parameter
                rnw.getCallback().run();

                this.resolveRequest(mapping, mConcerned, request, response, object);

                // this.resolveUrl(object, request, response);
                return true;
            }
        } catch (Exception e) {
            if (e instanceof ValidationException) {
                ValidationException vE = (ValidationException) e;
                this.goToPreviousRessource(vE.getErrorToPass(), request, response);
                return true;
            }
            throw e;
        }
        return false;
    }

    void checkBuildError() throws Exception {
        String buildErr = (String) this.getServletContext().getAttribute("buildError");
        if (buildErr != null) {
            throw new Exception(buildErr);
            // System.out.println("\n*** ERROR : " + buildErr + "\n");
        }
    }

    // boolean checkBuildError() {
    // boolean result = false;
    // String buildErr = (String)
    // this.getServletContext().getAttribute("buildError");
    // if (buildErr != null) {
    // System.out.println("\n*** ERROR : " + buildErr + "\n");
    // result = true;
    // }
    // return result;
    // }

    void writeModifiedContent(HttpServletResponse response, String modifiedContent) throws IOException {
        PrintWriter out = response.getWriter();
        out.write(modifiedContent);
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

    void doRedirect(HttpServletRequest request, HttpServletResponse response, ModelView mv) throws Exception {
        String newUrl = request.getContextPath() + Util.getPathWithoutRedirect(mv.getUrl());
        response.sendRedirect(newUrl + mv.generateParameters());
    }

    void doForward(HttpServletRequest request, HttpServletResponse response, ModelView mv) throws Exception {
        CharResponseWrapper responseWrapper = new CharResponseWrapper(response);
        if (!mv.getData().isEmpty()) {
            HashMap<String, Object> datas = mv.getData();
            Iterator<String> keys = datas.keySet().iterator();
            while (keys.hasNext()) {
                String dataKey = keys.next();
                request.setAttribute(dataKey, datas.get(dataKey));
            }
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher(mv.getUrl());
        dispatcher.forward(request, responseWrapper);

        String modifiedContent = responseWrapper.processJspContent(responseWrapper.toString(), request, this);
        // System.out.println("tafanova anle contenu tq : " + modifiedContent);
        this.writeModifiedContent(response, modifiedContent);
    }

    void resolveUrl(Object valToHandle, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        if (valToHandle instanceof String) {
            out.println((String) valToHandle);
        } else if (valToHandle instanceof ModelView) {
            ModelView mv = (ModelView) valToHandle;
            try {
                if(!Util.isRedirect(mv.getUrl())) {
                    this.doForward(request, response, mv);
                }
                else {
                    this.doRedirect(request, response, mv);
                }
            } catch (Exception e) {
                throw e;
            }
        } else {
            throw new Exception("The return type " + valToHandle.getClass().getName()
            + " is not supported for URL assignment");
    
        }
    }

    void initVariables() throws Exception {
        String base = this.getInitParameter("base-package");
        Util util = new Util();
        this.mappings = new HashMap<String, MyMapping>();
        try {
            basePackage = base;
            List<Class<?>> classes = util.getClassesByAnnotation(basePackage, Controller.class);
            // init hashMaps with Controlleur and Url annotation
            for (int i = 0; i < classes.size(); i++) {
                util.addMethodByAnnotation(classes.get(i), Url.class, this.mappings);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void init() {
        try {
            initVariables();
            // this.displayMyMappingHashMap();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                this.getServletContext().setAttribute("buildError", e.getMessage());
            }
            System.out.println("\n*** ERROR : " + e.getMessage() + "\n");
        }
    }

    Method checkVerbException(MyMapping map, String verb) throws Exception {
        return map.getVerbMethod(verb).getMethod();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        String clientVerb = request.getMethod();
        try {
            this.checkBuildError();
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
        }

        String servletPath = request.getServletPath();

        if (mappings.containsKey(servletPath)) {
            MyMapping map = mappings.get(servletPath);
            try {
                Method mMatched = this.checkVerbException(map, clientVerb);
                map.verifyPermission(mMatched, request);

                // sending data to controller's method (url/request body)
                if (this.hasPassedParameters(request, response, map, mMatched)) {
                    return;
                }
                // no data
                Object invokedObject = map.invokeMethode(request, mMatched, this);
                this.resolveRequest(map, mMatched, request, response, invokedObject);
            } catch (Exception e) {
                RequestDispatcher dispatcher = request
                        .getRequestDispatcher("/WEB-INF/lib/error.jsp");
                request.setAttribute("error", "ETU002375 : " + e.getLocalizedMessage());
                e.printStackTrace();
                dispatcher.forward(request, response);
            }

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Url "+servletPath+" introuvable");
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    // void showError(HttpServletResponse response, String message, int errorValue)
    // throws Exception {
    // response.setStatus(errorValue);
    // response.setContentType("text/html");
    // PrintWriter out = response.getWriter();
    // out.println("<html>");
    // out.println("<head> <title> Une erreur s'est produite </title> </head>");
    // out.println("<body>");
    // out.println("<h1> Erreur " + errorValue + " </h1>");
    // out.println("<p>" + message + "</p>");
    // out.println("</body> </html>");

    // }

    // void displayMyMappingHashMap() {
    // for (HashMap.Entry<String, MyMapping> entry : this.mappings.entrySet()) {
    // System.out.println("\nPour la clé " + entry.getKey() + " associé au Mapping
    // de nom de classe "
    // + entry.getValue().getClassName() + " ont les verbmethods length "
    // + entry.getValue().getVerbMethods().size());
    // Set<VerbMethod> vbm = entry.getValue().getVerbMethods();
    // for (VerbMethod vb : vbm) {
    // System.out.println("\t" + vb.getVerb() + " | " +
    // vb.getMethod().getDeclaringClass().getName()
    // + "/" + vb.getMethod().getName());
    // }
    // }
    // }

    // print methods
    // void printHttpSession(HttpServletRequest request) {
    //     HttpSession session = request.getSession();
    //     System.out.println("Here are the session " + session.getId() + " key_values ");
    //     Enumeration<String> sessionKeys = session.getAttributeNames();
    //     while (sessionKeys.hasMoreElements()) {
    //     String key = sessionKeys.nextElement();
    //     System.out.println("<" + key + ">" + ":" + session.getAttribute(key));
    //     }
    //     System.out.println();
    // }

}