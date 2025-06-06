package com.itu.myspringframework.util;

import java.lang.reflect.*;

public class Convertor {

    @SuppressWarnings("unchecked")
    public <T> T getDefaultValue(Class<T> clazz) {
        return (T) Array.get(Array.newInstance(clazz, 1), 0); // creates an array which will be specified with clazz
                                                              // type
    }

    public Object convertInputToParam(String inputValue, Class<?> paramType) throws Exception {
        try {
            if (paramType.equals(int.class) || paramType.equals(Integer.class)) {
                return Integer.parseInt(inputValue);
            } else if (paramType.equals(double.class) || paramType.equals(Double.class)) {
                return Double.parseDouble(inputValue);
            } else if (paramType.equals(float.class) || paramType.equals(Float.class)) {
                return Float.parseFloat(inputValue);
            } else if (paramType.equals(String.class)) {
                return inputValue;
            } else {
                throw new IllegalArgumentException("Unsupported parameter type: " + paramType);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Casting error for value: " + inputValue + " and type: " + paramType, e);
        }        
    }

}
