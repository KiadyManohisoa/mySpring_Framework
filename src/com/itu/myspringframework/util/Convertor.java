package com.itu.myspringframework.util;

import java.lang.reflect.*;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;

import com.itu.myspringframework.annotation.RequestParameter;
import com.itu.myspringframework.upload.MultiPartHandler;

import jakarta.servlet.http.HttpServletRequest;

public class Convertor {

    @SuppressWarnings("unchecked")
    public <T> T getDefaultValue(Class<T> clazz) {
        return (T) Array.get(Array.newInstance(clazz, 1), 0); // creates an array which will be specified with clazz
                                                              // type
    }

    public Object convertInputToParam(String inputValue, String inputName, Class<?> paramType, String fieldName, HttpServletRequest request) throws Exception {
        try {
            if (paramType.equals(int.class) || paramType.equals(Integer.class)) {
                return Integer.parseInt(inputValue);
            } else if (paramType.equals(double.class) || paramType.equals(Double.class)) {
                return Double.parseDouble(inputValue);
            } else if (paramType.equals(float.class) || paramType.equals(Float.class)) {
                return Float.parseFloat(inputValue);
            } else if (paramType.equals(String.class)) {
                return inputValue;
            } else if(paramType.equals(Date.class)) {
                return Date.valueOf(inputValue);
            } else if(paramType.equals(Timestamp.class)) {
                return Util.parseTimestamp(inputValue);
            } else if(paramType.equals(Time.class)) {
                return Util.parseTime(inputValue);
            } else if(paramType.equals(MultiPartHandler.class)) {
                MultiPartHandler handler = new MultiPartHandler();
                handler.setAppPath(request.getServletContext().getRealPath(""));
                handler.setSelf(request.getPart(inputName));
                return handler;
            }
            else {
                throw new IllegalArgumentException("Unsupported parameter type: " + paramType);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Casting error for value: " + inputValue + " and type: " + paramType, e);
        }        
    }

}
