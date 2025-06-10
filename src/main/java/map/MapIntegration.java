package map;

import javafx.embed.swing.SwingNode;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactory;
import javax.swing.event.MouseInputListener;
import static map.MapCache.setCache;
import static util.DebugUtil.*;

public class MapIntegration {
    JXMapViewer map;

    boolean isOnline;

    public MapIntegration(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public StackPane createMapPane() {
        StackPane mapPane = new StackPane();
        SwingNode swingNode = new SwingNode();
        TileUtil tileUtil = new TileUtil(isOnline);
        TileFactory tileFactory = tileUtil.getTileFactory();

        // Setup local file cache
        if (isOnline) setCache(tileFactory, tileUtil);

        map = new JXMapViewer();
        map.setTileFactory(tileFactory);
        map.setZoom(6);
        map.addPropertyChangeListener("zoom", _ -> map.repaint());
        setInitialPosition();

        MouseInputListener panListener = new PanMouseInputListener(map);
        map.addMouseListener(panListener);
        map.addMouseMotionListener(panListener);
        map.addMouseListener(new CenterMapListener(map));
        map.addMouseWheelListener(new ZoomMouseWheelListenerCursor(map));
        map.addKeyListener(new PanKeyListener(map));

        swingNode.setContent(map);
        mapPane.getChildren().add(swingNode);
        createZoomControls(mapPane);

        return mapPane;
    }

    private void createZoomControls(StackPane mapPane) {
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
        zoomControls.setTranslateX(430);
        zoomControls.setTranslateY(350);

        map.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int width = map.getWidth();
                int height = map.getHeight();

                double xRatio = 430.0 / 929.0;
                double yRatio = 350.0 / 816.0;

                zoomControls.setTranslateX(width * xRatio);
                zoomControls.setTranslateY(height * yRatio);
            }
        });
    }

    private void setInitialPosition() {
        GeoPosition initialPosition = new GeoPosition(41.9028, 12.4964); // Default to Rome, Italy
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
    }

    public JXMapViewer getMap() {
        return map;
    }
}