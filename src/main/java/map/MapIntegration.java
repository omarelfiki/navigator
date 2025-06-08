package map;

import javafx.embed.swing.SwingNode;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static util.DebugUtil.*;

public class MapIntegration {
    JXMapViewer map;

    boolean isOnline;

    GeoPosition initialPosition = new GeoPosition(41.9028, 12.4964); // Rome

    public MapIntegration(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public StackPane createMapPane() {
        StackPane mapPane = new StackPane();
        SwingNode swingNode = new SwingNode();
        TileUtil tileUtil = new TileUtil(isOnline);
        TileFactory tileFactory = tileUtil.getTileFactory();

        // Setup local file cache
        setCache(tileFactory, tileUtil);

        map = new JXMapViewer();
        map.setTileFactory(tileFactory);
        map.setZoom(6);
        map.addPropertyChangeListener("zoom", _ -> map.repaint());

        String start_location = System.getenv("START_COORDS");
        if (start_location != null && !start_location.isEmpty()) {
            String[] parts = start_location.split(",");
            if (parts.length == 2) {
                try {
                    double lat = Double.parseDouble(parts[0].trim());
                    double lon = Double.parseDouble(parts[1].trim());
                    map.setAddressLocation(new GeoPosition(lat, lon));
                } catch (NumberFormatException e) {
                    sendError("Invalid start location format: " + start_location);
                    map.setAddressLocation(initialPosition);
                }
            } else {
                sendError("Invalid start location: " + start_location);
                map.setAddressLocation(initialPosition);
            }
        } else {
            map.setAddressLocation(initialPosition);
        }

        MouseInputListener panListener = new PanMouseInputListener(map);
        map.addMouseListener(panListener);
        map.addMouseMotionListener(panListener);
        map.addMouseListener(new CenterMapListener(map));
        map.addMouseWheelListener(new ZoomMouseWheelListenerCursor(map));
        map.addKeyListener(new PanKeyListener(map));

        swingNode.setContent(map);

        mapPane.getChildren().add(swingNode);

        VBox zoomControls = new VBox();
        zoomControls.setSpacing(10);
        zoomControls.setStyle("-fx-padding: 10;");
        zoomControls.setAlignment(Pos.CENTER);
        // Make sure only the buttons capture mouse events, not the entire VBox
        zoomControls.setPickOnBounds(false);

        Button zoomInButton = new Button("+");
        zoomInButton.setStyle("-fx-font-size: 18; -fx-background-color: grey; -fx-text-fill: white;");
        zoomInButton.setOnAction(_ -> {
            map.setZoom(map.getZoom() - 1); // Zoom in
        });
        
        Button zoomOutButton = new Button("-");
        zoomOutButton.setStyle("-fx-font-size: 18; -fx-background-color: grey; -fx-text-fill: white;");
        zoomOutButton.setOnAction(_ -> {
            map.setZoom(map.getZoom() + 1); // Zoom out
        });

        zoomControls.getChildren().addAll(zoomInButton, zoomOutButton);

        mapPane.getChildren().add(zoomControls);
        StackPane.setAlignment(zoomControls, Pos.BOTTOM_RIGHT);
        zoomControls.setTranslateX(380); // Adjust X position to move it more to the left
        zoomControls.setTranslateY(300); // Adjust Y position to move it more to the top

        map.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int width = map.getWidth();
                int height = map.getHeight();

                double xRatio = 380.0 / 929.0; // Adjusted X position ratio
                double yRatio = 300.0 / 816.0; // Adjusted Y position ratio

                zoomControls.setTranslateX(width * xRatio);
                zoomControls.setTranslateY(height * yRatio);
            }
        });

        return mapPane;
    }

    private void setCache(TileFactory tileFactory, TileUtil tileUtil) {
        if (!isOnline) return;
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
                sendInfo("Preloading tiles for initial position: " + initialPosition);
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

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                deleteDirectory(file);
            }
        }
        if (!dir.delete()) {
            sendError("Failed to delete: " + dir.getAbsolutePath());
        }
    }

    public JXMapViewer getMap() {
        return map;
    }
}