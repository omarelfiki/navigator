
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
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
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import java.util.*;
public class homeUI extends Application {
    double[] romeCoords = {41.6558, 42.1233, 12.2453, 12.8558}; // {minLat, maxLat, minLng, maxLng}
    private final BooleanProperty isOn = new SimpleBooleanProperty(false);
    private Button hideSidePanel, showSidePanel;
    public boolean isOnline;
    private DBaccess access;
    private MapIntegration mapIntegration;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Rome Navigator");

        initializeNetwork();
        access = DBaccessProvider.getInstance();

        BorderPane root = new BorderPane();

        mapIntegration = new MapIntegration(isOnline);
        StackPane mapPane = mapIntegration.createMapPane();
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
        bindPosition(startGroup, root, 0.09);
        leftPane.getChildren().add(startGroup);

        StackPane endGroup = createTextFieldWithIcon("↙", "Destination");
        bindPosition(endGroup, root, 0.18);
        leftPane.getChildren().add(endGroup);

        parsePoint(
                (TextField) ((HBox) startGroup.getChildren().getFirst()).getChildren().get(1),
                (TextField) ((HBox) endGroup.getChildren().getFirst()).getChildren().get(1)
        );

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

        //Button to hide side panel
        hideSidePanel = new Button("<<");
        hideSidePanel.layoutXProperty().bind(root.widthProperty().multiply(0.245));
        hideSidePanel.layoutYProperty().bind(root.heightProperty().multiply(0.46));
        hideSidePanel.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        hideSidePanel.setOnAction(_ -> {
            toggleLeftBar(leftPane);
            hideSidePanel.setVisible(false);
            showSidePanel.setVisible(true);
        });
        leftPane.getChildren().add(hideSidePanel);

        // Button to show side panel (initially hidden)
        showSidePanel = new Button(">>");
        showSidePanel.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        showSidePanel.setVisible(false);
        mapPane.getChildren().add(showSidePanel);
        StackPane.setAlignment(showSidePanel, Pos.CENTER_LEFT);
        showSidePanel.translateXProperty().bind(root.widthProperty().multiply(0.01));
        showSidePanel.translateYProperty().bind(root.heightProperty().multiply(-0.04));
        showSidePanel.setOnAction(_ -> {
            toggleLeftBar(leftPane);
            showSidePanel.setVisible(false);
            hideSidePanel.setVisible(true);
        });

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
        togglePane.setOnMouseClicked(_ -> toggleSwitch(background));
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
        settings.setOnAction(_ -> {
            toggleLeftBar(leftPane);
            showSettingsMenu(root, leftPane, vbox_left);
        });
        leftPane.getChildren().add(settings);

        Scene scene = new Scene(root, 1280, 832);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/resources/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        if (access == null) {
            ErrorPopup.showError("SQL Error", "Database connection failed. Please check your configuration.");
        }
    }

    private void initializeNetwork() {
        if (NetworkUtil.isNetworkAvailable()) {
            isOnline = true;
        } else {
            System.out.println("Network is not available. Switching to offline mode.");
            isOnline = false;
        }
    }

    private void parsePoint(TextField origin, TextField destination) {
        JXMapViewer map = mapIntegration.getMap();
        destination.setOnAction(_ -> {
            String oaddress = origin.getText();
            String daddress = destination.getText();
            if (oaddress.isEmpty() || daddress.isEmpty()) {
                System.out.println("Please enter both origin and destination addresses.");
                return;
            }

            double[] ocoords = GeoUtil.getCoordinatesFromAddress(oaddress);
            double[] dcoords = GeoUtil.getCoordinatesFromAddress(daddress);

            if (ocoords != null && dcoords != null) {
                if (ocoords[0] < romeCoords[0] || ocoords[0] > romeCoords[1] || ocoords[1] < romeCoords[2] || ocoords[1] > romeCoords[3]) {
                    System.out.println("origin coordinates out of bounds");
                    return;
                } else if (dcoords[0] < romeCoords[0] || dcoords[0] > romeCoords[1] || dcoords[1] < romeCoords[2] || dcoords[1] > romeCoords[3]) {
                    System.out.println("destination coordinates out of bounds");
                    return;
                }

                System.out.println("Origin Coordinates: " + ocoords[0] + ", " + ocoords[1]);
                System.out.println("Destination Coordinates: " + dcoords[0] + ", " + dcoords[1]);
                GeoPosition op = new GeoPosition(ocoords[0], ocoords[1]);
                GeoPosition dp = new GeoPosition(dcoords[0], dcoords[1]);
                List<GeoPosition> track = Arrays.asList(op, dp);
                RoutePainter routePainter = new RoutePainter(track);

                map.zoomToBestFit(new HashSet<>(track), 0.7);
                Set<Waypoint> waypoints = new HashSet<>(Arrays.asList(
                        new DefaultWaypoint(op),
                        new DefaultWaypoint(dp)
                ));

                WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
                waypointPainter.setWaypoints(waypoints);

                List<Painter<JXMapViewer>> painters = new ArrayList<>();
                painters.add(routePainter);
                painters.add(waypointPainter);

                CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                map.setOverlayPainter(painter);

                access.connect();
                Stop closestStop = access.getClosestStops(ocoords[0], ocoords[1]);
                System.out.println("Closest Stop: " + closestStop);
            } else {
                System.out.println("Address not found");
            }
        });
    }

    private void toggleSwitch(Rectangle background) {
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

    private void bindPosition(Region node, Pane root, double yRatio) {
        node.layoutXProperty().bind(root.widthProperty().multiply(0.017));
        node.layoutYProperty().bind(root.heightProperty().multiply(yRatio));
        node.prefWidthProperty().bind(root.widthProperty().multiply(0.24));
        node.prefHeightProperty().bind(root.heightProperty().multiply(0.035));
    }

    private void showSettingsMenu(BorderPane root, Pane leftPane, VBox vbox_left) {
        settingsUI settingsUI = new settingsUI(root, leftPane);
        Pane settingsPane = settingsUI.createSettingsMenu();
        vbox_left.getChildren().add(settingsPane);
    }


    public static void main(String[] args) {
        launch(args);
    }
}