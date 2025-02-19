package com.itu.myspringframework.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    
    private Properties properties;
    private String configFile = "application.properties";


    public ConfigReader() throws Exception {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (input == null) {
                throw new IOException("Configuration file not found in classpath: application.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw e;
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
