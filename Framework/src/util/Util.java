package util;

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;
import java.lang.annotation.Annotation;
import mapping.MyMapping;

public class Util {

    public Util() {
    }

    public <T extends Annotation> void addMethodByAnnotation(Class<?> reference, Class<T> annotation,
            HashMap<String, MyMapping> mapping)
            throws Exception {
        Method[] methods = reference.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].isAnnotationPresent(annotation)) {
                T annotInstance = methods[i].getAnnotation(annotation);
                Method valueMethod = annotation.getMethod("value");
                String value = (String) valueMethod.invoke(annotInstance);
                mapping.put(value, new MyMapping(reference.getName(), methods[i].getName()));
            }
        }
    }

    public <T extends Annotation> List<Class<?>> getClassesByAnnotation(String basePackage, Class<T> annotation)
            throws Exception {
        List<Class<?>> lsClasses = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(basePackage.replace('.', '/'));

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File directory = new File(URLDecoder.decode(resource.getFile(), "UTF-8"));
            if (directory.isDirectory()) {
                searchClassesInDirectory(lsClasses, directory, basePackage, classLoader, annotation);
            }
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
