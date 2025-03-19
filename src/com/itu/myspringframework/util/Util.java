package com.itu.myspringframework.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.itu.myspringframework.mapping.MyMapping;
import com.itu.myspringframework.mapping.VerbMethod;

public class Util {

    public Util() {
    }

    public static boolean isArrayObjectInput(String inputName) {
        String arrayObjectPattern = "^\\[\\d+\\]\\w+\\.\\w+$";
        if (inputName.matches(arrayObjectPattern)) {
            return true;
        }
        return false;
    }

    public static Time parseTime(String time) throws Exception {
        Time theTime = null;
        try {
            if (!time.matches("\\d{2}:\\d{2}:\\d{2}")) {
                    time += ":00"; 
            }        
            theTime = Time.valueOf(time);
        } catch (Exception e) {
            throw new Exception("Format invalide pour l_heure : "+time);
        }
        return theTime;
    }

    public static Timestamp parseTimestamp(String date) throws Exception {
        Timestamp datyLera = null;
        try {
            String formattedDate = date.replace("T", " ") + ":00";
            datyLera = Timestamp.valueOf(formattedDate);
        } catch (Exception e) {
            throw new Exception("Format invalide pour la date-heure : "+date);
        }
        return datyLera;
    }

    public static String getPathWithoutRedirect(String url) {
        int index = url.indexOf("redirect:") + "redirect:".length();
        return url.substring(index); 
        //return url.replaceFirst(".*redirect:", "");   
    }

    public static boolean isRedirect(String url) {
        return url.contains("redirect:");
    }

    public static String getUrlPath(String referer, String baseURL) {
        return referer.substring(baseURL.length());
    }

    public boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    public static boolean isClassNotPresent(Class<?>[] classes, Class<?> targetClass) {
        Set<Class<?>> classSet = new HashSet<>();
        for (Class<?> clazz : classes) {
            classSet.add(clazz);
        }
        return !classSet.contains(targetClass);
    }

    String checkUrlValue(String url) {
        if (url.contains("?")) {
            return url.substring(0, url.indexOf("?"));
        }
        return url;
    }

    public <T extends Annotation> void addMethodByAnnotation(Class<?> reference, Class<T> annotation,
            HashMap<String, MyMapping> hashMap)
            throws Exception {
        Method[] methods = reference.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].isAnnotationPresent(annotation)) {
                T annotInstance = methods[i].getAnnotation(annotation);
                Method valueMethod = annotation.getMethod("value");
                String value = (String) valueMethod.invoke(annotInstance);
                value = this.checkUrlValue(value);
                if (hashMap.containsKey(value)) {
                    MyMapping myMapping = hashMap.get(value);

                    if (reference.getName().equals(myMapping.getClassName())) {
                        myMapping.addVerbMethod(new VerbMethod(methods[i]));
                    } else {
                        throw new RuntimeException(
                                "Two methods with the same URL can only be defined within the same class");
                    }
                    // throw new RuntimeException(
                    // "L'url '" + value
                    // + "' est associée plus d'une fois à deux ou plusieurs méthodes, ce qui n'est
                    // pas permis");
                } else {
                    hashMap.put(value, new MyMapping(reference.getName(), new VerbMethod(methods[i])));
                }
            }
        }
    }

    public <T extends Annotation> List<Class<?>> getClassesByAnnotation(String basePackage, Class<T> annotation)
            throws Exception {
        List<Class<?>> lsClasses = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> ressources = classLoader.getResources(basePackage.replace('.', '/'));
        boolean search = false; // initialement inexistant

        while (ressources.hasMoreElements()) {
            URL resource = ressources.nextElement();
            File directory = new File(URLDecoder.decode(resource.getFile(), "UTF-8"));
            if (directory.isDirectory()) {
                if (directory.listFiles().length == 0) {
                    throw new RuntimeException("The folder to scan " + basePackage + " for the annotation "
                    + annotation.getName() + " is empty");            
                }
                search = true; // est un dossier et n'est pas vide
                searchClassesInDirectory(lsClasses, directory, basePackage, classLoader, annotation);
            }
        }

        if (!search) {
            throw new RuntimeException("The folder to scan " + basePackage + " for the annotation "
            + annotation.getName() + " does not exist");    

        }

        return lsClasses;
    }

    <T extends Annotation> void searchClassesInDirectory(List<Class<?>> classes, File directory,
            String basePackage,
            ClassLoader loader,
            Class<T> annotation) throws Exception {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String newPackage = basePackage + "." + file.getName();
                    searchClassesInDirectory(classes, file, newPackage, loader, annotation);
                } else if (file.getName().endsWith(".class")) {
                    try {
                        String className = basePackage + "." + file.getName().replace(".class", "");
                        Class<?> clazz = loader.loadClass(className);
                        if (clazz.isAnnotationPresent(annotation)) {
                            classes.add(clazz);
                        }
                    } catch (NoClassDefFoundError | ClassNotFoundException e) {
                        throw e;
                    }
                }
            }
        }
    }

}
