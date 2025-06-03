package com.navigator14;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
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
import java.awt.geom.Point2D;
import models.HeatPoint;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import router.AStarRouterV;
import router.HeatMapRouter;
import util.NetworkUtil;
import router.Node;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import map.WayPoint;
import map.*;
import db.*;
import ui.*;
import static util.DebugUtil.getDebugMode;
import static util.NavUtil.parsePoint;
import static ui.UiHelper.*;
import static util.GeoUtil.*;

public class HomeUI extends Application {
    private final BooleanProperty isOn = new SimpleBooleanProperty(false);
    private Button hideSidePanel, showSidePanel;
    private boolean firstClick = true;
    private final Set<Waypoint> waypoints = new HashSet<>();
    private DefaultWaypoint originWaypoint;
    private DefaultWaypoint destinationWaypoint;
    private final WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Navigator");
        DBAccess access = DBAccessProvider.getInstance();
        BorderPane root = new BorderPane();

        MapIntegration mapIntegration = MapProvider.getInstance();
        StackPane mapPane = mapIntegration.createMapPane();
        root.setCenter(mapPane);

        Pane leftPane = new Pane();
        VBox vbox_left = new VBox();
        vbox_left.getChildren().add(leftPane);
        root.setLeft(vbox_left);

        Rectangle leftBar = getLeftBar(root);
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

        endGroup.visibleProperty().bind(isOn.not());

        TextField originField = (TextField) ((HBox) startGroup.getChildren().getFirst()).getChildren().get(1);
        TextField destinationField = (TextField) ((HBox) endGroup.getChildren().getFirst()).getChildren().get(1);

        StackPane timeContainer = createDateTimeContainer("⏰", "Time", 0.026, 0.12, 0.036, root, 1);
        StackPane dateContainer = createDateTimeContainer("🗓️", "Date", 0.11, 0.132, 0.061, root, 0);
        leftPane.getChildren().addAll(timeContainer, dateContainer);

        TextField timeField = (TextField) ((HBox) timeContainer.getChildren().getFirst()).getChildren().get(1);
        DatePicker dateField = (DatePicker) ((HBox) dateContainer.getChildren().getFirst()).getChildren().getFirst();

        Button flipButton = createFlipButton(root, originField, destinationField, dateField, timeField);
        leftPane.getChildren().add(flipButton);
        flipButton.visibleProperty().bind(isOn.not());

        BooleanBinding filled = Bindings.createBooleanBinding(
                () -> !originField.getText().isEmpty() &&
                        !destinationField.getText().isEmpty() &&
                        !timeField.getText().isEmpty() &&
                        dateField.getValue() != null,
                originField.textProperty(),
                destinationField.textProperty(),
                timeField.textProperty(),
                dateField.valueProperty()
        );

        Text label = new Text("Navigate to see public transport \n options");
        label.setTextAlignment(TextAlignment.CENTER);
        label.setFill(Color.WHITE);
        label.setStyle("-fx-font: 15 Ubuntu;");
        label.xProperty().bind(root.widthProperty().multiply(0.061)); // 78/1280
        label.yProperty().bind(root.heightProperty().multiply(0.48)); // 400/832
        leftPane.getChildren().add(label);


        AtomicReference<StackPane> resultPaneRef = new AtomicReference<>(new StackPane());
        leftPane.getChildren().add(resultPaneRef.get());

