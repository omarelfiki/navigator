package ui;

import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import router.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TripIntel {
    public final String mode;
    public final String time;
    public final String stop;

    public TripIntel(String mode, String time, String stop) {
        this.mode = mode;
        this.time = time;
        this.stop = stop;
    }


    private int turnTimeToInt(String sTime) {
        String[] splitTimes = sTime.split(":");
        int hours = Integer.parseInt(splitTimes[0]);
        int minutes = Integer.parseInt(splitTimes[1]);
        int seconds = Integer.parseInt(splitTimes[2]);

        return hours * 3600 + minutes * 60 + seconds;
    }

    private int[] splitTimes(int totalSeconds) {
        int[] timeArray = new int[2];
        int currentMinutes = totalSeconds / 60;
        int currentSeconds = totalSeconds % 60;
        timeArray[0] = currentMinutes;
        timeArray[1] = currentSeconds;
        return timeArray;
    }

    private StackPane displayTransportModes(Node destinationNode, BorderPane root) {
        // StackPane creation and styling - do not change
        StackPane resultPane = new StackPane();
        resultPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-padding: 10;");
        resultPane.setAlignment(Pos.CENTER);
        resultPane.setPrefSize(300, 200);
        resultPane.setTranslateX(root.getWidth() * 0.02); // 10/1280
        resultPane.setTranslateY(root.getHeight() * 0.4); // 80/832

        List<TripIntel> tripIntel = new ArrayList<>();
        Node current = destinationNode;
        while (current != null) {
            System.out.println(current);

            tripIntel.add(new TripIntel(current.getMode(), current.getArrivalTime(), current.getStopId()));

            current = current.getParent();
            // last Node (first current) is the start point of the trip.
            // this means that the 'arrivalTime' of the first stop
            // is just the start of the trip.
            // everything needs to be in seconds, so first 2 characters need
            // to be turned into integer, multiplied by 3600.
            // split after the colon, then multiply the second pair of numbers by 60.
            // count this up with the hour-number and the second-number after the last colon.
            // then subtract the arrivaltime of the next stop that turns into a transfer or end
            // by the arrivalTime of the last different type of transportation.
            // Turn it into minutes. If it's less than a minute, just make it into "<1min". If it
            // exceeds 60 minutes, turn it into x hours + minutes (just modulus the number by 60).
        }

        Collections.reverse(tripIntel);


        Text transportTitle = new Text("Modes of Transport:");
        transportTitle.setStyle("-fx-font: 16 Ubuntu; -fx-fill: white;");

        VBox contentBox = new VBox(10);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().add(transportTitle);


        int lastArrivalTime = turnTimeToInt(tripIntel.getFirst().time);
        int currentTripTime = 0;
        String tripType = "";
        String currentMode = "";
        boolean newTrip = false;
        int[] currentSplit = new int[2];

        for (int i = 0; i < tripIntel.size(); i++) {

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            TripIntel tripIntelX = tripIntel.get(i);
            if (tripIntelX.mode.equals("WALK")) {
                currentMode = "Walk";
            } else if (tripIntelX.mode.equals("SAME_TRIP")) {
                currentMode = "Bus";
            } else {
                currentMode = "Bus";
            }

            if (i + 1 < tripIntel.size()) {
                if (tripIntel.get(i + 1).mode.equals("TRANSFER")) {
                    currentTripTime = turnTimeToInt(tripIntelX.time) - lastArrivalTime;
                    lastArrivalTime = turnTimeToInt(tripIntelX.time);
                    newTrip = true;
                    currentSplit = splitTimes(currentTripTime);

                }
            }

            if (i == tripIntel.size() - 1) {
                currentTripTime = turnTimeToInt(tripIntelX.time) - lastArrivalTime;
                newTrip = true;
                currentSplit = splitTimes(currentTripTime);
            }

            if (newTrip) {
                Text modeText = new Text(currentMode + " Time: " + currentSplit[0] + " minutes " +
                        currentSplit[1] + " seconds " + tripIntelX.stop);
                modeText.setStyle("-fx-font: 14 Ubuntu; -fx-fill: white;");
                row.getChildren().add(modeText);
                newTrip = false;
            }


            // ImageView icon = getModeIcon(mode);
            // if (icon != null) row.getChildren().add(icon);

            contentBox.getChildren().add(row);
        }

        resultPane.getChildren().add(contentBox);
        return resultPane;
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
