package com.codegrogu.library.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading application configuration from properties file.
 */
public class ConfigUtil {

    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    /**
     * Load properties from application.properties file
     */
    private static void loadProperties() {
        try (InputStream input = ConfigUtil.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("Warning: application.properties not found, using default values");
            }
        } catch (IOException e) {
            System.err.println("Warning: Error loading application.properties, using default values: " + e.getMessage());
        }
    }

    /**
     * Get integer property with default value
     */
    public static int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid integer value for " + key + ", using default: " + defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Get double property with default value
     */
    public static double getDoubleProperty(String key, double defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid double value for " + key + ", using default: " + defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * Get string property with default value
     */
    public static String getStringProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}