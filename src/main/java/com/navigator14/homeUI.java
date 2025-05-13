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
import map.*;
import db.*;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import ui.*;
import util.Node;

import java.awt.geom.Point2D;
import java.util.*;

import static util.DebugUtli.getDebugMode;
import static util.NavUtil.parsePoint;
import static ui.UiHelper.*;
import static util.WeatherUtil.createWeatherTask;

public class homeUI extends Application {
    private final BooleanProperty isOn = new SimpleBooleanProperty(false);
    private Button hideSidePanel, showSidePanel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Navigator");
        DBaccess access = DBaccessProvider.getInstance();

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
        bindDateTime(timeContainer, dateContainer, originField, destinationField);
        leftPane.getChildren().addAll(timeContainer, dateContainer);

        TextField timeField = (TextField) ((HBox) timeContainer.getChildren().getFirst()).getChildren().get(1);
        DatePicker dateField = (DatePicker) ((HBox) dateContainer.getChildren().getFirst()).getChildren().getFirst();

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

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater(() -> {
                    if (filled.get()) {
                        String origin = originField.getText();
                        String destination = destinationField.getText();
                        String time = timeField.getText();
                        Date date = java.sql.Date.valueOf(dateField.getValue());
                        label.setText("Finding routes...");

                        new Thread(() -> {
                            List<Node> result = parsePoint(origin, destination, time, date);
                            if (result == null) {
                                Platform.runLater(() -> label.setText("No route found."));
                            } else {
                                Platform.runLater(() -> {
//                                    label.setVisible(false);
//                                    StackPane resultPane = new StackPane();
//                                    resultPane.layoutXProperty().bind(root.widthProperty().multiply(0.026));
//                                    resultPane.layoutYProperty().bind(root.heightProperty().multiply(0.4));
//                                    resultPane.setMaxWidth(300);
//                                    resultPane.setMaxHeight(700);
//                                    displayResult(result, resultPane);
//                                    leftPane.getChildren().add(resultPane);
                                    label.setText("Found " + result.size() + " stops along route.");
                                });
                            }
                        }).start();
                    }
                });
                return null;
            }
        };

        isOn.addListener((_, _, _) -> {
            if (isOn.get()) {
                title.setText("Heatmap");
                label.setText("Heatmap Mode Activated. \n Finding routes...");
            } else {
                title.setText("Navigator");
                label.setText("Navigate to see public transport \n options");
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

        dateField.valueProperty().addListener((_, _, _) -> new Thread(task).start()); // start task when date changes

        Text temperatureLabel = new Text("0°C");
        temperatureLabel.setFill(Color.WHITE);
        temperatureLabel.setStyle("-fx-font: 20 Ubuntu; -fx-font-weight: normal;");
        temperatureLabel.xProperty().bind(root.widthProperty().multiply(0.215)); // Positioning
        temperatureLabel.yProperty().bind(root.heightProperty().multiply(0.06)); // Positioning
        leftPane.getChildren().add(temperatureLabel);

        ImageView weatherIcon = new ImageView();
        weatherIcon.setFitWidth(40); // Set icon size
        weatherIcon.setFitHeight(40);
        weatherIcon.xProperty().bind(root.widthProperty().multiply(0.18)); // Positioning
        weatherIcon.yProperty().bind(root.heightProperty().multiply(0.025)); // Positioning
        leftPane.getChildren().add(weatherIcon);

        Task<Void> weatherTask = createWeatherTask(41.9028, 12.4964, temperatureLabel, weatherIcon);
        new Thread(weatherTask).start();

        JXMapViewer map = mapIntegration.getMap();

        map.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Point2D point = e.getPoint();
                GeoPosition geoPosition = map.convertPointToGeoPosition(point);
                double lat = geoPosition.getLatitude();
                double lon = geoPosition.getLongitude();

                Task<Void> weatherTask = createWeatherTask(lat, lon, temperatureLabel, weatherIcon);
                new Thread(weatherTask).start();
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
        if (isOn.get()) {
            background.setFill(Color.LIGHTGRAY); // Off state
        } else {
            background.setFill(Color.LIMEGREEN); // On state
        }
        isOn.set(!isOn.get()); // Toggle the state
    }

    public void displayResult(List<Node> result, StackPane pane) {
        for (Node node : result) {
            Text text = new Text(node.stop.stopName);
            text.setFill(Color.WHITE);
            text.setStyle("-fx-font: 14 Ubuntu;");
            pane.getChildren().add(text);
        }
    }

    public static void main(String[] args) {
        String gtfsDir = System.getenv("GTFS_DIR");
        System.setProperty("debug", System.getenv("debug"));
        boolean isDebugMode = getDebugMode();
        if (gtfsDir != null) {
            System.setProperty("GTFS_DIR", gtfsDir);
        } else {
            if (isDebugMode) System.err.println("Environment variable 'GTFS_DIR' is not set. Set the path manually in the settings panel.");
        }
        launch(args);
    }
}