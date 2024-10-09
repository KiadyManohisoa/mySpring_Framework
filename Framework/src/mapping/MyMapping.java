package mapping;

import java.lang.reflect.*;

import annotation.Post;
import jakarta.servlet.http.*;
import servlets.FrontControlleur;
import util.Convertor;
import util.Syntaxe;

public class MyMapping {

    String className;
    Method method;
    String verb; // méthode http : get,post, delete etc...

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
    public Object invokeMethode(HttpServletRequest request) throws Exception {
        Object objectReturned = new Object();
        try {
            Class<?> clazz = Class.forName(this.getClassName());
            Method mConcerned = this.getMethod();
            Object invokingObject = clazz.getDeclaredConstructor().newInstance();

            Object[] invokeParams = initializeParameters(mConcerned, request);
            Runnable sessionCallbackAsField = new FrontControlleur().checkSessionField(invokingObject, request);
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

    public MyMapping(String className, Method method) {
        this.setClassName(className);
        this.setMethod(method);
        this.setVerb();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getVerb() {
        return verb;
    }

    void setVerb() {
        this.setVerb("get"); // default http method
        if (this.getMethod().isAnnotationPresent(Post.class)) {
            this.setVerb("post");
        }
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

}
