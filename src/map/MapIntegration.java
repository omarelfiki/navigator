import javafx.embed.swing.SwingNode;
import javafx.scene.layout.StackPane;
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
                e.printStackTrace();
            }
        }
    }

    public JXMapViewer getMap() {
        return map;
    }
}