        dateField.valueProperty().addListener((_, _, _) -> {
            if (filled.get()) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        String origin = originField.getText();
                        String destination = destinationField.getText();
                        String time = timeField.getText();
                        Platform.runLater(() -> label.setText("Finding routes..."));

                        List<Node> result = parsePoint(origin, destination, time);

                        Platform.runLater(() -> {
                            if (result == null) {
                                label.setText("No route found.");
                            } else {
                                label.setVisible(false);
                                StackPane newResult = displayTransportModes(result.getLast(), root);
                                resultPaneRef.get().getChildren().setAll(newResult.getChildren());
                                resultPaneRef.get().setTranslateX(newResult.getTranslateX());
                                resultPaneRef.get().setTranslateY(newResult.getTranslateY());
                                resultPaneRef.get().setVisible(true);
                            }
                        });

                        return null;
                    }
                };
                new Thread(task).start();
            } else {
                Platform.runLater(() -> label.setText("Navigate to see public transport \n options"));
            }
        });

        Line line = getLine(root);
        leftPane.getChildren().add(line);

        // Background rectangle (toggle track)
        Rectangle background = new Rectangle();
        background.widthProperty().bind(root.widthProperty().multiply(0.047)); // 60/1280
        background.heightProperty().bind(root.heightProperty().multiply(0.036)); // 30/832
        background.setArcWidth(30);
        background.setArcHeight(30);
        background.setFill(Color.LIGHTGRAY);

        //Button to hide side panel
        hideSidePanel = new Button("←");
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
        showSidePanel = new Button("→");
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
        togglePane.setId("togglePane");

        Text toggleText = new Text("Heatmap Mode");
        toggleText.setFill(Color.WHITE);
        toggleText.setStyle("-fx-font: 10 Ubuntu;");
        toggleText.xProperty().bind(root.widthProperty().multiply(0.07)); // 90/1280
        toggleText.yProperty().bind(root.heightProperty().multiply(0.935).add(20)); // 778/832 + 20
        leftPane.getChildren().add(toggleText);

        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/settingsIcon.png")));
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

        Button searchButton = new Button("Search");
        searchButton.prefWidthProperty().bind(root.widthProperty().multiply(0.07));
        searchButton.prefHeightProperty().bind(root.heightProperty().multiply(0.04));
        searchButton.layoutXProperty().bind(root.widthProperty().multiply(0.02));
        searchButton.layoutYProperty().bind(root.heightProperty().multiply(0.85));
        searchButton.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        setHeatMapListener(searchButton,originField, isOn, label);
        leftPane.getChildren().add(searchButton);
        searchButton.setVisible(false);
        searchButton.setId("searchButton");

        // Clear fields button
        Button clearButton = new Button("Clear");
        clearButton.prefWidthProperty().bind(root.widthProperty().multiply(0.07));
        clearButton.prefHeightProperty().bind(root.heightProperty().multiply(0.04));
        clearButton.layoutXProperty().bind(root.widthProperty().multiply(0.1));
        clearButton.layoutYProperty().bind(root.heightProperty().multiply(0.85));
        clearButton.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        clearButton.setOnAction(_ -> {
            if (isOn.get()) {
                label.setText("Heatmap Mode Activated. \n Enter an origin point");
            } else {
                destinationField.clear();
                timeField.clear();
                dateField.setValue(null);
                label.setText("Navigate to see public transport \n options");
                label.setVisible(true);
                resultPaneRef.get().setVisible(false);
                destinationWaypoint = null;
            }
            originField.clear();
            originWaypoint = null;
            WayPoint.clearRoute();
            AStarRouterV router = new AStarRouterV();
            router.reset();
            waypoints.clear();
        });
        leftPane.getChildren().add(clearButton);
        clearButton.setVisible(false);

        bindElements(timeContainer, dateContainer, clearButton, originField, destinationField);

        JXMapViewer map = mapIntegration.getMap();

        map.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Point2D point = e.getPoint();
                GeoPosition geoPosition = map.convertPointToGeoPosition(point);
                double lat = geoPosition.getLatitude();
                double lon = geoPosition.getLongitude();
                updateCoordinateFields(lat, lon, originField, destinationField);
            }
        });

        isOn.addListener((_, _, _) -> {
            if (isOn.get()) {
                title.setText("Heatmap");
                label.setText("Heatmap Mode Activated. \n Enter an origin point...");
                originField.clear();
                if (!searchButton.isVisible()) {
                    searchButton.setVisible(true);
                }
            } else {
                title.setText("Navigator");
                label.setText("Navigate to see public transport \n options");
                originField.clear();
                destinationField.clear();
                if (searchButton.isVisible()) {
                    searchButton.setVisible(false);
                }
            }
        });

        Scene scene = new Scene(root, 1280, 832);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        if (access == null) {
            ErrorPopup.showError("SQL Error", "Database connection failed. Please check your configuration.");
        }

        originField.setId("originField");
        destinationField.setId("destinationField");
        timeField.setId("timeField");
        dateField.setId("dateField");
    }



    private void toggleSwitch(Rectangle background) {
        isOn.set(!isOn.get());
        if (isOn.get()) {
            background.setFill(Color.LIMEGREEN);
            if (originWaypoint != null) {
                waypoints.clear();
                waypoints.add(originWaypoint);
                waypointPainter.setWaypoints(waypoints);
                JXMapViewer map = MapProvider.getInstance().getMap();
                map.setOverlayPainter(waypointPainter);

            }
            firstClick = true;
        } else {
            background.setFill(Color.LIGHTGRAY);
            if (destinationWaypoint != null && originWaypoint != null) {
                waypoints.clear();
                waypoints.add(originWaypoint);
                waypoints.add(destinationWaypoint);
                waypointPainter.setWaypoints(waypoints);
                JXMapViewer map = MapProvider.getInstance().getMap();
                map.setOverlayPainter(waypointPainter);
            }
        }
    }

    // this method to add the markers on the clicks
    private void addMarkerOnClicks(double lat, double lon, boolean isOrigin) {
        MapIntegration mapIntegration = MapProvider.getInstance();
        JXMapViewer map = mapIntegration.getMap();
        GeoPosition geoPosition = new GeoPosition(lat, lon);
        if (isOrigin) {
            waypoints.clear();
            WayPoint.clearRoute();
            originWaypoint = new DefaultWaypoint(geoPosition);
            waypoints.add(originWaypoint);
        } else {
            destinationWaypoint = new DefaultWaypoint(geoPosition);
            waypoints.add(destinationWaypoint);
        }
        waypointPainter.setWaypoints(waypoints);
        map.setOverlayPainter(waypointPainter);
    }

    // this method to update the fields
    private void updateCoordinateFields(double lat, double lon, TextField originField, TextField destinationField) {
        // Format the coordinates
        String coordinateText = String.format("%.6f, %.6f", lat, lon);
        if (firstClick && originField.getText().isEmpty()) {
            addMarkerOnClicks(lat, lon, true);
            Platform.runLater(() -> {
                if (NetworkUtil.isNetworkAvailable()) {
                    String address = getAddress(lat, lon);
                    originField.setText(Objects.requireNonNullElse(address, coordinateText));
                } else {
                    originField.setText(coordinateText);
                }
                firstClick = false;
            });
        } else if (!firstClick && destinationField.getText().isEmpty()) {
            addMarkerOnClicks(lat, lon, false);
            Platform.runLater(() -> {
                if (NetworkUtil.isNetworkAvailable()) {
                    String address = getAddress(lat, lon);
                    destinationField.setText(Objects.requireNonNullElse(address, coordinateText));
                } else {
                    destinationField.setText(coordinateText);
                }
                firstClick = true;
            });
        }
    }

    private StackPane displayTransportModes(Node destinationNode, BorderPane root) {
        // StackPane creation and styling - do not change
        StackPane resultPane = new StackPane();
        resultPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-padding: 10;");
        resultPane.setAlignment(Pos.CENTER);
        resultPane.setPrefSize(300, 200);
        resultPane.setTranslateX(root.getWidth() * 0.02); // 10/1280
        resultPane.setTranslateY(root.getHeight() * 0.4); // 80/832

        Set<String> modes = new LinkedHashSet<>(); // To avoid duplicates
        Node current = destinationNode;
        while (current != null) {
            if (current.getMode() != null && !current.getMode().isBlank()) {
                modes.add(current.getMode());
            }
            current = current.getParent();
        }

        Text transportTitle = new Text("Modes of Transport:");
        transportTitle.setStyle("-fx-font: 16 Ubuntu; -fx-fill: white;");

        VBox contentBox = new VBox(10);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().add(transportTitle);

        for (String mode : modes) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER);

            Text modeText = new Text(mode);
            modeText.setStyle("-fx-font: 14 Ubuntu; -fx-fill: white;");
            row.getChildren().add(modeText);

            // ImageView icon = getModeIcon(mode);
            // if (icon != null) row.getChildren().add(icon);

            contentBox.getChildren().add(row);
        }

        resultPane.getChildren().add(contentBox);
        return resultPane;
    }

