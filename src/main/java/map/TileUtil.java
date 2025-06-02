package map;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;

import org.jxmapviewer.viewer.TileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.imageio.ImageIO;

import static util.DebugUtli.getDebugMode;

public class TileUtil {
    boolean isOnline;

    boolean isDebugMode;

    List<HeatPoint> heatPoints;

    int[] zoomLevels;

    public TileUtil(boolean isOnline) {
        this.isOnline = isOnline;
        this.isDebugMode = getDebugMode();
    }

    public TileUtil(List<HeatPoint> heatPoints, int[] zoomLevels) {
        this.isDebugMode = getDebugMode();
        this.heatPoints = heatPoints;
        if (zoomLevels == null) {
            this.zoomLevels = new int[20];
            for (int i = 0; i < 20; i++) this.zoomLevels[i] = i;
        } else {
            this.zoomLevels = zoomLevels;
        }
    }

    public TileFactory getTileFactory(int type) {
        TileFactoryInfo info;
        switch (type) {
            case 0:
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
                break;
            case 1:
                try {
                    createHeatTileDirectory();
                    String encodedPath = Paths.get(System.getProperty("user.home"), "heatTiles.zip").toUri().toString();
                    info = new OSMTileFactoryInfo("Zip archive", "jar:" + encodedPath + "!");
                } catch (Exception e) {
                    throw new RuntimeException("Failed to construct file path for HeatTileFactory", e);
                }
                break;
            default:
                throw new RuntimeException("Unsupported tile type: " + type);
        }
        return new DefaultTileFactory(info);
    }

    public void createHeatTileDirectory() throws IOException {
        Map<String, Double> tileHeat = new HashMap<>();
        int blurRadius = 2; // tiles to blur in each direction
        double blurSigma = 1.0; // for Gaussian kernel

        for (HeatPoint heatPoint : heatPoints) {
            double lat = heatPoint.latitude();
            double lon = heatPoint.longitude();
            double value = heatPoint.time();
            for (int zoom : zoomLevels) {
                int n = 1 << zoom;
                int tileX = (int) Math.floor((lon + 180.0) / 360.0 * n);
                int tileY = (int) Math.floor((1.0 - Math.log(Math.tan(Math.toRadians(lat)) + 1.0 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2.0 * n);
                for (int dx = -blurRadius; dx <= blurRadius; dx++) {
                    for (int dy = -blurRadius; dy <= blurRadius; dy++) {
                        int nx = tileX + dx;
                        int ny = tileY + dy;
                        if (nx < 0 || ny < 0 || nx >= n || ny >= n) continue;
                        double dist2 = dx * dx + dy * dy;
                        double weight = Math.exp(-dist2 / (2 * blurSigma * blurSigma));
                        String key = zoom + "," + nx + "," + ny;
                        tileHeat.put(key, tileHeat.getOrDefault(key, 0.0) + value * weight);
                    }
                }
            }
        }

        File mainDir = new File(System.getProperty("user.home"), "heatTiles");
        if (!mainDir.exists() && !mainDir.mkdirs()) {
            if (isDebugMode) System.err.println("Failed to create main directory: " + mainDir.getAbsolutePath());
            return;
        }

        double maxHeat = tileHeat.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        double scale = 60.0 / maxHeat; // 60 is the top of your color gradient

        for (Map.Entry<String, Double> entry : tileHeat.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int zoom = Integer.parseInt(parts[0]);
            int tileX = Integer.parseInt(parts[1]);
            int tileY = Integer.parseInt(parts[2]);
            double heat = entry.getValue() * scale;
            BufferedImage colorTile = ColorUtil.getColorTile(heat);
            File zoomDir = new File(mainDir, String.valueOf(zoom));
            File xDir = new File(zoomDir, String.valueOf(tileX));
            if (!xDir.exists() && !xDir.mkdirs()) {
                if (isDebugMode) System.err.println("Failed to create directory: " + xDir.getAbsolutePath());
                continue;
            }
            File tileFile = new File(xDir, tileY + ".png");
            try {
                assert colorTile != null;
                ImageIO.write(colorTile, "png", tileFile);
            } catch (IOException e) {
                if (isDebugMode) System.err.println("Failed to save tile: " + tileFile.getAbsolutePath());
            }
        }
        createZip(mainDir.getAbsolutePath(), System.getProperty("user.home") + File.separator + "heatTiles.zip");
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