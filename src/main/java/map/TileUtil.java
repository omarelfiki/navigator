package map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;

import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;

import static util.DebugUtil.getDebugMode;

public class TileUtil {
    private final boolean isOnline;

    private final boolean isDebugMode;

    private final boolean useGrayscaleCache;


    public TileUtil(boolean isOnline, boolean grayscale) {
        this.isOnline = isOnline;
        this.isDebugMode = getDebugMode();
        this.useGrayscaleCache = grayscale;
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
                if (isDebugMode) {
                    System.err.println("WARNING: Error constructing file path for TileFactory. Please ensure the 'Archive_color.zip' file exists in your home directory.");
                }
                throw new RuntimeException();
            }
        }
        if (useGrayscaleCache) {
            String grayscalePath = Paths.get(System.getProperty("user.home"), "Archive_grayscale.zip").toUri().toString();
            info =  new OSMTileFactoryInfo("Zip archive (grayscale)", "jar:" + grayscalePath + "!");
        }
        return new DefaultTileFactory(info);
    }

    public void generateGrayscaleCache(String colorCacheDirPath, String grayscaleCacheDirPath) throws IOException {
        File colorDir = new File(colorCacheDirPath);
        File grayDir = new File(grayscaleCacheDirPath);
        if (!colorDir.exists() || !colorDir.isDirectory()) {
            if (isDebugMode) {
                System.err.println("WARNING: Color cache directory does not exist or is not a directory: " + colorCacheDirPath);
            }
            throw new IOException();
        }
        if (!grayDir.exists() && !grayDir.mkdirs()) {
            if (isDebugMode) {
                System.err.println("WARNING: Failed to create grayscale cache directory: " + grayscaleCacheDirPath);
            }
            throw new IOException();
        }
        for (File file : Objects.requireNonNull(colorDir.listFiles())) {
            if (file.isDirectory()) {
                generateGrayscaleCache(file.getAbsolutePath(), new File(grayDir, file.getName()).getAbsolutePath());
            } else if (file.getName().endsWith(".png") || file.getName().endsWith(".jpg")) {
                File grayFile = new File(grayDir, file.getName());
                if (!grayFile.exists()) {
                    java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(file);
                    if (img != null) {
                        java.awt.image.BufferedImage gray = new java.awt.image.BufferedImage(img.getWidth(), img.getHeight(), java.awt.image.BufferedImage.TYPE_BYTE_GRAY);
                        java.awt.image.ColorConvertOp op = new java.awt.image.ColorConvertOp(java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_GRAY), null);
                        op.filter(img, gray);
                        javax.imageio.ImageIO.write(gray, "png", grayFile);
                    }
                }
            }
        }
    }


    public void createZip(String sourceDirPath, String zipFilePath) throws IOException {
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            if (isDebugMode) {
                System.err.println("WARNING (ZIP-creation): Source directory does not exist or is not a directory: " + sourceDirPath);
            }
        }
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipDirectory(sourceDir, "", zos);
        }
    }


    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            if (isDebugMode) {
                System.err.println("WARNING (ZIP-creation): Invalid folder: " + (folder != null ? folder.getAbsolutePath() : "null"));
            }
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