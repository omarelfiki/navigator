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
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import static ui.UiHelper.*;

import java.nio.file.Files;
import java.nio.file.Path;


import db.*;
import javafx.stage.FileChooser;

public class SettingsUI {
    private final BorderPane root;
    private final Pane leftPane;

    public SettingsUI(BorderPane root, Pane leftPane) {
        this.root = root;
        this.leftPane = leftPane;
    }

    public Pane createSettingsMenu() {
        Pane settingsMenu = new Pane();

        settingsMenu.getChildren().add(getLeftBar(root));
        settingsMenu.getChildren().addAll(
                getText("Settings", 0.015, 0.06, 28),
                getText("GTFS .zip Path", 0.017, 0.12, 15),
                getText("MySQL Connection Details", 0.017, 0.23, 20),
                getText("MySQL Host", 0.017, 0.276, 15),
                getText("User", 0.017, 0.385, 15),
                getText("Password", 0.017, 0.48, 15),
                getText("Port", 0.017, 0.6, 15)
        );

        TextField gtfsField = getTextField("Path to GTFS .zip file", 0.144, 0.22);
        TextField hostField = getTextField("Host", 0.3, 0.24);
        TextField userField = getTextField("User", 0.41, 0.24);
        PasswordField passwordField = getPasswordField();
        TextField portField = getTextField("Port", 0.625, 0.24);

        settingsMenu.getChildren().addAll(
                gtfsField,
                hostField,
                userField,
                passwordField,
                portField
        );

        Label testLabel = getLabel("", 0.95, 15);

        String info = "Set connection details as environment variables under \n \"ROUTING_ENGINE_MYSQL_JDBC\".";
        Label infoLabel = getLabel(info, 0.72, 12);
        infoLabel.setAlignment(Pos.CENTER);
        infoLabel.setTextAlignment(TextAlignment.CENTER);

        settingsMenu.getChildren().addAll(testLabel, infoLabel);

        Button fileSelectorButton = new Button("...");
        fileSelectorButton.layoutXProperty().bind(root.widthProperty().multiply(0.24)); // 330/1280
        fileSelectorButton.layoutYProperty().bind(root.heightProperty().multiply(0.144)); // 120/832
        fileSelectorButton.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        fileSelectorButton.setOnAction(_ -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select GTFS Zip File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Files", "*.zip"));
            java.io.File selectedFile = fileChooser.showOpenDialog(settingsMenu.getScene().getWindow());
            if (selectedFile != null) {
                gtfsField.setText(selectedFile.getAbsolutePath());
            }
        });

        Button importButton = getButton("Import GTFS Data", 0.85);
        handleImportAction(importButton, gtfsField, testLabel);


        Button testButton = getButton("Test Connection", 0.8);
        testButton.setOnAction(_ -> {
            testLabel.setText("");
            if (userField.getText().isEmpty() || passwordField.getText().isEmpty() || hostField.getText().isEmpty() || portField.getText().isEmpty()) {
                testLabel.setText("Please check configuration.");
            } else {
                DBAccess access = DBAccessProvider.getInstance();
                assert access != null;
                if (access.conn != null) {
                    testLabel.setText("Connection Established");
                    importButton.setDisable(false);

                } else {
                    testLabel.setText("Connection Failed");
                }
            }
        });
        settingsMenu.getChildren().addAll(fileSelectorButton, importButton, testButton);

        settingsMenu.getChildren().add(getLine(root));

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

    private PasswordField getPasswordField() {
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.layoutXProperty().bind(root.widthProperty().multiply(0.017));
        passwordField.layoutYProperty().bind(root.heightProperty().multiply(0.5));
        passwordField.prefWidthProperty().bind(root.widthProperty().multiply(0.24));
        passwordField.prefHeightProperty().bind(root.heightProperty().multiply(0.035));
        passwordField.setEditable(false);
        return passwordField;
    }

    private TextField getTextField(String prompt, double y, double width) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.layoutXProperty().bind(root.widthProperty().multiply(0.017));
        textField.layoutYProperty().bind(root.heightProperty().multiply(y));
        textField.prefWidthProperty().bind(root.widthProperty().multiply(width));
        textField.prefHeightProperty().bind(root.heightProperty().multiply(0.035));
        textField.setEditable(false);
        return textField;
    }

    private Text getText(String text, double x, double y, double fontSize) {
        Text t = new Text(text);
        t.setTextAlignment(TextAlignment.CENTER);
        t.setFill(Color.WHITE);
        t.setStyle("-fx-font: " + fontSize + " Ubuntu;");
        t.xProperty().bind(root.widthProperty().multiply(x));
        t.yProperty().bind(root.heightProperty().multiply(y));
        return t;
    }

    private Label getLabel(String text, double y, double fontSize) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setStyle("-fx-font: " + fontSize + " Ubuntu;");
        bindPosition(label, root, y);
        return label;
    }

    private Button getButton(String text, double y) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        bindPosition(button, root, y);
        return button;
    }

    private void handleImportAction(Button importButton, TextField gtfsField, Label testLabel) {
        importButton.setDisable(true);
        importButton.setOnAction(_ -> {
            if (gtfsField.getText().isEmpty()) {
                testLabel.setText("Please check GTFS Path.");
            } else if (!Files.exists(Path.of(gtfsField.getText()))) {
                testLabel.setText("File does not exist.");
            } else if (!Files.isReadable(Path.of(gtfsField.getText()))) {
                testLabel.setText("File is not readable.");
            } else {
                ConsolePopup consolePopup = new ConsolePopup();
                consolePopup.show();
                new Thread(() -> {
                    DBConfig config = new DBConfig(gtfsField.getText());
                    config.initializeDB();
                    Platform.runLater(() -> {
                        consolePopup.close();
                        testLabel.setText("GTFS data loaded successfully.");
                    });
                }).start();

            }
        });
    }
}
