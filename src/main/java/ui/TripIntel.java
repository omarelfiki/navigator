package ui;

import com.navigator14.HomeUI;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import models.Route;
import models.Trip;
import router.Node;
import util.TimeUtil;

import java.util.*;
import java.util.List;
import static ui.ErrorPopup.showSEConfirmDialog;
import static util.TimeUtil.removeSecondsSafe;

public class TripIntel extends HomeUI {
    public StackPane displayTransportModes(List<Node> result, BorderPane root) {
        StackPane resultPane = new StackPane();
        resultPane.setStyle("-fx-padding: 10;");
        resultPane.setAlignment(Pos.CENTER);
        resultPane.setPrefSize(300, 400);
        resultPane.setTranslateX(10);
        resultPane.setTranslateY(root.getHeight() * 0.38); // 80/832
        
        VBox mainVbox = new VBox();
        mainVbox.setAlignment(Pos.TOP_LEFT);
        mainVbox.setSpacing(10);
        mainVbox.setStyle("-fx-padding: 12;");
        
        ScrollPane scrollPane = new ScrollPane(mainVbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(false);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefViewportHeight(350);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        mainVbox.getChildren().add(getTitle(result));

        for (int i = 0; i < result.size() - 1; i++) {
            HBox hBox = new HBox(10);
            hBox.setAlignment(Pos.CENTER_LEFT);

            String mode = result.get(i + 1).getMode();
            switch (mode) {
                case "WALK" -> {
                    ImageView modeIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ui/walk.png"))));
                    String duration = getDuration(result, i);
                    if (duration.equals("0")) {
                        continue; // Skip if the duration is 0
                    }
                    Text text = getWalkText(result, i, duration);
                    text.setWrappingWidth(280);

                    modeIcon.setFitWidth(24);
                    modeIcon.setFitHeight(24);
                    hBox.getChildren().addAll(modeIcon, text);
                }
                case "SAME_TRIP", "TRANSFER" -> {
                    Trip trip = result.get(i + 1).getTrip();
                    Route route = trip.route();
                    String headSign = trip.headSign() != null ? trip.headSign() : "N/A";
                    String routeName = route.routeLongName() != null && !route.routeLongName().equals("Unknown") ? route.routeLongName() : route.routeShortName();
                    ImageView modeIcon;
                    if (routeName.contains("Metro")) {
                        modeIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ui/metro.png"))));
                    } else {
                        modeIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/ui/bus.png"))));
                    }

                    // Check for consecutive SAME_TRIP nodes with the same Trip
                    int j = i + 1;
                    while (j + 1 < result.size() && "SAME_TRIP".equals(result.get(j + 1).getMode())
                            && Objects.equals(trip, result.get(j + 1).getTrip())) {
                        j++;
                    }

                    VBox segmentBox = new VBox();
                    segmentBox.setSpacing(4);

                    Text routeText = new Text("Take " + routeName + " towards " + headSign);
                    routeText.setStyle("-fx-font-weight: bold; -fx-fill: white;");
                    routeText.setWrappingWidth(280);

                    segmentBox.getChildren().add(routeText);

                    for (int k = i + 1; k <= j; k++) {
                        String stopName = result.get(k).getStop().getStopName();
                        String stopId = result.get(k).getStop().getStopId();
                        Button stopButton = new Button(stopName);
                        stopButton.setPrefWidth(250);
                        stopButton.setStyle("-fx-background-color: rgba(255,71,71,0.63); -fx-text-fill: white; -fx-font-size: 12px; -fx-alignment: CENTER_LEFT;");
                        setStopButton(stopButton, stopId);
                        segmentBox.getChildren().add(stopButton);
                    }

                    modeIcon.setFitWidth(24);
                    modeIcon.setFitHeight(24);
                    hBox.getChildren().addAll(modeIcon, segmentBox);
                    i = j - 1;
                }
            }
            mainVbox.getChildren().add(hBox);
        }

        resultPane.getChildren().add(scrollPane);
        return resultPane;
    }

    private static VBox getTitle(List<Node> result) {
        String startTime = result.getFirst().getArrivalTime() != null ? result.getFirst().getArrivalTime() : "N/A";
        String endTime = result.getLast().getArrivalTime() != null ? result.getLast().getArrivalTime() : "N/A";
        int seconds = (int) TimeUtil.calculateDifference(TimeUtil.parseTime(startTime), TimeUtil.parseTime(endTime));
        int durationMinutes = (seconds > 0) ? Math.max(1, seconds / 60) : 0;

        VBox title_vbox = new VBox();
        title_vbox.setAlignment(Pos.TOP_LEFT);
        title_vbox.setSpacing(5);

        Text title = new Text("Your Trip");
        title.setStyle("-fx-font-size: 20px; -fx-fill: white;");
        title.setTextAlignment(TextAlignment.LEFT);

        Text subtitle = new Text(removeSecondsSafe(startTime) + " - " + removeSecondsSafe(endTime) + " (" + durationMinutes + " min)");
        subtitle.setStyle("-fx-font-size: 12px; -fx-fill: white;");

        Line firstline = getLine();
        title_vbox.getChildren().addAll(title, subtitle, firstline);
        return title_vbox;
    }

    private static Text getWalkText(List<Node> result, int i, String duration) {
        Text text;
        if (i == 0) {
            text = new Text("Walk for " + duration + " min to " + result.get(i + 1).getStop().getStopName());
        } else if (i + 2 == result.size()) {
            text = new Text("Walk for " + duration + " min to your destination");
        } else {
            text = new Text("Walk for " + duration + " min from " + result.get(i).getStop().getStopName() + " to " + result.get(i + 1).getStop().getStopName());

        }
        text.setStyle("-fx-fill: white;");
        return text;
    }

    private static String getDuration(List<Node> result, int i) {
        String departureTime = result.get(i).getArrivalTime() != null ? result.get(i).getArrivalTime() : "N/A";
        String arrivalTime = result.get(i + 1).getArrivalTime() != null ? result.get(i + 1).getArrivalTime() : "N/A";
        return String.valueOf((int) TimeUtil.calculateDifference(
                TimeUtil.parseTime(departureTime),
                TimeUtil.parseTime(arrivalTime)
        ) / 60);
    }

    private static void setStopButton(Button stopButton, String stopId) {
        stopButton.setOnAction(_ -> {
            boolean exclude = showSEConfirmDialog(stopButton);
            if (exclude) {
                stopButton.setStyle("-fx-background-color: rgba(121,110,110,0.63); -fx-text-fill: white; -fx-font-size: 12px; -fx-alignment: CENTER_LEFT;");
                stopButton.setText(stopButton.getText() + " (excluded)");
                avoidedStops.add(stopId);
            }
        });
    }

    private static Line getLine() {
        Line line = new Line();
        line.setStartX(0);
        line.setStartY(600);
        line.setEndX(300);
        line.setEndY(600);
        line.setStyle("-fx-stroke: white; -fx-stroke-width: 1;");
        return line;
    }
}