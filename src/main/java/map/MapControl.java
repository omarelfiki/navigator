package map;

import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import util.HeatMapRouter;
import java.util.List;

public class MapControl {

    public Task<Void> createRouterTask(double lat, double lon, StackPane mapStack, SwingNode heatmapNode, boolean isDebugMode) {
        return new Task<>() {
            @Override
            protected Void call() {
                HeatMapRouter router = new HeatMapRouter();
                if (isDebugMode) {
                    System.err.println("HeatmapRouter initialized with coordinates: " + lat + ", " + lon);
                }
                List<HeatPoint> heatPoints = router.toHeatPoints(router.buildWithoutWalk(lat, lon, "9:30:00"));
                JXMapViewer baseMap = MapProvider.getInstance().getMap();
                HeatMap heatMap = new HeatMap(new GeoPosition(lat, lon), heatPoints, baseMap);
                JXMapViewer map = heatMap.getHeatMap();

                javafx.application.Platform.runLater(() -> {
                    heatmapNode.setContent(map);
                    heatmapNode.setOpacity(0.5);
                    heatmapNode.setMouseTransparent(true);
                    mapStack.getChildren().add(heatmapNode);
                    addZoomControls(mapStack, map, baseMap);
                });
                return null;
            }
        };
    }

    private void addZoomControls(StackPane mapStack, JXMapViewer map, JXMapViewer baseMap) {
        MapIntegration mapIntegration = MapProvider.getInstance();
        VBox zoomControls = mapIntegration.getZoomControls();
        if (zoomControls != null) {
            mapStack.getChildren().remove(zoomControls);
            mapStack.getChildren().add(zoomControls);
            if (zoomControls.getChildren().size() >= 2 &&
                zoomControls.getChildren().get(0) instanceof javafx.scene.control.Button zoomInBtn &&
                zoomControls.getChildren().get(1) instanceof javafx.scene.control.Button zoomOutBtn) {
                zoomInBtn.setOnAction(_ -> {
                    int newZoom = map.getZoom() - 1;
                    map.setZoom(newZoom);
                    baseMap.setZoom(newZoom);
                });
                zoomOutBtn.setOnAction(_ -> {
                    int newZoom = map.getZoom() + 1;
                    map.setZoom(newZoom);
                    baseMap.setZoom(newZoom);
                });
            }
        }
    }
}