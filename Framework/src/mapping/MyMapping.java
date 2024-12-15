package mapping;

import java.lang.reflect.*;

import annotation.Post;
import jakarta.servlet.http.*;
import servlets.FrontServlet;
import util.Convertor;
import util.Syntaxe;
import java.util.Set;
import java.util.HashSet;

public class MyMapping {

    String className;
    Set<VerbMethod> verbMethods;

    public VerbMethod getVerbMethod(String verbRequest) throws Exception {
        for (VerbMethod vbm : this.getVerbMethods()) {
            if (vbm.getVerb().equalsIgnoreCase(verbRequest)) {
                return vbm;
            }
        }
        throw new Exception("Bad request, please verify your HTTP correspondance");
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
