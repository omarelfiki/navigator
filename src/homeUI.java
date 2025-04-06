import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.view.javafx.BrowserView;
import com.teamdev.jxbrowser.engine.RenderingMode;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.TranslateTransition;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.net.URL;

public class homeUI extends Application {

    double[] romeCoords = {41.6558, 42.1233, 12.2453, 12.8558}; // {minLat, maxLat, minLng, maxLng}
    private BooleanProperty isOn = new SimpleBooleanProperty(false); // State of the switch
    String licenseKey = "4UNGPZMYCRBBVOVZ0AWF82M7IHNDBS2EYN2C0FAYRVYOTVRTSSTZHLK2LVGNN0A6QRV6COK5SBS26" +
            "MOT46BELCJEJ1946IKC2CIZCU6CWEYUNGBLVUW1XGETH5MZ7UIRPV2ZNXW8FCK4DN99GBX";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Rome Navigator");

        BorderPane root = new BorderPane();
        EngineOptions options = EngineOptions.newBuilder(RenderingMode.HARDWARE_ACCELERATED)
                .licenseKey(licenseKey)
                .build();
        var engine = Engine.newInstance(options);
        var browser = engine.newBrowser();
        URL mapUrl = getClass().getResource("/resources/map.html");
        if (mapUrl != null) {
            browser.navigation().loadUrl(mapUrl.toString());
            System.out.println("Map loaded");
        } else {
            System.out.println("Map file not found");
        }
        BrowserView view = BrowserView.newInstance(browser);
        root.setCenter(view);

        Pane leftPane = new Pane();
        VBox vbox_left = new VBox();
        vbox_left.getChildren().add(leftPane);
        root.setLeft(vbox_left);

        Rectangle leftBar = new Rectangle();
        leftBar.widthProperty().bind(root.widthProperty().multiply(0.273)); // 350/1280
        leftBar.heightProperty().bind(root.heightProperty()); // Full height
        leftBar.setX(0);
        leftBar.setY(0);
        leftBar.setFill(Color.web("#5D5D5D"));
        leftBar.setOpacity(0.95);
        leftPane.getChildren().add(leftBar);

        Text title = new Text("Navigator");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font: 25 Ubuntu;");
        title.xProperty().bind(root.widthProperty().multiply(0.015)); // 20/1280
        title.yProperty().bind(root.heightProperty().multiply(0.06)); // 50/832
        leftPane.getChildren().add(title);

        TextField startPlace = new TextField();
        startPlace.setPromptText("Starting Point");
        startPlace.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 22/1280
        startPlace.layoutYProperty().bind(root.heightProperty().multiply(0.12)); // 100/832
        startPlace.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 307/1280
        startPlace.prefHeightProperty().bind(root.heightProperty().multiply(0.05)); // 41.74/832
        leftPane.getChildren().add(startPlace);

        TextField endPlace = new TextField();
        endPlace.setPromptText("Destination");
        endPlace.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 22/1280
        endPlace.layoutYProperty().bind(root.heightProperty().multiply(0.192)); // 160/832
        endPlace.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 307/1280
        endPlace.prefHeightProperty().bind(root.heightProperty().multiply(0.05)); // 41.74/832
        leftPane.getChildren().add(endPlace);

        Text label = new Text("Navigate to see public transport \n options");
        label.setTextAlignment(TextAlignment.CENTER);
        label.setFill(Color.WHITE);
        label.setStyle("-fx-font: 15 Ubuntu;");
        label.xProperty().bind(root.widthProperty().multiply(0.061)); // 78/1280
        label.yProperty().bind(root.heightProperty().multiply(0.48)); // 400/832
        leftPane.getChildren().add(label);

        Line line = new Line();
        line.startXProperty().bind(root.widthProperty().multiply(0)); // 0/1280
        line.startYProperty().bind(root.heightProperty().multiply(0.89).add(20)); // 740/832 + 20
        line.endXProperty().bind(root.widthProperty().multiply(0.273)); // 350/1280
        line.endYProperty().bind(root.heightProperty().multiply(0.89).add(20)); // 740/832 + 20
        line.setStroke(Color.WHITE);
        leftPane.getChildren().add(line);

        // Background rectangle (toggle track)
        Rectangle background = new Rectangle();
        background.widthProperty().bind(root.widthProperty().multiply(0.047)); // 60/1280
        background.heightProperty().bind(root.heightProperty().multiply(0.036)); // 30/832
        background.setArcWidth(30);
        background.setArcHeight(30);
        background.setFill(Color.LIGHTGRAY);

