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
                if (mapping.containsKey(value)) {
                    throw new RuntimeException(
                            "L'url '" + value
                                    + "' est associée plus d'une fois à deux ou plusieurs méthodes, ce qui n'est pas permis");
                }
                mapping.put(value, new MyMapping(reference.getName(), methods[i].getName()));
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
                    throw new RuntimeException("Le dossier à scanner " + basePackage + " pour l'annotation "
                            + annotation.getName() + " est vide");
                }
                search = true; // est un dossier et n'est pas vide
                searchClassesInDirectory(lsClasses, directory, basePackage, classLoader, annotation);
            }
        }

        if (!search) {
            throw new RuntimeException("Le dossier à scanner " + basePackage + " pour l'annotation "
                    + annotation.getName() + " n'existe pas");

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
