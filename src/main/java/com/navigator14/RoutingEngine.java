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
import models.Request;
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

                if (request.containsKey("routeFrom") && request.containsKey("to") && request.containsKey("startingAt")) {
                    Request requestR = parseRequest(request);
                    if (requestR.latStart() == 0 || requestR.lonStart() == 0 || requestR.latEnd() == 0 || requestR.lonEnd() == 0) {
                        sendError("Invalid coordinates");
                        break;
                    }
                    if (requestR.time() == null || requestR.time().isEmpty()) {
                        sendError("Invalid time format");
                        break;
                    }
                    AStarRouterV router = new AStarRouterV();
                    List<Node> path = router.findFastestPath(requestR.latStart(), requestR.lonStart(), requestR.latEnd(), requestR.lonEnd(), requestR.time());
                    if (path == null) {
                        sendError("No path found");
                        break;
                    }
                    List<Map<String, Object>> result = parseResult(path, requestR);
                    sendOk(result);
                    continue;
                }
                sendError("Bad request");
            }

        }

    }

    private static Request parseRequest(Map<?, ?> request) {
        Map<String, Object> routeFrom = (Map<String, Object>) request.get("routeFrom");
        Map<String, Object> to = (Map<String, Object>) request.get("to");
        String time = (String) request.get("startingAt");
        double latStart = ((Number) routeFrom.get("lat")).doubleValue();
        double lonStart = ((Number) routeFrom.get("lon")).doubleValue();
        double latEnd = ((Number) to.get("lat")).doubleValue();
        double lonEnd = ((Number) to.get("lon")).doubleValue();
        return new Request(time, latStart, lonStart, latEnd, lonEnd);
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

    private List<Map<String, Object>> parseResult(List<Node> path, Request request) {
        return path.stream().map(node -> {
            if (Objects.equals(node.mode, "WALK")) {
                if(node.parent == null) {
                    return Map.of(
                            "mode", "walk",
                            "to", Map.of("lat", 12.123, "lon", 12.123),
                            "duration", 0,
                            "startTime", request.time()
                    );
                }
                return Map.of(
                        "mode", "walk",
                        "to", Map.of("lat", node.stop.stopLat, "lon", node.stop.stopLon),
                        "duration", 0,
                        "startTime", node.arrivalTime
                );
            } else if (Objects.equals(node.mode, "SAME_TRIP")) {
                return Map.of(
                        "mode", "ride",
                        "to", Map.of("lat", node.stop.stopLat, "lon", node.stop.stopLon),
                        "duration", 0,
                        "startTime", node.arrivalTime,
                        "stop", node.stop.getStopName(),
                        "route", Map.of(
                                "operator", node.trip.route.getAgency() != null ? node.trip.route.getAgency().getAgencyName() : "N/A",
                                "shortName", node.trip.route != null ? node.trip.route.getRouteShortName() : "N/A",
                                "longName", node.trip.route != null ? node.trip.route.getRouteLongName() : "N/A",
                                "headSign", node.trip.getHeadSign() == null ? "N/A" : node.trip.getHeadSign()
                        )
                );
            } else if (Objects.equals(node.mode, "TRANSFER")) {
                return Map.of(
                        "mode", "ride",
                        "to", Map.of("lat", node.stop.stopLat, "lon", node.stop.stopLon),
                        "duration", 0,
                        "startTime", node.arrivalTime,
                        "stop", node.stop.getStopName(),
                        "route", Map.of(
                                "operator", node.trip.route.getAgency() != null ? node.trip.route.getAgency().getAgencyName() : "N/A",
                                "shortName", node.trip.route != null ? node.trip.route.getRouteShortName() : "N/A",
                                "longName", node.trip.route != null ? node.trip.route.getRouteLongName() : "N/A",
                                "headSign", node.trip.getHeadSign() == null ? "N/A" : node.trip.getHeadSign()
                        )
                );
            }
            return null;
        }).filter(Objects::nonNull).toList();
    }
}
// {"routeFrom":{"lat":41.8298,"lon":12.5563},"to":{"lat":41.8258,"lon":12.5623},"startingAt":"08:00:00"} - test case