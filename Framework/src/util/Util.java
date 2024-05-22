package util;

import java.io.*;
import java.util.*;
import java.net.*;

public class Util {

    public Util() {
    }

    @SuppressWarnings("unchecked")
    public List<Class<?>> getClassesByAnnotation(String basePackage, Class annotation) throws Exception {
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

    private void searchClassesInDirectory(List<Class<?>> classes, File directory, String basePackage,
            ClassLoader loader,
            Class annotation) throws Exception {
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
