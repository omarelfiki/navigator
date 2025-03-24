import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class homeUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Rome Navigator");

        StackPane stackPane = new StackPane();
        BorderPane root = new BorderPane();

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.load(getClass().getResource("map.html").toExternalForm());

        root.setCenter(webView);

        Pane leftPane = new Pane();
        VBox vbox_left = new VBox();
        vbox_left.getChildren().add(leftPane);
        root.setLeft(vbox_left);

        Rectangle leftBar = new Rectangle();
        leftBar.widthProperty().set(350);
        leftBar.heightProperty().set(720);
        leftBar.setX(33);
        leftBar.setY(73);
        leftBar.setArcHeight(35);
        leftBar.setArcWidth(35);
        leftBar.setFill(Color.web("#5D5D5D"));
        leftBar.setOpacity(0.95);
        leftPane.getChildren().add(leftBar);

        Text title = new Text("Navigator");
        title.setFill(Color.WHITE);
        title.setStyle("-fx-font: 25 Ubuntu;");
        title.setX(48);
        title.setY(105);
        leftPane.getChildren().add(title);

        TextField startPlace = new TextField();
        startPlace.setPromptText("Starting Point");
        startPlace.setLayoutX(55);
        startPlace.setLayoutY(134);
        startPlace.setPrefSize(307, 41.74);
        leftPane.getChildren().add(startPlace);

        TextField endPlace = new TextField();
        endPlace.setPromptText("Destination");
        endPlace.setLayoutX(55);
        endPlace.setLayoutY(192.55);
        endPlace.setPrefSize(307, 41.74);
        leftPane.getChildren().add(endPlace);

        Text label = new Text("Navigate to see public transport \n options");
        label.setTextAlignment(TextAlignment.CENTER);
        label.setFill(Color.WHITE);
        label.setStyle("-fx-font: 15 Ubuntu;");
        label.setX(110);
        label.setY(424);
        leftPane.getChildren().add(label);

        Line line = new Line();
        line.setStartX(33);
        line.setStartY(732);
        line.setEndX(383);
        line.setEndY(732);
        line.setStroke(Color.WHITE);
        leftPane.getChildren().add(line);

        ToggleButton toggleSwitch = new ToggleButton();
        toggleSwitch.setText("Off");
        toggleSwitch.setLayoutX(50);
        toggleSwitch.setLayoutY(746);
        toggleSwitch.setPrefSize(50, 30);
        toggleSwitch.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        toggleSwitch.setOnAction(_ -> {
            if (toggleSwitch.isSelected()) {
                toggleSwitch.setText("On");
                toggleSwitch.setStyle("-fx-background-color: green; -fx-text-fill: white;");
            } else {
                toggleSwitch.setText("Off");
                toggleSwitch.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
            }
        });
        leftPane.getChildren().add(toggleSwitch);

        Text toggleText = new Text("Hotmap Mode");
        toggleText.setFill(Color.WHITE);
        toggleText.setStyle("-fx-font: 10 Ubuntu;");
        toggleText.setX(110);
        toggleText.setY(765);
        leftPane.getChildren().add(toggleText);

        Button settings = new Button("Settings");
        settings.setLayoutX(330);
        settings.setLayoutY(742);
        settings.setPrefSize(40, 40);
        settings.setStyle("-fx-background-color: grey; -fx-text-fill: white;");
        settings.setOnAction(_ -> System.out.println("Settings"));
        leftPane.getChildren().add(settings);

        stackPane.getChildren().addAll(webView, root);

        Scene scene = new Scene(stackPane, 1280, 832);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}