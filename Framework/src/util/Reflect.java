package util;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

import annotation.FieldParameter;
import annotation.RequestParameter;
import mapping.MySession;

public class Reflect {

    public Object[] prepareInvokeParams(Map<String, String[]> parameterMap, Parameter[] mParameters,
            Object invokingObj,
            HttpServletRequest request)
            throws Exception {
        Object[] answers = new Object[2];
        Runnable callback = () -> {
        };
        Object[] invokeParams = new Object[mParameters.length];
        HashMap<String, Integer> paramsAssignationMap = new HashMap<>();
        Convertor convertor = new Convertor();
        Reflect reflect = new Reflect();

        this.checkParameters(mParameters, new Class<?>[] { MySession.class });

        int j = 0;
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String inputName = entry.getKey();
            String[] inputValue = entry.getValue();
            // wanna set value to an object
            if (inputName.contains(".")) {
                String[] objSpecs = inputName.split("\\.");
                if (this.canBeInstanced(objSpecs[0], mParameters)) {
                    if (paramsAssignationMap.containsKey(objSpecs[0])) {
                        int idToAssign = paramsAssignationMap.get(objSpecs[0]);
                        Object toAssign = invokeParams[idToAssign];
                        reflect.assignValueToObject(toAssign, objSpecs[1],
                                inputValue[0]);
                    } else {
                        Object toAssign = reflect.instanceObjectFromParm(objSpecs[0], mParameters);
                        reflect.assignValueToObject(toAssign, objSpecs[1],
                                inputValue[0]);
                        invokeParams[j] = toAssign;
                        paramsAssignationMap.put(objSpecs[0], j);
                        j++;
                    }
                }
            }
            // wanna set value to a standard variable
            else {
                if (!paramsAssignationMap.containsKey(inputName)) {
                    invokeParams[j] = this.getParameterValue(inputName, inputValue[0], mParameters[j],
                            convertor);
                    paramsAssignationMap.put(inputName, j);
                    j++;
                }
            }
        }

        // // Ensure all parameters are assigned a value
        for (int i = 0; i < mParameters.length; i++) {
            if (invokeParams[i] == null) {
                if (mParameters[i].getType().equals(MySession.class)) {
                    Class<?> clazz = Class.forName(mParameters[i].getType().getName());
                    MySession mySession = (MySession) clazz.getDeclaredConstructor().newInstance();
                    mySession.setKeyValues(request.getSession());
                    callback = () -> {
                        mySession.updateHttpSession(request.getSession());
                    };
                    // mySession.setSession(request.getSession());
                    invokeParams[i] = mySession;
                } else {
                    if (mParameters[i].getType().isPrimitive()) {
                        invokeParams[i] = convertor.getDefaultValue(mParameters[i].getType());
                    } else {
                        Class<?> clazz = Class.forName(mParameters[i].getType().getName());
                        invokeParams[i] = clazz.getDeclaredConstructor().newInstance();
                    }
                }
            }
        }
        answers[0] = callback;
        answers[1] = invokeParams;

        return answers;
    }

    void checkParameters(Parameter[] parameters, Class<?>[] notToCheck) throws Exception {
        String error = new String();
        for (int i = 0; i < parameters.length; i++) {
            if (Util.isClassNotPresent(notToCheck, parameters[i].getType())) {
                if (!parameters[i].isAnnotationPresent(RequestParameter.class)) {
                    if (i != 0) {
                        error += ", ";
                    }
                    error += "le paramètre " + parameters[i].getName();
                }
            }
        }
        if (!error.isEmpty()) {
            throw new Exception("Veuillez annoter " + error);
        }
    }

    Object getParameterValue(String inputName, String inputValue, Parameter parameter, Convertor convertor)
            throws Exception {
        RequestParameter annotation = parameter.getAnnotation(RequestParameter.class);

        if (annotation != null && inputName.equals(annotation.value())) {
            return convertor.convertInputToParam(inputValue, parameter.getType());
        } else if (inputName.equals(parameter.getName())) {
            return convertor.convertInputToParam(inputValue, parameter.getType());
        } else {
            return convertor.getDefaultValue(parameter.getType());
        }
    }

    public void assignValueToObject(Object reference, String attrSearch, String inputValue)
            throws Exception {
        try {
            Field[] fields = reference.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].getName().equals(attrSearch) || (fields[i].isAnnotationPresent(FieldParameter.class)
                        && fields[i].getAnnotation(FieldParameter.class).value().equals(attrSearch))) {
                    Method mSetter = reference.getClass().getDeclaredMethod(
                            "set" + Syntaxe.getSetterNorm(fields[i].getName()),
                            new Class[] { fields[i].getType() });

                    // System.out.println("field" + i + " " + mSetter.getName());
                    mSetter.invoke(reference, new Convertor().convertInputToParam(inputValue, fields[i].getType()));
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    boolean canBeInstanced(String varName, Parameter[] parameters) {
        boolean ans = false;
        for (int i = 0; i < parameters.length; i++) {
            RequestParameter annotation = parameters[i].getAnnotation(RequestParameter.class);
            if (annotation != null && varName.equals(annotation.value())) {
                ans = true;
            } else if (varName.equals(parameters[i].getName())) {
                ans = true;
            }
        }
        return ans;
    }

    public Object instanceObjectFromParm(String varName, Parameter[] params) throws Exception {
        Object obj = new Object();
        for (int i = 0; i < params.length; i++) {
            RequestParameter annotation = params[i].getAnnotation(RequestParameter.class);
            if (annotation != null && varName.equals(annotation.value())) {
                Class<?> clazz = Class.forName(params[i].getType().getName());
                obj = clazz.getDeclaredConstructor().newInstance();
                return obj;
            } else if (varName.equals(params[i].getName())) {
                Class<?> clazz = Class.forName(params[i].getType().getName());
                obj = clazz.getDeclaredConstructor().newInstance();
                return obj;
            } else {
                Class<?> clazz = Class.forName(params[i].getType().getName());
                obj = clazz.getDeclaredConstructor().newInstance();
                return obj;
            }
        }
        return obj;
    }

}
