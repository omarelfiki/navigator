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

import static util.DebugUtil.getDebugMode;

public class TileUtil {
    boolean isOnline;

    boolean isDebugMode;


    public TileUtil(boolean isOnline) {
        this.isOnline = isOnline;
        this.isDebugMode = getDebugMode();
    }

    public TileFactory getTileFactory() {
        TileFactoryInfo info;
        if (isOnline) {
            info = new OSMTileFactoryInfo();
        } else {
            try {
                String encodedPath = Paths.get(System.getProperty("user.home"), "Archive.zip").toUri().toString();
                info = new OSMTileFactoryInfo("Zip archive", "jar:" + encodedPath + "!");
            } catch (Exception e) {
                throw new RuntimeException("Failed to construct file path for TileFactory", e);
            }
        }
        return new DefaultTileFactory(info);
    }


    public void createZip(String sourceDirPath, String zipFilePath) throws IOException {
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new IOException("Source directory does not exist or is not a directory: " + sourceDirPath);
        }
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipDirectory(sourceDir, "", zos);
        }
    }


    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            throw new IOException("Invalid folder: " + (folder != null ? folder.getAbsolutePath() : "null"));
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