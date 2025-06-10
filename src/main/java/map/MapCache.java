package map;

import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.viewer.TileFactory;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static util.DebugUtil.*;
import static util.DebugUtil.sendError;

public class MapCache {
    public static void setCache(TileFactory tileFactory, TileUtil tileUtil) {
        File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
        File zipFile = new File(System.getProperty("user.home") + File.separator + "Archive_color.zip");
        long thirty = 30L * 24 * 60 * 60 * 1000; // 30 days in milliseconds

        // Check if the cache zip exists and is up to date
        if (zipFile.exists()) {
            long lastModified = zipFile.lastModified();
            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastModified) > thirty) {
                sendWarning("Cache is out of date: Reconstructing cache directory: " + cacheDir.getAbsolutePath());
                deleteDirectory(cacheDir);
                if (!zipFile.delete()) {
                    sendError("Failed to delete cache zip: " + zipFile.getAbsolutePath());
                }
            } else {
                long days = 30 - ((currentTime - lastModified) / (24 * 60 * 60 * 1000));
                sendInfo("Cache is up to date by " + days + " days: " + cacheDir.getAbsolutePath());
                return;
            }
        }

        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                sendError("Failed to create cache directory: " + cacheDir.getAbsolutePath());
                return;
            } else {
                sendInfo("Cache directory created at: " + cacheDir.getAbsolutePath());
                sendInfo("Preloading tiles for initial position");
                tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false));

            }
        } else {
            sendInfo("Cache directory exists at: " + cacheDir.getAbsolutePath());
        }

        String cacheDirPath = cacheDir.getAbsolutePath() + File.separator + "tile.openstreetmap.org";
        String zipFilePath = zipFile.getAbsolutePath();
        File tileCacheDir = new File(cacheDirPath);

        // Only create the zip if the tile cache directory exists and is populated
        if (tileCacheDir.exists() && tileCacheDir.isDirectory() && tileCacheDir.listFiles() != null && Objects.requireNonNull(tileCacheDir.listFiles()).length > 0) {
            try {
                tileUtil.createZip(cacheDirPath, zipFilePath);
                sendInfo("Cache zip created at: " + zipFilePath);
            } catch (IOException e) {
                sendError("Failed to create map cache zip file: " + e);
            }
        } else {
            sendError("Error: Tile cache directory does not exist or is not populated: " + cacheDirPath);
        }
    }

    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                deleteDirectory(file);
            }
        }
        if (!dir.delete()) {
            sendError("Failed to delete: " + dir.getAbsolutePath());
        }
    }
}
