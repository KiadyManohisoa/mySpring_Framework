package mapping;

import java.lang.reflect.*;
import jakarta.servlet.http.*;

import util.Syntaxe;

public class MyMapping {

    String className;
    Method method;

    public void updateSession(Object invokingObj, HttpServletRequest request, int idField) throws Exception {
        Field[] fields = invokingObj.getClass().getDeclaredFields();
        Method method = invokingObj.getClass()
                .getDeclaredMethod("get" + Syntaxe.getSetterNorm(fields[idField].getName()));
        MySession mySession = (MySession) method.invoke(invokingObj);
        HttpSession session = mySession.getSession();
        HttpSession requestSession = request.getSession();
        requestSession = session;
    }

    public int checkSession(Object invokingObj, HttpServletRequest request) throws Exception {
        Field[] fields = invokingObj.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getType().equals(MySession.class)) {
                Method method = invokingObj.getClass()
                        .getDeclaredMethod("get" + Syntaxe.getSetterNorm(fields[i].getName()));
                MySession mySession = (MySession) method.invoke(invokingObj);
                mySession.setSession(request.getSession());
                return i;
            }
        }
        return -1;
    }

    @SuppressWarnings("deprecation")
    public Object invokeMethode(HttpServletRequest request) throws Exception {
        Object answer = null;
        try {
            Class<?> clazz = Class.forName(this.getClassName());
            Method mConcerned = clazz.getDeclaredMethod(this.getMethod().getName());

            Object invokingObject = clazz.getDeclaredConstructor().newInstance();
            int idField = this.checkSession(invokingObject, request);
            if (idField != -1) {
                this.updateSession(invokingObject, request, idField);
            }
            answer = mConcerned.invoke(invokingObject, new Object[] {});
        } catch (Exception e) {
            throw e;
        }
        return answer;
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
