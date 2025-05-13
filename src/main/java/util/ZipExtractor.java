package util;

import java.io.*;
import java.nio.file.*;
import java.util.Comparator;
import java.util.zip.*;

public class ZipExtractor {
    public static void extractZipToDirectory(String zipFilePath, String destinationDir) throws IOException {
        boolean isDebugMode = DebugUtli.getDebugMode();
        File zipFile = new File(zipFilePath);
        if (!zipFile.exists()) {
            throw new FileNotFoundException("Zip file not found: " + zipFilePath);
        }

        Path destDirPath = Paths.get(destinationDir);

        // Clear the directory if it exists
        if (Files.exists(destDirPath)) {
            Files.walk(destDirPath)
                    .sorted(Comparator.reverseOrder()) // Sort in reverse order to delete files before directories
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            if (isDebugMode) System.err.println("Error deleting file: " + path + " - " + e.getMessage());
                        }
                    });
        } else {
            Files.createDirectories(destDirPath);
        }

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                // Skip __MACOSX and ._ files
                if (entry.getName().startsWith("__MACOSX") || entry.getName().contains("/._")) {
                    continue;
                }
                Path entryPath = Paths.get(entry.getName());
                Path filePath = entryPath.getNameCount() > 1
                        ? destDirPath.resolve(entryPath.subpath(1, entryPath.getNameCount()))
                        : destDirPath.resolve(entryPath.toString());

                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    try (OutputStream err = Files.newOutputStream(filePath)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = zipIn.read(buffer)) != -1) {
                            err.write(buffer, 0, bytesRead);
                        }
                    }
                }
                zipIn.closeEntry();
            }
        }
    }
}