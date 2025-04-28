//import com.gluonhq.maps.MapLayer;
//import com.gluonhq.maps.MapPoint;
//import com.gluonhq.maps.MapView;
//import com.gluonhq.impl.maps.tile.osm.CachedOsmTileRetriever;
//import javafx.application.Application;
//import javafx.scene.Scene;
//import javafx.scene.layout.StackPane;
//import javafx.stage.Stage;
//
//public class OfflineMapWithCachedOsmTileRetriever extends Application {
//
//    private static final String TILE_CACHE_DIR = "tiles"; // Directory for cached tiles
//
//    @Override
//    public void start(Stage primaryStage) {
//        // Create a CachedOsmTileRetriever for offline caching
//        CachedOsmTileRetriever tileRetriever = new CachedOsmTileRetriever(TILE_CACHE_DIR);
//
//        // Create a MapView with the CachedOsmTileRetriever
//        MapView mapView = new MapView(tileRetriever);
//
//        // Add a marker to the map (optional)
//        MapPoint initialPoint = new MapPoint(37.7749, -122.4194); // Example: San Francisco coordinates
//        MapLayer markerLayer = new MapLayer();
//        markerLayer.addPoint(initialPoint);
//        mapView.addLayer(markerLayer);
//
//        // Set the initial map position and zoom level
//        mapView.setCenter(initialPoint);
//        mapView.setZoom(12);
//
//        // Create the UI
//        StackPane root = new StackPane(mapView);
//        Scene scene = new Scene(root, 800, 600);
//
//        primaryStage.setTitle("Offline Map with CachedOsmTileRetriever");
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//}