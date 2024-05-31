package mapping;

import java.lang.reflect.*;

public class MyMapping {

    String className;
    String methodName;

    @SuppressWarnings("deprecation")
    public Object invokeMethode() throws Exception {
        Object answer = null;
        try {
            Class<?> clazz = Class.forName(this.getClassName());
            Method mConcerned = clazz.getDeclaredMethod(this.getMethodName());
            answer = mConcerned.invoke(clazz.newInstance(), new Object[] {});
        } catch (Exception e) {
            throw e;
        }
        return answer;
    }

    public MyMapping(String className, String methodName) {
        this.setClassName(className);
        this.setMethodName(methodName);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

}
