package util;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.plaf.multi.MultiDesktopIconUI;

import jakarta.servlet.http.HttpServletRequest;

import annotation.FieldParameter;
import annotation.RequestParameter;
import exception.ValidationException;
import mapping.MySession;
import upload.MultiPartHandler;
import validation.MessageValue;
import validation.ValueController;

public class Reflect {

    public void controlValue(ArrayList<String> inputNameAssociedToObject, Parameter[] parameters, Object[] invokeParams)
            throws Exception {
        Control control = new Control();
        if (inputNameAssociedToObject.size() > 0) {
            for (String inputName : inputNameAssociedToObject) {
                int idParameter = this.findIdParameter(parameters, inputName);
                if (idParameter != -1) {
                    Object binded = (Object) invokeParams[idParameter];
                    control.checkOnFields(binded);
                }
            }
        }
    }

    public void checkLeftParams(Parameter[] mParameters, Object[] invokeParams, HttpServletRequest request,
            Runnable callback) throws Exception {
        Convertor convertor = new Convertor();
        for (int i = 0; i < mParameters.length; i++) {
            if (invokeParams[i] == null) {
                if (mParameters[i].getType().equals(MySession.class)) {
                    Class<?> clazz = Class.forName(mParameters[i].getType().getName());
                    MySession mySession = (MySession) clazz.getDeclaredConstructor().newInstance();
                    mySession.setKeyValues(request.getSession());
                    callback = () -> {
                        mySession.updateHttpSession(request.getSession());
                    };
                    invokeParams[i] = mySession;
                } else if (mParameters[i].getType().equals(MultiPartHandler.class)) {
                    MultiPartHandler handler = new MultiPartHandler();
                    handler.setAppPath(request.getServletContext().getRealPath(""));
                    String inputName = mParameters[i].getAnnotation(RequestParameter.class).value();
                    handler.setSelf(request.getPart(inputName));
                    invokeParams[i] = handler;
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
    }

    int findIdParameter(Parameter[] functionParams, String inputName) {
        int idParameter = -1;
        for (int i = 0; i < functionParams.length; i++) {
            RequestParameter annotation = functionParams[i].getAnnotation(RequestParameter.class);
            if (annotation != null && inputName.equals(annotation.value())) {
                return i;
            }
            // else if (inputName.equals(functionParams[i].getName())) {
            // return i;
            // }
        }
        return idParameter;
    }

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

        ArrayList<String> inputNameAssociatedToObject = new ArrayList<>();

        ValueController vC = new ValueController();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String inputName = entry.getKey();
            String[] inputValue = entry.getValue();
            // System.out.println("inputName " + inputName + " value " + inputValue[0]);
            // wanna set value to an object
            if (inputName.contains(".")) {
                String[] objSpecs = inputName.split("\\.");
                int idParameter = this.findIdParameter(mParameters, objSpecs[0]);
                if (idParameter != -1) {
                    if (this.canBeInstanced(objSpecs[0], mParameters)) {
                        if (paramsAssignationMap.containsKey(objSpecs[0])) {
                            int idToAssign = paramsAssignationMap.get(objSpecs[0]);
                            Object toAssign = invokeParams[idToAssign];
                            reflect.assignValueToObject(toAssign, objSpecs[1], inputName,
                                    inputValue[0], vC);
                        } else {
                            inputNameAssociatedToObject.add(objSpecs[0]);
                            Object toAssign = reflect.instanceObjectFromParm(objSpecs[0], mParameters);
                            reflect.assignValueToObject(toAssign, objSpecs[1], inputName,
                                    inputValue[0], vC);
                            invokeParams[idParameter] = toAssign;
                            paramsAssignationMap.put(objSpecs[0], idParameter);
                        }
                    }
                }
            }
            // wanna set value to a standard variable
            else {
                int idParameter = this.findIdParameter(mParameters, inputName);
                if (idParameter != -1) {
                    if (!paramsAssignationMap.containsKey(inputName)) {
                        invokeParams[idParameter] = this.getParameterValue(inputName, inputValue[0],
                                mParameters[idParameter],
                                convertor);
                        paramsAssignationMap.put(inputName, idParameter);
                    }
                }
            }

        }

        if (vC.isHasError()) {
            // System.out.println("misy blem");
            throw new ValidationException(vC);
        } else {
            // System.out.println("tsisy blem");
        }

        // Control the value of objets binded with an input
        // this.controlValue(inputNameAssociatedToObject, mParameters, invokeParams);

        // // Ensure all parameters are assigned a value
        this.checkLeftParams(mParameters, invokeParams, request, callback);

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

        if ((annotation != null && inputName.equals(annotation.value())) || (inputName.equals(parameter.getName()))) {
            return convertor.convertInputToParam(inputValue, parameter.getType());
            // } else if (inputName.equals(parameter.getName())) {
            // return convertor.convertInputToParam(inputValue, parameter.getType());
            // }
        } else {
            return convertor.getDefaultValue(parameter.getType());
        }
    }

    public void assignValueToObject(Object reference, String attrSearch, String inputName, String inputValue,
            ValueController vC)
            throws Exception {
        try {
            Control control = new Control();
            Field[] fields = reference.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {

                if (fields[i].getName().equals(attrSearch) || (fields[i].isAnnotationPresent(FieldParameter.class)
                        && fields[i].getAnnotation(FieldParameter.class).value().equals(attrSearch))) {

                    if (control.isFieldInvalid(fields[i], inputValue, inputName, vC)) {
                        // System.out.println("yes" + attrSearch + " was invalid");
                        break;
                    } else {
                        vC.add(inputName, new MessageValue(inputValue));
                    }
                    Method mSetter = reference.getClass().getDeclaredMethod(
                            "set" + Syntaxe.getSetterGetterNorm(fields[i].getName()),
                            new Class[] { fields[i].getType() });

                    // System.out.println("field" + i + " " + mSetter.getName());
                    mSetter.invoke(reference, new Convertor().convertInputToParam(inputValue, fields[i].getType()));
                    return;
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
