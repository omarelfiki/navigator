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
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import java.awt.geom.Point2D;

import models.HeatPoint;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
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
    private final WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Navigator");
        DBAccess access = DBAccessProvider.getInstance();
        BorderPane root = new BorderPane();

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
        root.setCenter(mapPane);

        BooleanBinding filled = Bindings.createBooleanBinding(
                () -> !originField.getText().isEmpty() &&
                        !destinationField.getText().isEmpty() &&
                        !timeField.getText().isEmpty(),
                originField.textProperty(),
                destinationField.textProperty(),
                timeField.textProperty()
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

        Line line = getLine(root);
        leftPane.getChildren().add(line);

        createSidePanelButtons(root, leftPane, mapPane);

        Pane togglePane = createToggleSwitch(root, isOn);
        leftPane.getChildren().add(togglePane);

        Text toggleText = new Text("Heatmap Mode");
        toggleText.setFill(Color.WHITE);
        toggleText.setStyle("-fx-font: 10 Ubuntu;");
        toggleText.xProperty().bind(root.widthProperty().multiply(0.07)); // 90/1280
        toggleText.yProperty().bind(root.heightProperty().multiply(0.935).add(20)); // 778/832 + 20
        leftPane.getChildren().add(toggleText);

        Button settings = getSettingsButton(root, leftPane, vbox_left);
        leftPane.getChildren().add(settings);

        Button searchButton = new Button("Search");
        searchButton.prefWidthProperty().bind(root.widthProperty().multiply(0.07));
        searchButton.prefHeightProperty().bind(root.heightProperty().multiply(0.04));
        searchButton.layoutXProperty().bind(root.widthProperty().multiply(0.02));
        searchButton.layoutYProperty().bind(root.heightProperty().multiply(0.85));
        searchButton.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        setHeatMapListener(searchButton, originField, isOn, label);
        searchButton.setVisible(false);
        searchButton.setId("searchButton");
        leftPane.getChildren().add(searchButton);

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
        leftPane.getChildren().add(clearButton);
        clearButton.setVisible(false);

        bindElements(timeContainer, goButtonContainer, clearButton, originField, destinationField);

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
                addMarkerOnClicks(lat, lon, true, waypoints, waypointPainter);

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

    private void createSidePanelButtons(BorderPane root, Pane leftPane, StackPane mapPane) {
        // Button to hide side panel
        hideSidePanel = new Button("←");
        hideSidePanel.layoutXProperty().bind(root.widthProperty().multiply(0.245));
        hideSidePanel.layoutYProperty().bind(root.heightProperty().multiply(0.033));
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


