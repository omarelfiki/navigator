package ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import static ui.UiHelper.*;
import java.nio.file.Files;
import java.nio.file.Path;


import db.*;

public class settingsUI {
    private final BorderPane root;
    private final Pane leftPane;

    public settingsUI(BorderPane root, Pane leftPane) {
        this.root = root;
        this.leftPane = leftPane;
    }

    public Pane createSettingsMenu() {
        Pane settingsMenu = new Pane();

        Rectangle settings = getLeftBar(root);
        settingsMenu.getChildren().add(settings);

        Text title = new Text("Settings");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font: 28 Ubuntu;");
        title.xProperty().bind(root.widthProperty().multiply(0.015)); // 20/1280
        title.yProperty().bind(root.heightProperty().multiply(0.06)); // 50/832
        settingsMenu.getChildren().add(title);

        Text label = new Text("GTFS .zip Path");
        label.setTextAlignment(TextAlignment.CENTER);
        label.setFill(Color.WHITE);
        label.setStyle("-fx-font: 15 Ubuntu;");
        label.xProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        label.yProperty().bind(root.heightProperty().multiply(0.12)); // 100/832
        settingsMenu.getChildren().add(label);

        TextField textField = new TextField();
        textField.setPromptText("Path to GTFS zip file");
        textField.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        textField.layoutYProperty().bind(root.heightProperty().multiply(0.144)); // 120/832
        textField.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        textField.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        settingsMenu.getChildren().add(textField);
        textField.setText(System.getProperty("GTFS_DIR"));

        Text label2 = new Text("MySQL Connection Details");
        label2.setTextAlignment(TextAlignment.CENTER);
        label2.setFill(Color.WHITE);
        label2.setStyle("-fx-font: 20 Ubuntu;");
        label2.xProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        label2.yProperty().bind(root.heightProperty().multiply(0.23)); // 200/832
        settingsMenu.getChildren().add(label2);

        Text host = new Text("Host");
        host.setTextAlignment(TextAlignment.CENTER);
        host.setFill(Color.WHITE);
        host.setStyle("-fx-font: 15 Ubuntu;");
        host.xProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        host.yProperty().bind(root.heightProperty().multiply(0.276)); // 230/832
        settingsMenu.getChildren().add(host);

        TextField hostField = new TextField();
        hostField.setPromptText("Host");
        hostField.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        hostField.layoutYProperty().bind(root.heightProperty().multiply(0.3)); // 250/832
        hostField.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        hostField.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        settingsMenu.getChildren().add(hostField);
        hostField.setEditable(false);

        Text user = new Text("User");
        user.setTextAlignment(TextAlignment.CENTER);
        user.setFill(Color.WHITE);
        user.setStyle("-fx-font: 15 Ubuntu;");
        user.xProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        user.yProperty().bind(root.heightProperty().multiply(0.385)); // 320/832
        settingsMenu.getChildren().add(user);

        TextField userField = new TextField();
        userField.setPromptText("User");
        userField.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        userField.layoutYProperty().bind(root.heightProperty().multiply(0.41)); // 340/832
        userField.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        userField.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        settingsMenu.getChildren().add(userField);
        userField.setEditable(false);

        Text password = new Text("Password");
        password.setTextAlignment(TextAlignment.CENTER);
        password.setFill(Color.WHITE);
        password.setStyle("-fx-font: 15 Ubuntu;");
        password.xProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        password.yProperty().bind(root.heightProperty().multiply(0.48)); // 400/832
        settingsMenu.getChildren().add(password);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        passwordField.layoutYProperty().bind(root.heightProperty().multiply(0.5)); // 420/832
        passwordField.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        passwordField.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        settingsMenu.getChildren().add(passwordField);
        passwordField.setEditable(false);

        Text port = new Text("Port");
        port.setTextAlignment(TextAlignment.CENTER);
        port.setFill(Color.WHITE);
        port.setStyle("-fx-font: 15 Ubuntu;");
        port.xProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        port.yProperty().bind(root.heightProperty().multiply(0.6)); // 500/832
        settingsMenu.getChildren().add(port);

        TextField portField = new TextField();
        portField.setPromptText("Port");
        portField.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        portField.layoutYProperty().bind(root.heightProperty().multiply(0.625)); // 520/832
        portField.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        portField.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        settingsMenu.getChildren().add(portField);
        portField.setEditable(false);

        Label testLabel = new Label();
        testLabel.setText("");
        testLabel.setTextFill(Color.WHITE);
        testLabel.setStyle("-fx-font: 15 Ubuntu;");
        testLabel.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        testLabel.layoutYProperty().bind(root.heightProperty().multiply(0.95)); // 700/832
        testLabel.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        testLabel.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        settingsMenu.getChildren().add(testLabel);

        Label infoLabel = new Label();
        infoLabel.setText("Set connection details as environment variables under \n \"ROUTING_ENGINE_MYSQL_JDBC\".");
        infoLabel.setTextFill(Color.WHITE);
        infoLabel.setStyle("-fx-font: 12 Ubuntu;");
        infoLabel.setAlignment(Pos.CENTER);
        infoLabel.setTextAlignment(TextAlignment.CENTER);
        infoLabel.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        infoLabel.layoutYProperty().bind(root.heightProperty().multiply(0.72)); // 700/832
        infoLabel.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        infoLabel.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        settingsMenu.getChildren().add(infoLabel);


        Button test = new Button("Test MySQL Connection");
        test.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        test.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        test.layoutYProperty().bind(root.heightProperty().multiply(0.8)); // 700/832
        test.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        test.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        settingsMenu.getChildren().add(test);

        Button importButton = new Button("Reimport GTFS");
        importButton.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        importButton.layoutXProperty().bind(root.widthProperty().multiply(0.017)); // 78/1280
        importButton.layoutYProperty().bind(root.heightProperty().multiply(0.85)); // 700/832
        importButton.prefWidthProperty().bind(root.widthProperty().multiply(0.24)); // 300/1280
        importButton.prefHeightProperty().bind(root.heightProperty().multiply(0.035)); // 30/832
        importButton.setOnAction(_ -> {
            if (textField.getText().isEmpty()) {
                testLabel.setText("Please check GTFS Path.");
            } else if (!Files.isDirectory(Path.of(textField.getText()))) {
                testLabel.setText("Not a directory.");
            } else if (!Files.exists(Path.of(textField.getText()))) {
                testLabel.setText("File does not exist.");
            } else if (!Files.isReadable(Path.of(textField.getText()))) {
                testLabel.setText("File is not readable.");
            } else {
                System.setProperty("GTFS_DIR", textField.getText());
                ConsolePopup consolePopup = new ConsolePopup();
                consolePopup.show();
                new Thread(() -> {
                    DBconfig config = new DBconfig(DBaccessProvider.getInstance());
                    config.initializeDB();
                    Platform.runLater(() -> {
                        consolePopup.close();
                        testLabel.setText("GTFS data loaded successfully.");
                    });
                }).start();

            }
        });
        importButton.setDisable(true);
        settingsMenu.getChildren().add(importButton);

        test.setOnAction(_ -> {
            testLabel.setText("");
            if (userField.getText().isEmpty() || passwordField.getText().isEmpty() || hostField.getText().isEmpty() || portField.getText().isEmpty()) {
                testLabel.setText("Please check configuration.");
            } else {
                DBaccess access = DBaccessProvider.getInstance();
                assert access != null;
                if (access.conn != null) {
                    testLabel.setText("Connection Established");
                    importButton.setDisable(false);

                } else {
                    testLabel.setText("Connection Failed");
                }
            }
        });

        Line line = getLine(root);
        settingsMenu.getChildren().add(line);

        Button close = new Button("X");
        close.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        close.layoutXProperty().bind(root.widthProperty().multiply(0.219)); // 280/1280
        close.layoutYProperty().bind(root.heightProperty().multiply(0.94)); // 50/832
        close.prefWidthProperty().bind(root.widthProperty().multiply(0.039)); // 50/1280
        close.prefHeightProperty().bind(root.heightProperty().multiply(0.036)); // 30/832
        close.setOnAction(_ -> {
            settingsMenu.setVisible(false);
            settingsMenu.setManaged(false);
            toggleLeftBar(leftPane);
        });
        settingsMenu.getChildren().add(close);

        String connectionString = System.getenv("ROUTING_ENGINE_MYSQL_JDBC");
        if (connectionString == null || connectionString.isEmpty()) {
            ErrorPopup.showError("SQL Error", "Database connection failed. Please check your configuration.");
        } else {
            parseConnectionString(connectionString, userField, passwordField, hostField, portField);
        }

        return settingsMenu;
    }
}
