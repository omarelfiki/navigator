package map;

import javafx.embed.swing.SwingNode;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import javax.swing.event.MouseInputListener;
import java.io.File;
import java.io.IOException;

public class MapIntegration {
    JXMapViewer map;

    Boolean isOnline;

    public MapIntegration(Boolean isOnline) {
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
        map.setAddressLocation(new GeoPosition(41.9028, 12.4964)); // Rome

        MouseInputListener mia = new PanMouseInputListener(map);
        map.addMouseListener(mia);
        map.addMouseMotionListener(mia);
        map.addMouseListener(new CenterMapListener(map));
        map.addMouseWheelListener(new ZoomMouseWheelListenerCursor(map));
        map.addKeyListener(new PanKeyListener(map));

        swingNode.setContent(map);

        mapPane.getChildren().add(swingNode);

        VBox zoomControls = new VBox();
        zoomControls.setSpacing(10);
        zoomControls.setStyle("-fx-padding: 10;");
        zoomControls.setAlignment(Pos.CENTER);


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
        zoomControls.setTranslateX(430); // Initial X position
        zoomControls.setTranslateY(350); // Initial Y position

        map.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int width = map.getWidth();
                int height = map.getHeight();

                // Calculate new positions based on the initial ratio
                double xRatio = 430.0 / 929.0; // Initial X position ratio
                double yRatio = 350.0 / 816.0; // Initial Y position ratio

                zoomControls.setTranslateX(width * xRatio);
                zoomControls.setTranslateY(height * yRatio);
            }
        });

        return mapPane;
    }

    private void setCache(TileFactory tileFactory, TileUtil tileUtil) {
        if (isOnline) {
            File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
            if (!cacheDir.exists()) {
                if (cacheDir.mkdirs()) {
                    System.out.println("Cache directory created: " + cacheDir.getAbsolutePath());
                } else {
                    System.err.println("Failed to create cache directory: " + cacheDir.getAbsolutePath());
                }
            }
            tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false));
            String cacheDirPath = System.getProperty("user.home") + File.separator + ".jxmapviewer2" + File.separator + "tile.openstreetmap.org";
            String zipFilePath = System.getProperty("user.home") + File.separator + "Archive.zip";
            try {
                tileUtil.createZip(cacheDirPath, zipFilePath);
                System.out.println("Cache created at: " + zipFilePath);
            } catch (IOException e) {
                System.err.println("Failed to create map cache zip file: " + e);
            }
        }
    }

    public JXMapViewer getMap() {
        return map;
    }
}