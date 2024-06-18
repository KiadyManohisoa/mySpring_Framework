package mapping;

import java.lang.reflect.*;

public class MyMapping {

    String className;
    Method method;

    @SuppressWarnings("deprecation")
    public Object invokeMethode() throws Exception {
        Object answer = null;
        try {
            Class<?> clazz = Class.forName(this.getClassName());
            Method mConcerned = clazz.getDeclaredMethod(this.getMethod().getName());
            answer = mConcerned.invoke(clazz.newInstance(), new Object[] {});
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
