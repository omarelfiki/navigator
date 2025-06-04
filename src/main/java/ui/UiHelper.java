package ui;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import map.HeatMapPainter;
import map.MapIntegration;
import map.MapProvider;
import map.WayPoint;
import models.HeatPoint;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import router.AStarRouterV;
import router.HeatMapRouter;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UiHelper {
    public static void toggleLeftBar(Pane leftPane) {
        boolean isVisible = leftPane.isVisible();
        leftPane.setVisible(!isVisible);
        leftPane.setManaged(!isVisible);
    }

    public static void createSidePanelButtons(BorderPane root, Pane leftPane, StackPane mapPane) {
        // Button to hide side panel
        Button hideSidePanel = new Button("←");
        Button showSidePanel = new Button("→");

        hideSidePanel.layoutXProperty().bind(root.widthProperty().multiply(0.245));
        hideSidePanel.layoutYProperty().bind(root.heightProperty().multiply(0.033));
        hideSidePanel.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        hideSidePanel.setOnAction(_ -> {
            toggleLeftBar(leftPane);
            hideSidePanel.setVisible(false);
            showSidePanel.setVisible(true);
        });
        leftPane.getChildren().add(hideSidePanel);


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

    public static void bindPosition(Region node, Pane root, double yRatio) {
        node.layoutXProperty().bind(root.widthProperty().multiply(0.017));
        node.layoutYProperty().bind(root.heightProperty().multiply(yRatio));
        node.prefWidthProperty().bind(root.widthProperty().multiply(0.24));
        node.prefHeightProperty().bind(root.heightProperty().multiply(0.035));
    }

    public static void showSettingsMenu(BorderPane root, Pane leftPane, VBox vbox_left) {
        SettingsUI settingsUI = new SettingsUI(root, leftPane);
        Pane settingsPane = settingsUI.createSettingsMenu();
        vbox_left.getChildren().add(settingsPane);
    }

    public static StackPane createDateTimeContainer(String iconText, String promptText, double widthMultiplier, double heightMultiplier, Pane root, int type) {
        StackPane container = new StackPane();
        container.getStyleClass().add("input-container-small");
        HBox inner = new HBox(5);
        inner.setStyle("-fx-padding: 5;");
        inner.setAlignment(Pos.CENTER);
        switch (type) {
            case 0:
                DatePicker dateField = new DatePicker();
                dateField.setPromptText(promptText);
                dateField.getStyleClass().add("small-date-picker");
                HBox.setHgrow(dateField, Priority.ALWAYS);
                inner.getChildren().add(dateField);
                break;
            case 1:
                StackPane iconCircle = new StackPane();
                iconCircle.getStyleClass().add("circle-icon-small");
                Label iconLabel = new Label(iconText);
                iconLabel.getStyleClass().add("icon-label");
                iconCircle.getChildren().add(iconLabel);
                TextField timeField = new TextField();
                timeField.setPromptText(promptText);
                timeField.getStyleClass().add("rounded-textfield");
                HBox.setHgrow(timeField, Priority.ALWAYS);
                inner.getChildren().addAll(iconCircle, timeField);
                timeField.setId("timeField");
                break;
        }
        container.getChildren().add(inner);
        container.prefWidthProperty().bind(root.widthProperty().multiply(widthMultiplier));
        container.prefHeightProperty().bind(root.heightProperty().multiply(heightMultiplier));
        container.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: gray;");
        return container;
    }

    public static StackPane createTextFieldWithIcon(String icon, String prompt, String id) {
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
        container.setId(id);
        return container;
    }

    public static void bindElements(StackPane timeContainer, StackPane ButtonContainer, Button clear, TextField originField, TextField destinationField) {
        timeContainer.visibleProperty().bind(
                Bindings.createBooleanBinding(
                        () -> !originField.getText().isEmpty() && !destinationField.getText().isEmpty(),
                        originField.textProperty(),
                        destinationField.textProperty()
                )
        );

        ButtonContainer.visibleProperty().bind(
                Bindings.createBooleanBinding(
                        () -> !originField.getText().isEmpty() && !destinationField.getText().isEmpty(),
                        originField.textProperty(),
                        destinationField.textProperty()
                )
        );

        clear.visibleProperty().bind(
                Bindings.createBooleanBinding(
                        () -> !originField.getText().isEmpty() || !destinationField.getText().isEmpty(),
                        originField.textProperty(),
                        destinationField.textProperty()
                )
        );
    }

    public static Rectangle getLeftBar(BorderPane root) {
        Rectangle leftBar = new Rectangle();
        leftBar.widthProperty().bind(root.widthProperty().multiply(0.273)); // 350/1280
        leftBar.heightProperty().bind(root.heightProperty()); // Full height
        leftBar.setX(0);
        leftBar.setY(0);
        leftBar.setFill(Color.web("#5D5D5D"));
        leftBar.setOpacity(0.95);
        return leftBar;
    }

    public static void parseConnectionString(String connectionString, TextField usernameField, TextField passwordField, TextField hostField, TextField portField) {
        // Example connection string: jdbc:mysql://USER:PASSWORD@HOST:PORT/DBNAME
        String regex = "^jdbc:mysql://([^:]+):([^@]+)@([^:/]+):?(\\d+)?/([^?]+)(\\?.*)?$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(connectionString);

        if (matcher.matches()) {
            String user = matcher.group(1);
            String password = matcher.group(2);
            String host = matcher.group(3);
            String port = matcher.group(4) != null ? matcher.group(4) : "3306"; // Default MySQL port
            usernameField.setText(user);
            passwordField.setText(password);
            hostField.setText(host);
            portField.setText(port);
        }
    }

    public static Line getLine(BorderPane root) {
        Line line = new Line();
        line.startXProperty().bind(root.widthProperty().multiply(0)); // 0/1280
        line.startYProperty().bind(root.heightProperty().multiply(0.89).add(20)); // 740/832 + 20
        line.endXProperty().bind(root.widthProperty().multiply(0.273)); // 350/1280
        line.endYProperty().bind(root.heightProperty().multiply(0.89).add(20)); // 740/832 + 20
        line.setStroke(Color.WHITE);
        return line;
    }

    public static Button createFlipButton(BorderPane root, TextField originField, TextField destinationField, TextField timeField) {
        Button flipButton = new Button("⇅");
        flipButton.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        flipButton.layoutXProperty().bind(root.widthProperty().multiply(0.2)); // 130/1280
        flipButton.layoutYProperty().bind(root.heightProperty().multiply(0.1555)); // 120/832
        flipButton.setOnAction(_ -> {
            String temp = originField.getText();
            originField.setText(destinationField.getText());
            destinationField.setText(temp);
            timeField.clear();
            WayPoint.clearRoute();
            AStarRouterV router = new AStarRouterV();
            router.reset();
        });
        return flipButton;
    }

    public static StackPane createButtonContainer(BorderPane root, double w, double h) {
        Image image = new Image(Objects.requireNonNull(UiHelper.class.getResourceAsStream("/searchIcon.png")));
        ImageView imageView = new ImageView(image);
        imageView.fitWidthProperty().bind(root.widthProperty().multiply(0.02)); // 25/1280
        imageView.fitHeightProperty().bind(root.heightProperty().multiply(0.03)); // 25/832
        Button goButton = new Button("", imageView);
        goButton.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        StackPane goButtonContainer = new StackPane();
        goButtonContainer.setStyle("-fx-background-color: #d5d5d5; -fx-text-fill: #ffffff;");
        goButtonContainer.getStyleClass().add("input-container-small");
        HBox inner = new HBox(5);
        inner.setStyle("-fx-padding: 5;");
        inner.setAlignment(Pos.CENTER);
        goButton.getStyleClass().add("rounded-textfield");
        HBox.setHgrow(goButton, Priority.ALWAYS);
        inner.getChildren().add(goButton);
        goButtonContainer.getChildren().add(inner);
        goButtonContainer.prefWidthProperty().bind(root.widthProperty().multiply(w));
        goButtonContainer.prefHeightProperty().bind(root.heightProperty().multiply(h));
        goButton.setId("goButton");
        return goButtonContainer;
    }

    public static Button getSettingsButton(BorderPane root, Pane leftPane, VBox vbox_left) {
        Image image = new Image(Objects.requireNonNull(UiHelper.class.getResourceAsStream("/settingsIcon.png")));
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
        return settings;
    }

    public static HBox getCombinedContainer(BorderPane root, double w, double h, double x, double y, StackPane... stackPanes) {
        HBox combinedContainer = new HBox(10);
        combinedContainer.prefWidthProperty().bind(root.widthProperty().multiply(w)); // 330/1280
        combinedContainer.prefHeightProperty().bind(root.heightProperty().multiply(h)); // 50/832
        combinedContainer.layoutXProperty().bind(root.widthProperty().multiply(x));
        combinedContainer.layoutYProperty().bind(root.heightProperty().multiply(y)); // 100/832
        combinedContainer.getChildren().addAll(stackPanes);
        return combinedContainer;
    }

    public static Pane createToggleSwitch(BorderPane root, BooleanProperty isOn) {
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
        togglePane.setOnMouseClicked(_ -> {
            isOn.set(!isOn.get());
            background.setFill(isOn.get() ? Color.LIMEGREEN : Color.LIGHTGRAY);
        });
        togglePane.setId("togglePane");

        return togglePane;
    }

    public static void addMarkerOnClicks(double lat, double lon, boolean isOrigin, Set<Waypoint> waypoints, WaypointPainter<Waypoint> waypointPainter) {
        MapIntegration mapIntegration = MapProvider.getInstance();
        JXMapViewer map = mapIntegration.getMap();
        GeoPosition geoPosition = new GeoPosition(lat, lon);
        if (isOrigin) {
            waypoints.clear();
            WayPoint.clearRoute();
            DefaultWaypoint originWaypoint = new DefaultWaypoint(geoPosition);
            waypoints.add(originWaypoint);
        } else {
            DefaultWaypoint destinationWaypoint = new DefaultWaypoint(geoPosition);
            waypoints.add(destinationWaypoint);
        }
        waypointPainter.setWaypoints(waypoints);
        map.setOverlayPainter(waypointPainter);
    }

    public static Task<Void> createRouterTask(double lat, double lon, WaypointPainter<Waypoint> waypointPainter) {
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

    public static Text getNewLabel(String text, BorderPane root, Color fill, int fontSize, double x, double y, TextAlignment alignment) {
        Text label = new Text(text);
        label.setFill(fill);
        label.setTextAlignment(alignment);
        label.setStyle("-fx-font-family: Ubuntu; -fx-font-size: " + fontSize + "px;");
        label.xProperty().bind(root.widthProperty().multiply(x));
        label.yProperty().bind(root.heightProperty().multiply(y));
        return label;
    }

    public static Button createButton(BorderPane root, String label, double xFactor) {
        Button button = new Button(label);
        button.prefWidthProperty().bind(root.widthProperty().multiply(0.07));
        button.prefHeightProperty().bind(root.heightProperty().multiply(0.04));
        button.layoutXProperty().bind(root.widthProperty().multiply(xFactor));
        button.layoutYProperty().bind(root.heightProperty().multiply(0.85));
        button.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        button.setVisible(false);
        button.setId("label");
        return button;
    }
}
