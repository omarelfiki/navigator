import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.view.javafx.BrowserView;
import com.teamdev.jxbrowser.engine.RenderingMode;
import javafx.application.Application;
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
    private boolean isOn = false; // State of the switch
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
        leftBar.widthProperty().set(350);
        leftBar.heightProperty().set(720);
        leftBar.setX(33);
        leftBar.setY(50);
        leftBar.setArcHeight(35);
        leftBar.setArcWidth(35);
        leftBar.setFill(Color.web("#5D5D5D"));
        leftBar.setOpacity(0.95);
        leftPane.getChildren().add(leftBar);

        Text title = new Text("Navigator");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font: 25 Ubuntu;");
        title.setX(50);
        title.setY(100);
        leftPane.getChildren().add(title);

        TextField startPlace = new TextField();
        startPlace.setPromptText("Starting Point");
        startPlace.setLayoutX(55);
        startPlace.setLayoutY(130);
        startPlace.setPrefSize(307, 41.74);
        leftPane.getChildren().add(startPlace);

        TextField endPlace = new TextField();
        endPlace.setPromptText("Destination");
        endPlace.setLayoutX(55);
        endPlace.setLayoutY(190);
        endPlace.setPrefSize(307, 41.74);
        leftPane.getChildren().add(endPlace);

        Text label = new Text("Navigate to see public transport \n options");
        label.setTextAlignment(TextAlignment.CENTER);
        label.setFill(Color.WHITE);
        label.setStyle("-fx-font: 15 Ubuntu;");
        label.setX(110);
        label.setY(400);
        leftPane.getChildren().add(label);

        Line line = new Line();
        line.setStartX(33);
        line.setStartY(709);
        line.setEndX(383);
        line.setEndY(709);
        line.setStroke(Color.WHITE);
        leftPane.getChildren().add(line);

        // Background rectangle (toggle track)
        Rectangle background = new Rectangle(60, 30);
        background.setArcWidth(30);
        background.setArcHeight(30);
        background.setFill(Color.LIGHTGRAY);

        // Toggle circle (switch knob)
        Circle knob = new Circle(15);
        knob.setFill(Color.WHITE);
        knob.setLayoutX(30);
        knob.setTranslateX(-15); // Start on the left
        knob.setTranslateY(15);

        // Pane to hold the switch
        Pane togglePane = new Pane(background, knob);
        togglePane.setPrefSize(60, 30);
        togglePane.setLayoutX(40);
        togglePane.setLayoutY(723);

        // Handle click event
        togglePane.setOnMouseClicked(_ -> toggleSwitch(knob, background));
        leftPane.getChildren().add(togglePane);

        Text toggleText = new Text("Hotmap Mode");
        toggleText.setFill(Color.WHITE);
        toggleText.setStyle("-fx-font: 10 Ubuntu;");
        toggleText.setX(110);
        toggleText.setY(742);
        leftPane.getChildren().add(toggleText);

        Image image = new Image("settingsIcon.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(25);
        imageView.setFitHeight(25);

        Button settings = new Button("", imageView);
        settings.setLayoutX(313);
        settings.setLayoutY(723);
        settings.setPrefSize(50, 30);
        settings.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        settings.setOnAction(_ -> System.out.println("Settings"));
        leftPane.getChildren().add(settings);

        startPlace.setOnAction(_ -> {
            String start = startPlace.getText();
            double[] coords = GeoUtil.getCoordinatesFromAddress(start);
            if (coords != null) {
                System.out.println("Coordinates: " + coords[0] + ", " + coords[1]);
                GTFSaccess gtfs = new GTFSaccess("rome-gtfs.database.windows.net", "rome-gtfs", "gtfsaccess", "Gtfs-142025");
                gtfs.connect();
                Stop closestStop = gtfs.getClosestStops(coords[0], coords[1]);
                System.out.println("Closest Stop: " + closestStop);
            } else {
                System.out.println("Address not found");
            }
        });

        Scene scene = new Scene(root, 1280, 832);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void toggleSwitch(Circle knob, Rectangle background) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(200), knob);

        if (isOn) {
            transition.setToX(-15); // Move knob to left
            background.setFill(Color.LIGHTGRAY); // Gray color for off state
        } else {
            transition.setToX(15); // Move knob to right
            background.setFill(Color.LIMEGREEN); // Green color for on state
        }

        isOn = !isOn; // Toggle the state
        transition.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}