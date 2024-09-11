package mapping;

import java.lang.reflect.*;
import jakarta.servlet.http.*;
import servlets.FrontControlleur;
import util.Convertor;
import util.Syntaxe;

public class MyMapping {

    String className;
    Method method;

    Object[] initializeParameters(Method method, HttpServletRequest request) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] invObjects = new Object[parameters.length];
        Convertor convertor = new Convertor();

        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramType = parameters[i].getType();

            if (paramType.equals(MySession.class)) {
                MySession mySession = (MySession) paramType.getDeclaredConstructor().newInstance();
                // mySession.setSession(request.getSession());
                invObjects[i] = mySession;
            } else if (paramType.isPrimitive()) {
                invObjects[i] = convertor.getDefaultValue(paramType);
            } else {
                invObjects[i] = paramType.getDeclaredConstructor().newInstance();
            }
        }

        return invObjects;
    }

    @SuppressWarnings("deprecation")
    public Object[] invokeMethode(HttpServletRequest request) throws Exception {
        Object[] toReturn = new Object[2];
        toReturn[0] = new Object();
        Runnable callback = () -> {
        };
        toReturn[1] = callback;
        try {
            Class<?> clazz = Class.forName(this.getClassName());
            Method mConcerned = this.getMethod();
            Object invokingObject = clazz.getDeclaredConstructor().newInstance();

            Object[] invObjects = initializeParameters(mConcerned, request);

            if (invObjects.length > 0) {
                toReturn[0] = mConcerned.invoke(invokingObject, invObjects);
            } else {
                new FrontControlleur().printHttpSession(request);
                toReturn[1] = new FrontControlleur().checkSession(invokingObject, request);
                toReturn[0] = mConcerned.invoke(invokingObject);
            }
        } catch (Exception e) {
            throw e;
        }
        return toReturn;
    }

    public MyMapping(String className, Method method) {
        this.setClassName(className);
        this.setMethod(method);
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

}
