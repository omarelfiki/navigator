package map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;

import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;

import static util.DebugUtil.*;

public class TileUtil {
    private final boolean isOnline;

    public TileUtil(boolean isOnline) {
        this.isOnline = isOnline;

    }

    public TileFactory getTileFactory() {
        TileFactoryInfo info;
        if (isOnline) {
            info = new OSMTileFactoryInfo();
        } else {
            try {
                String encodedPath = Paths.get(System.getProperty("user.home"), "Archive_color.zip").toUri().toString();
                info = new OSMTileFactoryInfo("Zip archive", "jar:" + encodedPath + "!");
            } catch (Exception e) {
                sendWarning("Error constructing file path for TileFactory. Please ensure the 'Archive_color.zip' file exists in your home directory.");
                throw new RuntimeException();
            }
        }
        return new DefaultTileFactory(info);
    }


    public void createZip(String sourceDirPath, String zipFilePath) throws IOException {
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            sendError("Source directory does not exist or is not a directory: " + sourceDirPath);
            throw new IOException();
        }
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipDirectory(sourceDir, "", zos);
        }
    }


    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            sendError("Invalid folder: " + (folder != null ? folder.getAbsolutePath() : "null"));
            throw new IOException();
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            String path = parentFolder.isEmpty() ? file.getName() : parentFolder + "/" + file.getName();
            if (file.isDirectory()) {
                zipDirectory(file, path, zos);
                continue;
            }
            try (FileInputStream fis = new FileInputStream(file)) {
                zos.putNextEntry(new ZipEntry(path));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }
        }
    }

}