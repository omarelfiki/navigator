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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
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
import java.util.Objects;

public class homeUI extends Application {


    StackPane settingMenu;
    boolean isMenuOpen = false;
    double[] romeCoords = {41.6558, 42.1233, 12.2453, 12.8558}; // {minLat, maxLat, minLng, maxLng}

    Browser browser;
    private final BooleanProperty isOn = new SimpleBooleanProperty(false); // State of the switch
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
        browser = engine.newBrowser();
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
        title.setStyle("-fx-font: 28 Ubuntu;");
        title.xProperty().bind(root.widthProperty().multiply(0.015)); // 20/1280
        title.yProperty().bind(root.heightProperty().multiply(0.06)); // 50/832
        leftPane.getChildren().add(title);

        StackPane startGroup = createTextFieldWithIcon("↗", "Starting Point");
        bindPosition(startGroup, root, 0.017, 0.09, 0.24, 0.035);
        leftPane.getChildren().add(startGroup);

        StackPane endGroup = createTextFieldWithIcon("↙", "Destination");
        bindPosition(endGroup, root, 0.017, 0.18, 0.24, 0.035);
        leftPane.getChildren().add(endGroup);

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
        //TODO: fix button
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
        settings.setOnAction(event -> {
            if (!isMenuOpen) {
                createAndShowSettingMenu(leftPane);
                isMenuOpen = true;
            } else {
                leftPane.getChildren().remove(settingMenu);
                settingMenu = null;
                leftPane.setOnMouseClicked(null);
                isMenuOpen = false;
            }
        });
        leftPane.getChildren().add(settings);
        Scene scene = new Scene(root, 1280, 832);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/resources/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createAndShowSettingMenu(Pane leftPane) {
        settingMenu = new StackPane();
        settingMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85); -fx-background-radius: 10;");
        settingMenu.setPrefSize(200, 150);
        settingMenu.layoutXProperty().bind(leftPane.widthProperty().subtract(settingMenu.widthProperty()).divide(2));
        settingMenu.layoutYProperty().bind(leftPane.heightProperty().subtract(settingMenu.heightProperty()).divide(2));
        leftPane.getChildren().add(settingMenu);
        leftPane.setOnMouseClicked(e -> {
            if (settingMenu != null && !isClickInsideSettingMenu(e)) {
                leftPane.getChildren().remove(settingMenu);
                settingMenu = null;
                leftPane.setOnMouseClicked(null);
                isMenuOpen = false;
            }
        });
    }
    private boolean isClickInsideSettingMenu(javafx.scene.input.MouseEvent e) {
        if (settingMenu == null) {
            return false;
        }
        javafx.geometry.Bounds bounds = settingMenu.localToScene(settingMenu.getBoundsInLocal());
        return bounds.contains(e.getSceneX(), e.getSceneY());
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
        if (isOn.get()) {
            background.setFill(Color.LIGHTGRAY); // Off state
        } else {
            background.setFill(Color.LIMEGREEN); // On state
        }

        isOn.set(!isOn.get()); // Toggle the state
    }


    private StackPane createTextFieldWithIcon(String icon, String prompt) {
        StackPane container = new StackPane();
        container.getStyleClass().add("input-container");

        HBox inner = new HBox(10);
        inner.setStyle("-fx-padding: 10;");

        StackPane iconCircle = new StackPane();
        iconCircle.getStyleClass().add("circle-icon");

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("icon-label");
        iconCircle.getChildren().add(iconLabel);

        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.getStyleClass().add("rounded-textfield");
        parsePoint(browser, textField);

        HBox.setHgrow(textField, Priority.ALWAYS);
        inner.getChildren().addAll(iconCircle, textField);

        container.getChildren().add(inner);
        return container;
    }

    private void bindPosition(Region node, Pane root, double xRatio, double yRatio, double wRatio, double hRatio) {
        node.layoutXProperty().bind(root.widthProperty().multiply(xRatio));
        node.layoutYProperty().bind(root.heightProperty().multiply(yRatio));
        node.prefWidthProperty().bind(root.widthProperty().multiply(wRatio));
        node.prefHeightProperty().bind(root.heightProperty().multiply(hRatio));
    }


    public static void main(String[] args) {
        launch(args);
    }
}