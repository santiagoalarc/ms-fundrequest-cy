package co.com.crediya.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class EnvLoader {

    private static final String DEFAULT_ENV_FILE = ".env";

    public static void loadConfig() {

        String envMode = System.getenv("ENV_MODE");
        if (envMode == null) {
            envMode = "default";
        }

        String envFile;
        switch (envMode.toLowerCase()) {
            case "production":
                envFile = ".env.production";
                break;
            case "develop":
                envFile = ".env.develop";
                break;
            default:
                envFile = DEFAULT_ENV_FILE;
        }

        System.out.println("Loading configuration file: " + envFile);

        EnvLoader.loadEnv(envFile);
    }
    public static void loadEnv(String filePath) {
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            lines.forEach(line -> {
                String trimmedLine = line.trim();
                if (!trimmedLine.startsWith("#") && trimmedLine.contains("=")) {
                    String[] parts = trimmedLine.split("=", 2);
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    System.setProperty(key, value);
                }
            });
        } catch (IOException e) {
            System.err.println("Could not load .env file: " + e.getMessage());
        }
    }
}