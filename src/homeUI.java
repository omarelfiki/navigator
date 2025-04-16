import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import com.gluonhq.maps.MapView;
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
import javafx.scene.shape.Circle;
import java.util.Objects;

public class homeUI extends Application {


    StackPane settingMenu;
    boolean isMenuOpen = false;
    double[] romeCoords = {41.6558, 42.1233, 12.2453, 12.8558}; // {minLat, maxLat, minLng, maxLng}
    private final BooleanProperty isOn = new SimpleBooleanProperty(false); // State of the switch

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Rome Navigator");

        BorderPane root = new BorderPane();

        // Create a MapView instance
        MapView mapView = new MapView();
        mapView.setPrefWidth(600);
        mapView.setPrefHeight(600);
        mapView.setZoom(13);

        MapPoint romePoint = new MapPoint(41.9028, 12.4964); // Coordinates for Rome
        MapLayer layer = new MapLayer();
        mapView.addLayer(layer);
        mapView.setCenter(romePoint);

        StackPane mapPane = new StackPane(mapView);
        root.setCenter(mapPane);

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
            toggleLeftBar(leftPane);
            Pane settingsPane = setSettingMenu(root, leftPane);
            vbox_left.getChildren().add(settingsPane);
        });
        leftPane.getChildren().add(settings);

        Scene scene = new Scene(root, 1280, 832);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/resources/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void parsePoint(TextField field) {
        field.setOnAction(_ -> {
            String address = field.getText();
            double[] coords = GeoUtil.getCoordinatesFromAddress(address);
            if (coords != null) {
                if (coords[0] < romeCoords[0] || coords[0] > romeCoords[1] || coords[1] < romeCoords[2] || coords[1] > romeCoords[3]) {
                    System.out.println("coordinates out of bounds");
                    return;
                }
                System.out.println("Coordinates: " + coords[0] + ", " + coords[1]);
                GTFSaccess gtfs = new GTFSaccess("rome-gtfs.database.windows.net", "rome-gtfs", "gtfsaccess", "Gtfs-142025");
                gtfs.connect(1);
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
        parsePoint(textField);

        HBox.setHgrow(textField, Priority.ALWAYS);
        inner.getChildren().addAll(iconCircle, textField);

        container.getChildren().add(inner);
        return container;
    }

    private void toggleLeftBar(Pane leftPane) {
        boolean isVisible = leftPane.isVisible();
        leftPane.setVisible(!isVisible);
        leftPane.setManaged(!isVisible);
    }

    private void bindPosition(Region node, Pane root, double xRatio, double yRatio, double wRatio, double hRatio) {
        node.layoutXProperty().bind(root.widthProperty().multiply(xRatio));
        node.layoutYProperty().bind(root.heightProperty().multiply(yRatio));
        node.prefWidthProperty().bind(root.widthProperty().multiply(wRatio));
        node.prefHeightProperty().bind(root.heightProperty().multiply(hRatio));
    }

    private Pane setSettingMenu(BorderPane root, Pane leftPane) {
        Pane settingMenu = new Pane();

        Rectangle settings = new Rectangle();
        settings.widthProperty().bind(root.widthProperty().multiply(0.273)); // 350/1280
        settings.heightProperty().bind(root.heightProperty()); // Full height
        settings.setX(0);
        settings.setY(0);
        settings.setFill(Color.web("#5D5D5D"));
        settings.setOpacity(0.95);
        settingMenu.getChildren().add(settings);

        Text title = new Text("Settings");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font: 28 Ubuntu;");
        title.xProperty().bind(root.widthProperty().multiply(0.015)); // 20/1280
        title.yProperty().bind(root.heightProperty().multiply(0.06)); // 50/832
        settingMenu.getChildren().add(title);

        Text label = new Text("GTFS .zip Path");
        label.setTextAlignment(TextAlignment.CENTER);
        label.setFill(Color.WHITE);
        label.setStyle("-fx-font: 15 Ubuntu;");
        label.xProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        label.yProperty().bind(root.heightProperty().multiply(0.12)); // 100/832
        settingMenu.getChildren().add(label);

        TextField textField = new TextField();
        textField.setPromptText("Path to GTFS zip file");
        textField.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        textField.layoutYProperty().bind(root.heightProperty().multiply(0.144)); // 120/832
        textField.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        textField.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        settingMenu.getChildren().add(textField);

        Text label2 = new Text("MySQL Connection Details");
        label2.setTextAlignment(TextAlignment.CENTER);
        label2.setFill(Color.WHITE);
        label2.setStyle("-fx-font: 20 Ubuntu;");
        label2.xProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        label2.yProperty().bind(root.heightProperty().multiply(0.23)); // 200/832
        settingMenu.getChildren().add(label2);

        Text host = new Text("Host");
        host.setTextAlignment(TextAlignment.CENTER);
        host.setFill(Color.WHITE);
        host.setStyle("-fx-font: 15 Ubuntu;");
        host.xProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        host.yProperty().bind(root.heightProperty().multiply(0.276)); // 230/832
        settingMenu.getChildren().add(host);

        TextField hostField = new TextField();
        hostField.setPromptText("Host");
        hostField.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        hostField.layoutYProperty().bind(root.heightProperty().multiply(0.3)); // 250/832
        hostField.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        hostField.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        settingMenu.getChildren().add(hostField);

        Text user = new Text("User");
        user.setTextAlignment(TextAlignment.CENTER);
        user.setFill(Color.WHITE);
        user.setStyle("-fx-font: 15 Ubuntu;");
        user.xProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        user.yProperty().bind(root.heightProperty().multiply(0.385)); // 320/832
        settingMenu.getChildren().add(user);

        TextField userField = new TextField();
        userField.setPromptText("User");
        userField.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        userField.layoutYProperty().bind(root.heightProperty().multiply(0.41)); // 340/832
        userField.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        userField.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        settingMenu.getChildren().add(userField);

        Text password = new Text("Password");
        password.setTextAlignment(TextAlignment.CENTER);
        password.setFill(Color.WHITE);
        password.setStyle("-fx-font: 15 Ubuntu;");
        password.xProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        password.yProperty().bind(root.heightProperty().multiply(0.48)); // 400/832
        settingMenu.getChildren().add(password);

        TextField passwordField = new TextField();
        passwordField.setPromptText("Password");
        passwordField.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        passwordField.layoutYProperty().bind(root.heightProperty().multiply(0.5)); // 420/832
        passwordField.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        passwordField.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        settingMenu.getChildren().add(passwordField);

        Text port = new Text("Port");
        port.setTextAlignment(TextAlignment.CENTER);
        port.setFill(Color.WHITE);
        port.setStyle("-fx-font: 15 Ubuntu;");
        port.xProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        port.yProperty().bind(root.heightProperty().multiply(0.6)); // 500/832
        settingMenu.getChildren().add(port);

        TextField portField = new TextField();
        portField.setPromptText("Port");
        portField.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        portField.layoutYProperty().bind(root.heightProperty().multiply(0.625)); // 520/832
        portField.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        portField.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        settingMenu.getChildren().add(portField);

        Text database = new Text("Database");
        database.setTextAlignment(TextAlignment.CENTER);
        database.setFill(Color.WHITE);
        database.setStyle("-fx-font: 15 Ubuntu;");
        database.xProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        database.yProperty().bind(root.heightProperty().multiply(0.72)); // 600/832
        settingMenu.getChildren().add(database);

        TextField databaseField = new TextField();
        databaseField.setPromptText("Database");
        databaseField.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        databaseField.layoutYProperty().bind(root.heightProperty().multiply(0.745)); // 620/832
        databaseField.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        databaseField.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        settingMenu.getChildren().add(databaseField);

        Label testLabel = new Label();
        testLabel.setText("");
        testLabel.setTextFill(Color.WHITE);
        testLabel.setStyle("-fx-font: 15 Ubuntu;");
        testLabel.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        testLabel.layoutYProperty().bind(root.heightProperty().multiply(0.95)); // 700/832
        testLabel.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        testLabel.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        settingMenu.getChildren().add(testLabel);

        Button test = new Button("Test Connection");
        test.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        test.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        test.layoutYProperty().bind(root.heightProperty().multiply(0.8)); // 700/832
        test.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        test.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        test.setOnAction(event -> {
            testLabel.setText("");
            String hostText = hostField.getText();
            String userText = userField.getText();
            String passwordText = passwordField.getText();
            String portText = portField.getText();
            String databaseText = databaseField.getText();
            GTFSaccess gtfs = new GTFSaccess(hostText, portText, databaseText, userText, passwordText);
            gtfs.connect(2);
            if (gtfs.conn != null) {
                testLabel.setText("Connection Established");
            } else {
                testLabel.setText("Connection Failed");
            }
        });
        settingMenu.getChildren().add(test);

        Button importButton = new Button("Import GTFS");
        importButton.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        importButton.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        importButton.layoutYProperty().bind(root.heightProperty().multiply(0.85)); // 700/832
        importButton.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        importButton.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
//        importButton.setOnAction(event -> {
//            String pathText = textField.getText();
//            String hostText = hostField.getText();
//            String userText = userField.getText();
//            String passwordText = passwordField.getText();
//            String portText = portField.getText();
//            String databaseText = databaseField.getText();
//
//            GTFSaccess gtfs = new GTFSaccess(hostText, databaseText, userText, passwordText);
//            gtfs.connect();
////            gtfs.importGTFS(pathText);
//        });
        settingMenu.getChildren().add(importButton);

        Line line = new Line();
        line.startXProperty().bind(root.widthProperty().multiply(0)); // 0/1280
        line.startYProperty().bind(root.heightProperty().multiply(0.89).add(20)); // 740/832 + 20
        line.endXProperty().bind(root.widthProperty().multiply(0.273)); // 350/1280
        line.endYProperty().bind(root.heightProperty().multiply(0.89).add(20)); // 740/832 + 20
        line.setStroke(Color.WHITE);
        settingMenu.getChildren().add(line);

        Button close = new Button("X");
        close.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        close.layoutXProperty().bind(root.widthProperty().multiply(0.219)); // 280/1280
        close.layoutYProperty().bind(root.heightProperty().multiply(0.94)); // 50/832
        close.prefWidthProperty().bind(root.widthProperty().multiply(0.039)); // 50/1280
        close.prefHeightProperty().bind(root.heightProperty().multiply(0.036)); // 30/832
        close.setOnAction(event -> {
            settingMenu.setVisible(false);
            settingMenu.setManaged(false);
            toggleLeftBar(leftPane);
        });
        settingMenu.getChildren().add(close);

        return settingMenu;
    }


    public static void main(String[] args) {
        launch(args);
    }
}