        // Toggle circle (switch knob)
        Circle knob = new Circle();
        knob.radiusProperty().bind(root.widthProperty().multiply(0.012)); // 15/1280
        knob.setFill(Color.WHITE);
        knob.layoutXProperty().bind(root.widthProperty().multiply(0.023)); // 30/1280
        knob.translateXProperty().bind(Bindings.when(isOn).then(root.widthProperty().multiply(0.012)).otherwise(root.widthProperty().multiply(-0.012))); // 15/1280 or -15/1280
        knob.translateYProperty().bind(root.heightProperty().multiply(0.018)); // 15/832

        // Pane to hold the switch
        Pane togglePane = new Pane(background, knob);
        togglePane.prefWidthProperty().bind(root.widthProperty().multiply(0.047)); // 60/1280
        togglePane.prefHeightProperty().bind(root.heightProperty().multiply(0.036)); // 30/832
        togglePane.layoutXProperty().bind(root.widthProperty().multiply(0.015)); // 20/1280
        togglePane.layoutYProperty().bind(root.heightProperty().multiply(0.913).add(20)); // 760/832 + 20

        // Handle click event
        togglePane.setOnMouseClicked(_ -> toggleSwitch(knob, background));
        leftPane.getChildren().add(togglePane);

        Text toggleText = new Text("Heatmap Mode");
        toggleText.setFill(Color.WHITE);
        toggleText.setStyle("-fx-font: 10 Ubuntu;");
        toggleText.xProperty().bind(root.widthProperty().multiply(0.07)); // 90/1280
        toggleText.yProperty().bind(root.heightProperty().multiply(0.935).add(20)); // 778/832 + 20
        leftPane.getChildren().add(toggleText);

        Image image = new Image("settingsIcon.png");
        ImageView imageView = new ImageView(image);
        imageView.fitWidthProperty().bind(root.widthProperty().multiply(0.02)); // 25/1280
        imageView.fitHeightProperty().bind(root.heightProperty().multiply(0.03)); // 25/832

        Button settings = new Button("", imageView);
        settings.layoutXProperty().bind(root.widthProperty().multiply(0.219)); // 280/1280
        settings.layoutYProperty().bind(root.heightProperty().multiply(0.913).add(20)); // 760/832 + 20
        settings.prefWidthProperty().bind(root.widthProperty().multiply(0.039)); // 50/1280
        settings.prefHeightProperty().bind(root.heightProperty().multiply(0.036)); // 30/832
        settings.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        settings.setOnAction(_ -> System.out.println("Settings"));
        leftPane.getChildren().add(settings);

        parsePoint(browser, startPlace);
        parsePoint(browser, endPlace);

        Scene scene = new Scene(root, 1280, 832);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void parsePoint(Browser browser, TextField field) {
        field.setOnAction(_ -> {
            String address = field.getText();
            double[] coords = GeoUtil.getCoordinatesFromAddress(address);
            if (coords != null) {
                if (coords[0] < romeCoords[0] || coords[0] > romeCoords[1] || coords[1] < romeCoords[2] || coords[1] > romeCoords[3]) {
                    System.out.println("coordinates out of bounds");
                    return;
                }
                System.out.println("Coordinates: " + coords[0] + ", " + coords[1]);
                browser.mainFrame().ifPresent(frame -> frame.executeJavaScript(
                        "updateMap(" + coords[0] + ", " + coords[1] + ");"
                ));
                GTFSaccess gtfs = new GTFSaccess("rome-gtfs.database.windows.net", "rome-gtfs", "gtfsaccess", "Gtfs-142025");
                gtfs.connect();
                Stop closestStop = gtfs.getClosestStops(coords[0], coords[1]);
                System.out.println("Closest Stop: " + closestStop);
            } else {
                System.out.println("Address not found");
            }
        });
    }

    private void toggleSwitch(Circle knob, Rectangle background) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(200), knob);

        if (isOn.get()) {
            transition.setToX(-15); // Move knob to left
            background.setFill(Color.LIGHTGRAY); // Gray color for off state
        } else {
            transition.setToX(15); // Move knob to right
            background.setFill(Color.LIMEGREEN); // Green color for on state
        }

        isOn.set(!isOn.get()); // Toggle the state
        transition.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}