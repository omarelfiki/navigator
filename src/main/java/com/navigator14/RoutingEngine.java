package com.navigator14;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.leastfixedpoint.json.JSONReader;
import com.leastfixedpoint.json.JSONSyntaxError;
import com.leastfixedpoint.json.JSONWriter;
import db.DBconfig;
import util.AStarRouterV;
import util.Node;

public class RoutingEngine {
    private final JSONReader requestReader =
            new JSONReader(new InputStreamReader(System.in));
    private final JSONWriter<OutputStreamWriter> responseWriter =
            new JSONWriter<>(new OutputStreamWriter(System.out));

    public static void main(String[] args) throws IOException {
        new RoutingEngine().run();
    }

    public void run() throws IOException {
        System.err.println("Starting");
        while (true) {
            Object json;
            try {
                json = requestReader.read();
            } catch (JSONSyntaxError e) {
                sendError("Bad JSON input");
                break;
            } catch (EOFException e) {
                System.err.println("End of input detected");
                break;
            }

            if (json instanceof Map<?, ?> request) {
                if (request.containsKey("load")) {
                    DBconfig dbConfig = new DBconfig((String) request.get("load"));
                    dbConfig.initializeDB();
                    sendOk("Database and GTFS dataset loaded");
                    continue;
                }

                // {"routeFrom":{"lat":41.9296,"lon":12.4844},"to":{"lat":41.7489,"lon":12.5015},"startingAt":"08:00:00"}
                // -- test method call

                //< [{"mode":"walk","to":{"lat":3,"lon":3},"duration":1,"startTime":"08:30"},
                //{"mode":"ride","to":{"lat":2,"lon":2},"duration":6,"startTime":"08:35",
                //"stop":"Hoofdstraat","route":{"operator":"My Bus Company","shortName":"5",
                //"longName":"Bus number 5","headSign":"Naar Hoofdstraat"}}]
                // -- complete output

                // [] -- outer array
                // {"mode":"walk", "to":{"lat":3,"lon":3}, "duration":1, "startTime":"08:30"} -- inner object
                // {"mode":"ride", "to":{"lat":2,"lon":2}, "duration":6, "startTime":"08:35", "stop":"Hoofdstraat", "route":{"operator":"My Bus Company","shortName":"5","longName":"Bus number 5","headSign":"Naar Hoofdstraat"} } -- second object
                // -- output breakdown


                if (request.containsKey("routeFrom") && request.containsKey("to") && request.containsKey("startingAt")) {
                    AStarRouterV router = new AStarRouterV();
                    Map<String, Object> routeFrom = (Map<String, Object>) request.get("routeFrom");
                    Map<String, Object> to = (Map<String, Object>) request.get("to");
                    String time = (String) request.get("startingAt");
                    double latStart = ((Number) routeFrom.get("lat")).doubleValue();
                    double lonStart = ((Number) routeFrom.get("lon")).doubleValue();
                    double latEnd = ((Number) to.get("lat")).doubleValue();
                    double lonEnd = ((Number) to.get("lon")).doubleValue();
                    List<Node> path = router.findFastestPath(latStart, lonStart, latEnd, lonEnd, time);
                    StringBuilder object = new StringBuilder("[");
                    if (path == null) {
                        sendError("No path found");
                    } else {
                        for (Node node : path) {
                            if (Objects.equals(node.mode, "WALK")){
                                object.append("{\"mode\":\"walk\", \"to\":{\"lat\":")
                                        .append(node.stop.stopLat).append(",\"lon\":")
                                        .append(node.stop.stopLon).append("}, \"duration\":")
                                        .append(0).append(", \"startTime\":\"")
                                        .append(node.arrivalTime).append("\"},");
                            }
                            else if (Objects.equals(node.mode, "SAME_TRIP")){
                                object.append("{\"mode\":\"ride\", \"to\":{\"lat\":")
                                        .append(node.stop.stopLat)
                                        .append(",\"lon\":")
                                        .append(node.stop.stopLon)
                                        .append("}, \"duration\":")
                                        .append(0).append(", \"startTime\":\"")
                                        .append(node.arrivalTime)
                                        .append("\", \"stop\":\"")
                                        .append(node.stop.getStopName())
                                        .append("\", \"route\":{\"operator\":\"")
                                        .append(node.trip.route.getAgency().getAgencyName())
                                        .append("\"shortName\":\"").append(node.trip.route.getRouteShortName())
                                        .append("\",\"longName\":\"")
                                        .append(node.trip.route.getRouteLongName())
                                        .append("\"headSign\":\"")
                                        .append(node.trip.getHeadSign())
                                        .append("\"}},");
                            }
                            object.append("]");
                        }
                    }
                    sendOk(object.toString());
                    continue;
                }
            }
            sendError("Bad request");
        }
    }

    private void sendOk(Object value) throws IOException {
        responseWriter.write(Map.of("ok", value));
        responseWriter.getWriter().write('\n');
        responseWriter.getWriter().flush();
    }

    private void sendError(String message) throws IOException {
        responseWriter.write(Map.of("error", message));
        responseWriter.getWriter().write('\n');
        responseWriter.getWriter().flush();
    }
}
