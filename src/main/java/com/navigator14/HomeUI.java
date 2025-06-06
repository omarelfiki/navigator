package com.navigator14;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import javafx.stage.Stage;
import java.awt.geom.Point2D;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import map.*;
import router.AStarRouterV;
import ui.*;
import util.NetworkUtil;
import router.Node;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import map.WayPoint;
import db.*;

import static ui.TripIntel.displayTransportModes;
import static util.DebugUtil.getDebugMode;
import static util.NavUtil.parsePoint;
import static ui.UiHelper.*;
import static util.GeoUtil.*;

public class HomeUI extends Application {
    private final BooleanProperty isOn = new SimpleBooleanProperty(false);
    private boolean firstClick = true;
    private final Set<Waypoint> waypoints = new HashSet<>();
    private final WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
    private AtomicReference<StackPane> resultPaneRef;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Navigator");
        DBAccess access = DBAccessProvider.getInstance();
        BorderPane root = new BorderPane();

        Pane leftPane = new Pane();
        VBox vbox_left = new VBox(leftPane);
        root.setLeft(vbox_left);

        Rectangle leftBar = getLeftBar(root);
        leftPane.getChildren().add(leftBar);

        Text title = getNewLabel("Navigator", root, Color.WHITE, 28, 0.015, 0.06, null);
        leftPane.getChildren().add(title);

        StackPane startGroup = createTextFieldWithIcon("↗", "Origin","originField");
        bindPosition(startGroup, root, 0.09);
        leftPane.getChildren().add(startGroup);

        StackPane endGroup = createTextFieldWithIcon("↙", "Destination", "destinationField");
        bindPosition(endGroup, root, 0.18);
        leftPane.getChildren().add(endGroup);

        StackPane timeContainer = createDateTimeContainer("⏰", "Time", 0.1, 0.12, root, 1);
        StackPane goButtonContainer = createButtonContainer(root, 0.06, 0.061);
        HBox combinedContainer = getCombinedContainer(root, 0.2, 0.061, 0.05, 0.28, timeContainer, goButtonContainer);
        leftPane.getChildren().add(combinedContainer);

        TextField originField = (TextField) ((HBox) startGroup.getChildren().getFirst()).getChildren().get(1);
        TextField destinationField = (TextField) ((HBox) endGroup.getChildren().getFirst()).getChildren().get(1);
        TextField timeField = (TextField) ((HBox) timeContainer.getChildren().getFirst()).getChildren().get(1);
        Button goButton = (Button) ((HBox) goButtonContainer.getChildren().getFirst()).getChildren().getFirst();

        Button flipButton = createFlipButton(root, originField, destinationField, timeField);
        leftPane.getChildren().add(flipButton);

        flipButton.visibleProperty().bind(isOn.not());
        endGroup.visibleProperty().bind(isOn.not());

        MapIntegration mapIntegration = MapProvider.getInstance();
        StackPane mapPane = mapIntegration.createMapPane();
        JXMapViewer map = mapIntegration.getMap();
        addMapListener(map, originField, destinationField);
        root.setCenter(mapPane);

        BooleanBinding filled = Bindings.createBooleanBinding(
                () -> !originField.getText().isEmpty() &&
                        !destinationField.getText().isEmpty() &&
                        !timeField.getText().isEmpty(),
                originField.textProperty(),
                destinationField.textProperty(),
                timeField.textProperty()
        );

        String labelText = "Navigate to see public transport \n options";
        Text label = getNewLabel(labelText, root, Color.WHITE, 15, 0.061, 0.48, TextAlignment.CENTER);
        leftPane.getChildren().add(label);

        resultPaneRef = new AtomicReference<>(new StackPane());
        leftPane.getChildren().add(resultPaneRef.get());

        goButton.setOnAction(_ -> {
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

        leftPane.getChildren().add(getLine(root));

        createSidePanelButtons(root, leftPane, mapPane);

        Pane togglePane = createToggleSwitch(root, isOn);
        leftPane.getChildren().add(togglePane);

        Text toggleText = getNewLabel("Heatmap Mode", root, Color.WHITE, 10, 0.07, 0.959, null);
        leftPane.getChildren().add(toggleText);

        leftPane.getChildren().add(getSettingsButton(root, leftPane, vbox_left));

        Button searchButton = createButton(root, "Search", 0.02);
        setHeatMapListener(searchButton, originField, isOn, label);
        leftPane.getChildren().add(searchButton);

        Button clearButton = createButton(root, "Clear", 0.1);
        setClearAction(clearButton, label, destinationField, timeField, originField);
        leftPane.getChildren().add(clearButton);

        bindElements(timeContainer, goButtonContainer, clearButton, originField, destinationField);

        isOn.addListener((_, _, _) -> {
            if (isOn.get()) {
                title.setText("Heatmap");
                label.setText("Heatmap Mode Activated. \n Enter an origin point...");
                originField.clear();
                if (!searchButton.isVisible()) {
                    searchButton.setVisible(true);
                }
                waypoints.clear();
                combinedContainer.setVisible(false);
                endGroup.setVisible(false);
                destinationField.setEditable(false);
            } else {
                title.setText("Navigator");
                label.setText("Navigate to see public transport \n options");
                originField.clear();
                destinationField.clear();
                if (searchButton.isVisible()) {
                    searchButton.setVisible(false);
                }
                map.setOverlayPainter(waypointPainter);
                combinedContainer.setVisible(true);
                endGroup.setVisible(true);
                destinationField.setEditable(true);
                waypoints.clear();
            }
        });

        Scene scene = new Scene(root, 1280, 832);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        if (access == null) {
            ErrorPopup.showError("SQL Error", "Database connection failed. Please check your configuration.");
        }

    }

    // this method to update the fields
    private void updateCoordinateFields(double lat, double lon, TextField originField, TextField destinationField) {
        // Format the coordinates
        String coordinateText = String.format("%.6f, %.6f", lat, lon);
        if (firstClick && originField.getText().isEmpty()) {
            addMarkerOnClicks(lat, lon, true, waypoints, waypointPainter);
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
            addMarkerOnClicks(lat, lon, false, waypoints, waypointPainter);
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
                addMarkerOnClicks(lat, lon, true, waypoints, waypointPainter);

                Task<Void> routerTask = createRouterTask(lat, lon, waypointPainter);
                routerTask.setOnSucceeded(_ -> Platform.runLater(() -> label.setText("Heatmap Created")));
                routerTask.setOnFailed(_ -> Platform.runLater(() -> label.setText("Heatmap Mode Activated. \n Enter an origin point")));
                new Thread(routerTask).start();
            }
        });
    }

    public void addMapListener(JXMapViewer map, TextField originField, TextField destinationField) {
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
    }
    private void setClearAction(Button clearButton, Text label, TextField destinationField, TextField timeField, TextField originField) {
        clearButton.setOnAction(_ -> {
            if (isOn.get()) {
                label.setText("Heatmap Mode Activated. \n Enter an origin point");
            } else {
                destinationField.clear();
                timeField.clear();
                label.setText("Navigate to see public transport \n options");
                label.setVisible(true);
                resultPaneRef.get().setVisible(false);

            }
            originField.clear();
            WayPoint.clearRoute();
            AStarRouterV router = new AStarRouterV();
            router.reset();
            waypoints.clear();
        });
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

