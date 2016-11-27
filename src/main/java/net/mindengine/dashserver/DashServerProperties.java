package net.mindengine.dashserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Collectors;

public class DashServerProperties {


    public static final String DASHSERVER_PROPERTIES = "dashserver.properties";

    private Properties properties = null;

    public DashServerProperties() {
        File propertyFile = new File(DASHSERVER_PROPERTIES);
        properties = new Properties();

        if (propertyFile.exists()) {
            try {
                InputStream in = new FileInputStream(propertyFile);
                properties.load(in);
                in.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    public int getPort() {
        return Integer.parseInt(property("dashserver.port", "8080"));
    }

    private String property(String name, String defaultValue) {
        String envProperty = findEnvProperty(name);
        if (envProperty != null) {
            return envProperty;
        }

        return properties.getProperty(name, defaultValue);
    }

    private String findEnvProperty(String name) {
        return System.getenv(convertPropertyNameToEnvVariable(name));
    }

    private String convertPropertyNameToEnvVariable(String name) {
        StringBuilder envName = new StringBuilder();

        return name.chars().map(c -> {
            if ((c >= 48 && c <= 57)
                || (c >= 65 && c<= 90 )
                ) {
                return c;
            } else if (c >= 97 && c <= 122) {
                return c - 32;
            } else {
                return 95;
            }
        }).collect(StringBuilder::new,StringBuilder::appendCodePoint,StringBuilder::append).toString();
    }
}
