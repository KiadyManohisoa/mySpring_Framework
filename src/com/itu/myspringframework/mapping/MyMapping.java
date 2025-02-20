package com.itu.myspringframework.mapping;

import java.lang.reflect.*;

import jakarta.servlet.http.*;
import java.util.Set;
import java.util.HashSet;

import com.itu.myspringframework.servlets.FrontServlet;
import com.itu.myspringframework.util.ConfigReader;
import com.itu.myspringframework.util.Convertor;
import com.itu.myspringframework.annotation.permission.Logged;
import com.itu.myspringframework.configuration.Configuration;
import com.itu.myspringframework.exception.PermissionException;

public class MyMapping {

    String className;
    Set<VerbMethod> verbMethods;

    public Logged hasLoggedAnnotation(Method m, HttpServletRequest request) {
        Logged annotation = null;
        if(m.getDeclaringClass().isAnnotationPresent(Logged.class)) {
            annotation = m.getDeclaringClass().getAnnotation(Logged.class);
        }
        else if (m.isAnnotationPresent(Logged.class)) {
            annotation = m.getAnnotation(Logged.class);
        }
        return annotation;
    }

    public void verifyPrivileges(Logged loggedAnnotation, Object sessionValue) throws Exception { 
        Class<?>[] allowedClasses = loggedAnnotation.value(); 
        boolean hasAccess = false; 
        for (Class<?> allowedClass : allowedClasses) { 
            if (allowedClass.isInstance(sessionValue)) { 
                hasAccess = true; 
                break; 
            } 
        } 
        if (!hasAccess) { 
            throw new PermissionException("Access denied: User does not have the required privileges."); 
        } 
    }

    public String verifyLogState(MySession mySession) throws Exception {
        String keyToSearch = new ConfigReader().getProperty(Configuration.userKeyNaming);
        if(mySession.get(keyToSearch)==null) {
            throw new PermissionException("Access to this method is restricted to authenticated users");
        }
        return keyToSearch;
    }
    
    public void verifyPermission(Method mMatched, HttpServletRequest request) throws Exception {
        Logged loggedAnnotation = this.hasLoggedAnnotation(mMatched, request); 
        if(loggedAnnotation!=null) {
            MySession mySession = new MySession();
            mySession.setKeyValues(request.getSession());
            // System.out.println("Etat de la session");
            // mySession.print();
            String keyToSearch = this.verifyLogState(mySession);
            if(loggedAnnotation.value().length>0) {
                this.verifyPrivileges(loggedAnnotation, mySession.get(keyToSearch));
            }
        }
    }

    public VerbMethod getFirstOccurrenceGet() {
        VerbMethod theVbm = null;
        for(VerbMethod vbm : this.getVerbMethods()) {
            if(vbm.getVerb().equalsIgnoreCase("GET")) {
                theVbm = vbm; break;
            }
        }
        return theVbm;
    }

    public VerbMethod getVerbMethod(String verbRequest, int checkingVbm) throws Exception {
        for (VerbMethod vbm : this.getVerbMethods()) {
            if (vbm.getVerb().equalsIgnoreCase(verbRequest)) {
                return vbm;
            }
        }
        if(checkingVbm==1) {
            throw new Exception("Bad request, please verify your HTTP correspondance");
        }
        return this.getFirstOccurrenceGet();
    }

    public void addVerbMethod(VerbMethod vbm) throws RuntimeException {
        if (!this.getVerbMethods().add(vbm)) {
            throw new RuntimeException(
                    "Different methods must have different HTTP verbs");
        }
    }

    Object[] initializeParameters(Method method, HttpServletRequest request) throws Exception {
        Object[] answers = new Object[2];

        Parameter[] parameters = method.getParameters();
        Object[] invObjects = new Object[parameters.length];
        Convertor convertor = new Convertor();

        Runnable sessionCallback = () -> {
        };
        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramType = parameters[i].getType();

            if (paramType.equals(MySession.class)) {
                MySession mySession = (MySession) paramType.getDeclaredConstructor().newInstance();
                // mySession.setSession(request.getSession());
                mySession.setKeyValues(request.getSession());
                sessionCallback = () -> {
                    mySession.updateHttpSession(request.getSession());
                };
                invObjects[i] = mySession;
            } else if (paramType.isPrimitive()) {
                invObjects[i] = convertor.getDefaultValue(paramType);
            } else {
                invObjects[i] = paramType.getDeclaredConstructor().newInstance();
            }
        }
        answers[0] = invObjects;
        answers[1] = sessionCallback;
        return answers;
    }

    @SuppressWarnings("deprecation")
    public Object invokeMethode(HttpServletRequest request, Method mConcerned, FrontServlet frontServlet)
            throws Exception {
        Object objectReturned = new Object();
        try {
            Class<?> clazz = Class.forName(this.getClassName());
            Object invokingObject = clazz.getDeclaredConstructor().newInstance();

            Object[] invokeParams = initializeParameters(mConcerned, request);
            Runnable sessionCallbackAsField = frontServlet.checkSessionField(invokingObject, request);
            objectReturned = mConcerned.invoke(invokingObject, (Object[]) invokeParams[0]);

            // case MySession as a field
            sessionCallbackAsField.run();

            // case MySession as a parameter
            ((Runnable) invokeParams[1]).run();

        } catch (Exception e) {
            throw e;
        }
        return objectReturned;
    }

    public void print() {
        System.out.println("For this mapping with the className " + this.getClassName() + " size:"
                + this.getVerbMethods().size() + " it's verbmethods");
        for (VerbMethod vbm : this.getVerbMethods()) {
            System.out.println("\t" + vbm.getVerb() + " | " + vbm.getMethod().getDeclaringClass().getName()
                    + "/" + vbm.getMethod().getName());
        }
    }

    public MyMapping(String className, VerbMethod verbMethod) {
        this.setClassName(className);
        this.verbMethods = new HashSet<>();
        this.addVerbMethod(verbMethod);
    }

    public Set<VerbMethod> getVerbMethods() {
        return verbMethods;
    }

    public void setVerbMethods(Set<VerbMethod> verbMethods) {
        this.verbMethods = verbMethods;
    }

    public MyMapping(String className) {
        this.setClassName(className);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

}
