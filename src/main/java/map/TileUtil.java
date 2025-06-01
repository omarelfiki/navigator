package map;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
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
        this.zoomLevels = zoomLevels;
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
        MapIntegration mapIntegration = MapProvider.getInstance();
        JXMapViewer map = mapIntegration.getMap();

        File mainDir = new File(System.getProperty("user.home"), "heatTiles");
        if (!mainDir.exists() && !mainDir.mkdirs()) {
            if (isDebugMode) System.err.println("Failed to create main directory: " + mainDir.getAbsolutePath());
            return;
        }

        for (HeatPoint heatPoint : heatPoints) {
            GeoPosition position = new GeoPosition(heatPoint.longitude(), heatPoint.latitude());
            Point2D point = map.convertGeoPositionToPoint(position);
            double x = point.getX();
            double y = point.getY();
            BufferedImage colorTile = ColorUtil.getColorTile(heatPoint.time());

            for (int zoom : zoomLevels) {
                int tileX = (int) (x / 256);
                int tileY = (int) (y / 256);

                // Create directory structure under the main directory
                File zoomDir = new File(mainDir, String.valueOf(zoom));
                File xDir = new File(zoomDir, String.valueOf(tileX));
                if (!xDir.exists() && !xDir.mkdirs()) {
                    if (isDebugMode) System.err.println("Failed to create directory: " + xDir.getAbsolutePath());
                    continue;
                }

                // Save the image
                File tileFile = new File(xDir, tileY + ".png");
                try {
                    assert colorTile != null;
                    ImageIO.write(colorTile, "png", tileFile);
                } catch (IOException e) {
                    if (isDebugMode) System.err.println("Failed to save tile: " + tileFile.getAbsolutePath());
                }
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
        if (files == null || files.length == 0) {
            throw new IOException("No files to zip in directory: " + folder.getAbsolutePath());
        }

        for (File file : files) {
            if (file.isDirectory()) {
                if (parentFolder.isEmpty() && file.getName().equals("tile.openstreetmap.org")) {
                    zipDirectory(file, "", zos);
                } else {
                    zipDirectory(file, parentFolder + (parentFolder.isEmpty() ? "" : "/") + file.getName(), zos);
                }
                continue;
            }
            try (FileInputStream fis = new FileInputStream(file)) {
                String zipEntryName = parentFolder + "/" + file.getName();
                zos.putNextEntry(new ZipEntry(zipEntryName));
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