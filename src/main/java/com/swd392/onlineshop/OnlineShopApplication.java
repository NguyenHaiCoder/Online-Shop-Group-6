package com.swd392.onlineshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@SpringBootApplication
public class OnlineShopApplication {

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(OnlineShopApplication.class, args);
    }

    /**
     * Auto-loads .env file into System Properties if present,
     * allowing Spring Boot to resolve properties like Cloudinary credentials in local development.
     */
    private static void loadDotenv() {
        File[] candidateFiles = new File[]{
                new File(".env"),
                new File("../.env"),
                new File("Online-Shop-Group-6/.env")
        };
        for (File file : candidateFiles) {
            if (file.exists() && file.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                            continue;
                        }
                        int eqIndex = line.indexOf('=');
                        String key = line.substring(0, eqIndex).trim();
                        String value = line.substring(eqIndex + 1).trim();

                        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                                (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }

                        if (!key.isEmpty() && System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                } catch (IOException ignored) {
                }
                break;
            }
        }
    }

}
