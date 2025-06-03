package ui;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import map.WayPoint;
import router.AStarRouterV;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UiHelper {
    public static void toggleLeftBar(Pane leftPane) {
        boolean isVisible = leftPane.isVisible();
        leftPane.setVisible(!isVisible);
        leftPane.setManaged(!isVisible);
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

    public static StackPane createDateTimeContainer(String iconText, String promptText, double layoutXMultiplier, double widthMultiplier, double heightMultiplier, Pane root, int type) {
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
                break;
        }
        container.getChildren().add(inner);
        container.layoutXProperty().bind(root.widthProperty().multiply(layoutXMultiplier));
        container.layoutYProperty().bind(root.heightProperty().multiply(0.276));
        container.prefWidthProperty().bind(root.widthProperty().multiply(widthMultiplier));
        container.prefHeightProperty().bind(root.heightProperty().multiply(heightMultiplier));
        container.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: gray;");
        return container;
    }

    public static StackPane createTextFieldWithIcon(String icon, String prompt) {
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

    public static void bindElements(StackPane timeContainer, StackPane dateContainer, Button clear, TextField originField, TextField destinationField) {
        timeContainer.visibleProperty().bind(
                Bindings.createBooleanBinding(
                        () -> !originField.getText().isEmpty() && !destinationField.getText().isEmpty(),
                        originField.textProperty(),
                        destinationField.textProperty()
                )
        );

        dateContainer.visibleProperty().bind(
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

    public static Button createFlipButton(BorderPane root, TextField originField, TextField destinationField, DatePicker dateField, TextField timeField) {
        Button flipButton = new Button("⇅");
        flipButton.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        flipButton.layoutXProperty().bind(root.widthProperty().multiply(0.2)); // 130/1280
        flipButton.layoutYProperty().bind(root.heightProperty().multiply(0.1555)); // 120/832
        flipButton.setOnAction(_ -> {
            String temp = originField.getText();
            originField.setText(destinationField.getText());
            destinationField.setText(temp);
            dateField.setValue(null);
            timeField.clear();
            WayPoint.clearRoute();
            AStarRouterV router = new AStarRouterV();
            router.reset();
        });
        return flipButton;
    }
}
