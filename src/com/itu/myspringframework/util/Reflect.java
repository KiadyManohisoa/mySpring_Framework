package com.itu.myspringframework.util;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import com.itu.myspringframework.annotation.FieldParameter;
import com.itu.myspringframework.annotation.RequestParameter;
import com.itu.myspringframework.exception.ValidationException;
import com.itu.myspringframework.mapping.MySession;
import com.itu.myspringframework.servlets.RunnableWrapper;
import com.itu.myspringframework.upload.MultiPartHandler;
import com.itu.myspringframework.validation.MessageValue;
import com.itu.myspringframework.validation.ValueController;

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
            RunnableWrapper rnw) throws Exception {
        Convertor convertor = new Convertor();
        for (int i = 0; i < mParameters.length; i++) {
            if (invokeParams[i] == null) {
                if (mParameters[i].getType().equals(MySession.class)) {
                    // System.out.println("nisy mysession en paramètre");
                    Class<?> clazz = Class.forName(mParameters[i].getType().getName());
                    MySession mySession = (MySession) clazz.getDeclaredConstructor().newInstance();
                    mySession.setKeyValues(request.getSession());
                    rnw.setCallback(() -> {
                        mySession.updateHttpSession(request.getSession());
                    });
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

    public void assignForParts(Parameter[] mParameters, HttpServletRequest request, Object[] invokeParams, HashMap<String, Integer> paramsAssignationMap, ValueController vC) throws Exception {
        if(!request.getParts().isEmpty()) {
            // System.out.println("longueur de parts est" +request.getParts().size());
            for(Part part : request.getParts()) {
                // System.out.println("les noms sont "+part.getName());
                String inputName = part.getName();
                if (part.getSubmittedFileName() != null && !part.getSubmittedFileName().isEmpty()) {
                    if(inputName.contains(".")) {
                        String[] objSpecs = inputName.split("\\.");
                        int idParameter = this.findIdParameter(mParameters, objSpecs[0]);
                        if (idParameter != -1) {
                            // if (this.canBeInstanced(objSpecs[0], mParameters)) {
                                if (paramsAssignationMap.containsKey(objSpecs[0])) {
                                    int idToAssign = paramsAssignationMap.get(objSpecs[0]);
                                    Object toAssign = invokeParams[idToAssign];
                                    this.assignValueToObject(toAssign, objSpecs[1], inputName,
                                            "", vC, request);
                                } else {
                                    Object toAssign = this.instanceObjectFromParm(objSpecs[0], mParameters[idParameter]);
                                    this.assignValueToObject(toAssign, objSpecs[1], inputName,
                                            "", vC, request);
                                    invokeParams[idParameter] = toAssign;
                                    paramsAssignationMap.put(objSpecs[0], idParameter);
                                }
                            // }
                        }
                    }
                }
            }
        }
    }  

    public Object[] prepareInvokeParams(Map<String, String[]> parameterMap, Parameter[] mParameters,
                                        Object invokingObj, HttpServletRequest request, RunnableWrapper rnw) throws Exception {
        Object[] answers = new Object[2];
        Runnable callback = () -> {};
        Object[] invokeParams = new Object[mParameters.length];
        HashMap<String, Integer> paramsAssignationMap = new HashMap<>();
        Convertor convertor = new Convertor();
        ValueController vC = new ValueController();

        this.checkParameters(mParameters, new Class<?>[] { MySession.class });

        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String inputName = parameterNames.nextElement();
            String[] inputValues = request.getParameterValues(inputName); // Récupérer toutes les valeurs pour ce nom
            // System.out.println("for inputName "+inputName);
            // System.out.print("Values are :");
            // for (int i = 0; i < inputValues.length; i++) {
                // System.out.println("value "+i+" "+inputValues[i]);
            // }

            // Traiter chaque valeur individuellement
            for (String inputValue : inputValues) {
                if (!inputValue.isEmpty()) {
                    processInput(inputName, inputValue, mParameters, invokeParams, paramsAssignationMap, vC, request);
                }
            }
        }
        // for(int i=0;i<invokeParams.length;i++) {
        //     if(invokeParams[i]==null) {
                // System.out.println(i+" is still null");
        //     }
        // }
        if(request.getContentType()!=null && request.getContentType().toLowerCase().startsWith("multipart/"))
            this.assignForParts(mParameters, request, invokeParams, paramsAssignationMap, vC);

        if (vC.isHasError()) {
            throw new ValidationException(vC);
        } 
        // // Ensure all parameters are assigned a value
        this.checkLeftParams(mParameters, invokeParams, request, rnw);

        answers[0] = callback;
        answers[1] = invokeParams;

        return answers;
    }

    private void processInput(String inputName, String inputValue, Parameter[] mParameters, Object[] invokeParams,
            HashMap<String, Integer> paramsAssignationMap, ValueController vC, HttpServletRequest request) throws Exception {
        
        // System.out.println("[DEBUG] processInput: inputName=" + inputName + ", inputValue=" + inputValue);    
        if (Util.isArrayObjectInput(inputName)) {
            // System.out.println("[DEBUG] Processing as array object input");
            processArrayObjectInput(inputName, inputValue, mParameters, invokeParams, paramsAssignationMap, vC, request);
        } else if (inputName.contains(".")) {
            // System.out.println("[DEBUG] Processing as single object input");
            processSingleObjectInput(inputName, inputValue, mParameters, invokeParams, paramsAssignationMap, vC, request);
        } else {
            // System.out.println("[DEBUG] Processing as standard input");
            processStandardInput(inputName, inputValue, mParameters, invokeParams, paramsAssignationMap, request);
        }
    }

    Set<String> extractExpectedAttributes(Map<String, String[]> parameterMap, String paramName) {
        Set<String> expectedAttributes = new HashSet<>();
        for (String inputName : parameterMap.keySet()) {
            if (inputName.startsWith("[]" + paramName + ".")) {
                String[] objSpecs = inputName.split("\\.");
                if (objSpecs.length > 1) {
                    expectedAttributes.add(objSpecs[1]);
                }
            }
        }
        return expectedAttributes;
    }   

    private void processArrayObjectInput(String inputName, String inputValue, Parameter[] mParameters, Object[] invokeParams,
        HashMap<String, Integer> paramsAssignationMap, ValueController vC, HttpServletRequest request) throws Exception {
    
    // System.out.println("[DEBUG] processArrayObjectInput: inputName=" + inputName + ", inputValue=" + inputValue);
    int index = extractIndexFromInputName(inputName);
    String paramName = extractParamNameFromInputName(inputName);

    // System.out.println("[DEBUG] Extracted index=" + index + ", paramName=" + paramName);

    int idParameter = findIdParameter(mParameters, paramName);
    // System.out.println("[DEBUG] idParameter=" + idParameter);

    if (idParameter != -1) {
        // System.out.println("[DEBUG] Parameter found, processing...");
        
        List<Object> objectList = getOrCreateObjectList(paramName, invokeParams, paramsAssignationMap, idParameter);
        // System.out.println("[DEBUG] objectList size=" + objectList.size());
        
        Object targetObject = getOrCreateObjectAtIndex(objectList, index, paramName, mParameters[idParameter]);
        // System.out.println("[DEBUG] targetObject=" + targetObject);
        
        String attributeName = extractAttributeNameFromInputName(inputName);
        // System.out.println("[DEBUG] attributeName=" + attributeName);
        
        assignValueToObject(targetObject, attributeName, inputName, inputValue, vC, request);
        // System.out.println("[DEBUG] Value assigned successfully");
    } else {
        // System.out.println("[DEBUG] Parameter not found: " + paramName);
        throw new IllegalArgumentException("Parameter not found: " + paramName);
    }
    }

    private String extractAttributeNameFromInputName(String inputName) {
        int startIndex = inputName.lastIndexOf('.') + 1;
        String attributeName = inputName.substring(startIndex);
        // System.out.println("[DEBUG] extractAttributeNameFromInputName: inputName=" + inputName + ", attributeName=" + attributeName);
        return attributeName;
    }
    

    private int extractIndexFromInputName(String inputName) {
        int startIndex = inputName.indexOf('[') + 1;
        int endIndex = inputName.indexOf(']');
        int index = Integer.parseInt(inputName.substring(startIndex, endIndex));
        // System.out.println("[DEBUG] extractIndexFromInputName: inputName=" + inputName + ", index=" + index);
        return index;
    }
    
    private String extractParamNameFromInputName(String inputName) {
        int startIndex = inputName.indexOf(']') + 1;
        int endIndex = inputName.indexOf('.');
        String paramName = inputName.substring(startIndex, endIndex);
        // System.out.println("[DEBUG] extractParamNameFromInputName: inputName=" + inputName + ", paramName=" + paramName);
        return paramName;
    }
    

    private void processSingleObjectInput(String inputName, String inputValue, Parameter[] mParameters, Object[] invokeParams,
                                        HashMap<String, Integer> paramsAssignationMap, ValueController vC, HttpServletRequest request) throws Exception {
        String[] objSpecs = inputName.split("\\.");
        int idParameter = findIdParameter(mParameters, objSpecs[0]);

        if (idParameter != -1) {
            Object toAssign = getOrCreateObject(objSpecs[0], invokeParams, paramsAssignationMap, mParameters[idParameter], idParameter);
            if(toAssign==null) {
                // System.out.println("problem with the input name ="+inputName);
            }
            assignValueToObject(toAssign, objSpecs[1], inputName, inputValue, vC, request);
        }
    }

    private void processStandardInput(String inputName, String inputValue, Parameter[] mParameters, Object[] invokeParams,
                                    HashMap<String, Integer> paramsAssignationMap, HttpServletRequest request) throws Exception {
        int idParameter = findIdParameter(mParameters, inputName);
        if (idParameter != -1 && !paramsAssignationMap.containsKey(inputName)) {
            invokeParams[idParameter] = getParameterValue(inputName, inputValue, mParameters[idParameter], new Convertor(), request);
            paramsAssignationMap.put(inputName, idParameter);
        }
    }

    private List<Object> getOrCreateObjectList(String paramName, Object[] invokeParams, HashMap<String, Integer> paramsAssignationMap, int idParameter) throws Exception {
        // System.out.println("[DEBUG] getOrCreateObjectList: paramName=" + paramName + ", idParameter=" + idParameter);
    
        if (paramsAssignationMap.containsKey(paramName)) {
            // System.out.println("[DEBUG] List exists in invokeParams: " + invokeParams[idParameter]);
            return (List<Object>) invokeParams[idParameter];
        } else {
            // System.out.println("[DEBUG] Creating new list for paramName=" + paramName);
            List<Object> objectList = new ArrayList<>();
            invokeParams[idParameter] = objectList;
            paramsAssignationMap.put(paramName, idParameter);
            return objectList;
        }
    }

    private Object getOrCreateObjectAtIndex(List<Object> objectList, int index, String paramName, Parameter parameter) throws Exception {
        // System.out.println("[DEBUG] getOrCreateObjectAtIndex: objectList size=" + objectList.size() + ", index=" + index);
    
        while (objectList.size() <= index) {
            // System.out.println("[DEBUG] Creating new object for index=" + index);
            objectList.add(instanceObjectFromParm(paramName, parameter));
        }
    
        Object targetObject = objectList.get(index);
        // System.out.println("[DEBUG] Retrieved targetObject=" + targetObject);
        return targetObject;
    }

    private Object getOrCreateObject(String paramName, Object[] invokeParams, HashMap<String, Integer> paramsAssignationMap, Parameter parameter, int idParameter) throws Exception {
        // System.out.println("[DEBUG] getOrCreateObject: paramName=" + paramName + ", idParameter=" + idParameter);

        if (paramsAssignationMap.containsKey(paramName)) {
            // System.out.println("[DEBUG] Object exists in invokeParams: " + invokeParams[paramsAssignationMap.get(paramName)]);
            return invokeParams[paramsAssignationMap.get(paramName)];
        } else {
            // System.out.println("[DEBUG] Creating new object for paramName=" + paramName);
            Object newObject = instanceObjectFromParm(paramName, parameter);
            invokeParams[idParameter] = newObject;
            paramsAssignationMap.put(paramName, idParameter);
            return newObject;
        }
    }

    void checkParameters(Parameter[] parameters, Class<?>[] notToCheck) throws Exception {
        String error = new String();
        for (int i = 0; i < parameters.length; i++) {
            if (Util.isClassNotPresent(notToCheck, parameters[i].getType())) {
                if (!parameters[i].isAnnotationPresent(RequestParameter.class)) {
                    if (i != 0) {
                        error += ", ";
                    }
                    error += "the parameter " + parameters[i].getName();
                }
            }
        }
        if (!error.isEmpty()) {
            throw new Exception("Please annotate " + error);
        }
    }

    Object getParameterValue(String inputName, String inputValue, Parameter parameter, Convertor convertor, HttpServletRequest request)
            throws Exception {
        RequestParameter annotation = parameter.getAnnotation(RequestParameter.class);

        if ((annotation != null && inputName.equals(annotation.value())) || (inputName.equals(parameter.getName()))) {
            return convertor.convertInputToParam(inputValue, inputName, parameter.getType(), inputName, request);
            // } else if (inputName.equals(parameter.getName())) {
            // return convertor.convertInputToParam(inputValue, parameter.getType());
            // }
        } else {
            return convertor.getDefaultValue(parameter.getType());
        }
    }

    public void assignValueToObject(Object reference, String attrSearch, String inputName, String inputValue,
            ValueController vC, HttpServletRequest request)
            throws Exception {
        try {
            Control control = new Control();
            Field[] fields = this.getAllFields(reference.getClass());
            for (int i = 0; i < fields.length; i++) {

                if (fields[i].getName().equals(attrSearch) || (fields[i].isAnnotationPresent(FieldParameter.class)
                        && fields[i].getAnnotation(FieldParameter.class).value().equals(attrSearch))) {

                    if (control.isFieldInvalid(fields[i], inputValue, inputName, vC)) {
                        System.out.println("yes" + attrSearch + " was invalid");
                        break;
                    } else {
                        vC.add(inputName, new MessageValue(inputValue));
                    }
                    Method mSetter = reference.getClass().getMethod(
                            "set" + Syntaxe.getSetterGetterNorm(fields[i].getName()),
                            new Class[] { fields[i].getType() });

                    mSetter.invoke(reference, new Convertor().convertInputToParam(inputValue, inputName, fields[i].getType(), attrSearch, request));
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

    public Field[] getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        collectFields(clazz, fields);
        return fields.toArray(new Field[0]);
    }

    private void collectFields(Class<?> clazz, List<Field> fields) {
        if (clazz == null || clazz == Object.class) return; 

        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            fields.add(field);
        }

        collectFields(clazz.getSuperclass(), fields);
    }

    public Object instanceObjectFromParm(String varName, Parameter param) throws Exception {
        Object obj = new Object();
        RequestParameter annotation = param.getAnnotation(RequestParameter.class);
        if (annotation != null && varName.equals(annotation.value())) {
            Type type = param.getParameterizedType();
            if(type instanceof ParameterizedType) {
                ParameterizedType parameterizedType =(ParameterizedType) type;
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class<?>) {
                    Class<?> elementType = (Class<?>) actualTypeArguments[0]; // Type des éléments de la liste
                    Object elementInstance = elementType.getDeclaredConstructor().newInstance(); // Instanciation de l'élément
                    obj = elementInstance;
                }
            }
            else {
                Class<?> clazz = Class.forName(param.getType().getName());
                obj = clazz.getDeclaredConstructor().newInstance();
            }
        }
        return obj;
    }

}
