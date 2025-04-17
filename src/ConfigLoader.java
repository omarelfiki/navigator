import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    public static boolean checkIfConfigExists(String filePath) {
        try (FileInputStream input = new FileInputStream(filePath)) {
            Properties properties = new Properties();
            properties.load(input);
            return true;
        } catch (IOException e) {
            System.out.println("Configuration file not found");
            return false;
        }
    }

    public static void createConfig(String filePath) {
        Properties properties = new Properties();
        properties.setProperty("DB_USER", "");
        properties.setProperty("DB_PASSWORD", "");
        properties.setProperty("DB_HOST", "");
        properties.setProperty("DB_PORT", "");
        properties.setProperty("DB_NAME", "");
        properties.setProperty("GTFS_DIR", "");

        try (FileOutputStream output = new FileOutputStream(filePath)) {
            properties.store(output, "Configuration");
            System.out.println("Configuration created successfully.");
        } catch (IOException e) {
            System.out.println("Configuration creation failed: " + e);
        }
    }

    public static void loadConfig(String filePath) {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(filePath)) {
            properties.load(input);
            properties.forEach((key, value) -> System.setProperty(key.toString(), value.toString()));
            System.out.println("Configuration loaded successfully.");
        } catch (IOException e) {
            System.out.println("Configuration loading failed: " + e);

        }
    }

    public static void saveConfig(String filePath) {
        Properties properties = new Properties();
        try (FileOutputStream output = new FileOutputStream(filePath)) {
            properties.setProperty("DB_USER", System.getProperty("DB_USER"));
            properties.setProperty("DB_PASSWORD", System.getProperty("DB_PASSWORD"));
            properties.setProperty("DB_HOST", System.getProperty("DB_HOST"));
            properties.setProperty("DB_PORT", System.getProperty("DB_PORT"));
            properties.setProperty("DB_NAME", System.getProperty("DB_NAME"));
            properties.setProperty("GTFS_DIR", System.getProperty("GTFS_DIR"));
            properties.store(output, "Configuration");
            System.out.println("Configuration saved successfully.");
        } catch (IOException e) {
            System.out.println("Configuration saving failed: " + e);
        }
    }
}