import com.gluonhq.maps.MapView;
import com.gluonhq.maps.tiles.TileSource;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javafx.scene.image.Image;
import com.gluonhq.maps.tiles.Tile;


public class OfflineMapApp extends Application {

    @Override
    public void start(Stage stage) {
        TileProvider localTileProvider = (x, y, zoom) -> {
            try {
                String tilePath = "/your/local/folder/" + zoom + "/" + x + "/" + y + ".png";
                File file = new File(tilePath);
                if (file.exists()) {
                    InputStream inputStream = new FileInputStream(file);
                    Image image = new Image(inputStream);
                    return new Tile(image);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Tile.EMPTY_TILE;
        };

    }

    public static void main(String[] args) {
        launch(args);
    }
}