//    private ImageView getModeIcon(String mode) {
//        try {
//            String iconPath = switch (mode.toLowerCase()) {

    /// /             add pictures, after case include path
//                case "bus" ->
//                case "walk" ->
//                case "metro" ->
//                default -> null;
//            };
//
//            if (iconPath != null) {
//                Image icon = new Image(getClass().getResourceAsStream(iconPath));
//                ImageView imageView = new ImageView(icon);
//                return imageView;
//            }
//        } catch (Exception e) {
//
//        }
//        return null;
//    }

    private void setHeatMapListener(Button submit, TextField originField, BooleanProperty isOn, Text label) {
        submit.setOnAction(_ -> {
            if (isOn.get()) {
               Platform.runLater(() -> label.setText("Creating Heatmap..."));
                double[] coordinates = getCoordinatesFromAddress(originField.getText());
                if (coordinates == null) {
                    Platform.runLater(() -> label.setText("Invalid address. Please try again."));
                    return;
                }
                double lat = coordinates[0];
                double lon = coordinates[1];
                addMarkerOnClicks(lat, lon, true);

                Task<Void> routerTask = createRouterTask(lat, lon);
                routerTask.setOnSucceeded(_ -> Platform.runLater(() -> label.setText("Heatmap Created")));
                routerTask.setOnFailed(_ -> Platform.runLater(() -> label.setText("Heatmap Mode Activated. \n Enter an origin point")));
                new Thread(routerTask).start();
            }
        });
    }

    public Task<Void> createRouterTask(double lat, double lon) {
        return new Task<>() {
            @Override
            protected Void call() {
                HeatMapRouter router = new HeatMapRouter(0);
                List<HeatPoint> heatPoints = router.build(lat, lon, "9:30:00");
                JXMapViewer baseMap = MapProvider.getInstance().getMap();
                HeatMapPainter heatMapPainter = new HeatMapPainter(heatPoints);
                @SuppressWarnings("unchecked")
                CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>(heatMapPainter, waypointPainter);
                baseMap.setOverlayPainter(compoundPainter);
                return null;
            }
        };
    }

    public static void main(String[] args) {
        String debug = System.getenv("debug");
        boolean isDebugMode = getDebugMode();
        if (debug != null) {
            System.setProperty("debug", debug);
        } else {
            if (isDebugMode)
                System.err.println("Environment variable 'debug' is not set. Debug mode is enabled by default.");
        }
        launch(args);
    }
}


