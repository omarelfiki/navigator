package com.navigator14;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.leastfixedpoint.json.JSONReader;
import com.leastfixedpoint.json.JSONSyntaxError;
import com.leastfixedpoint.json.JSONWriter;
import db.DBConfig;
import models.Request;
import router.AStarRouterV;
import router.Node;
import util.PathCompressor;
import util.TimeUtil;

import static util.DebugUtil.getDebugMode;
import static util.TimeUtil.parseTime;

public class RoutingEngine {
    private final JSONReader requestReader;
    private final JSONWriter<OutputStreamWriter> responseWriter;

    public RoutingEngine(JSONReader requestReader, JSONWriter<OutputStreamWriter> responseWriter) {
        this.requestReader = requestReader;
        this.responseWriter = responseWriter;
    }

    public RoutingEngine() {
        this(new JSONReader(new InputStreamReader(System.in)), new JSONWriter<>(new OutputStreamWriter(System.out)));
    }

    public static void main(String[] args) throws IOException {
        String debug = System.getenv("debug");
        boolean isDebugMode = getDebugMode();
        if (debug != null) {
            System.setProperty("debug", debug);
        } else {
            if (isDebugMode) System.err.println("Environment variable 'debug' is not set. Debug mode is enabled by default.");
        }
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
                if (request.containsKey("exit")) {
                    System.err.println("Exit command received");
                    break; // Exit the loop
                }

                if (request.containsKey("load")) {
                    if (initDB(request)) return;
                    sendOk("loaded");
                    continue;
                }

                if (request.containsKey("routeFrom") && request.containsKey("to") && request.containsKey("startingAt")) {
                    Request requestR = parseRequest(request);
                    if (requestR.time() == null || requestR.time().isEmpty()) {
                        sendError("Invalid time format");
                        continue;
                    }
                    List<String> avoidedStops = new ArrayList<>();
                    AStarRouterV router = new AStarRouterV();
                    List<Node> path = router.findFastestPath(requestR.latStart(), requestR.lonStart(), requestR.latEnd(), requestR.lonEnd(), requestR.time(),avoidedStops);
                    if (path == null) {
                        sendError("No path found");
                        continue;
                    }
                    List<Map<String, Object>> result = parseResult(path, requestR);
                    result = PathCompressor.compress(result);
                    sendOk(result);
                    continue;
                }
                sendError("Bad request");
            }

        }

    }

    private boolean initDB(Map<?, ?> request) throws IOException {
        String load = (String) request.get("load");
        if (load == null || load.isEmpty()) {
            sendError("Invalid load path");
            return true;
        }
        File file = new File(load);
        if (!file.exists()) {
            sendError("File does not exist");
            return true;
        }
        DBConfig dbConfig = new DBConfig(load);
        dbConfig.initializeDB();
        return false;
    }

    private static Request parseRequest(Map<?, ?> request) {
        Map<String, Object> routeFrom = (Map<String, Object>) request.get("routeFrom");
        Map<String, Object> to = (Map<String, Object>) request.get("to");
        String time = (String) request.get("startingAt");
        time = parseTime(time);
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
            if (Objects.equals(node.getMode(), "WALK")) {
                if(node.getParent() == null) {
                    return Map.of(
                            "mode", "walk",
                            "to", Map.of("lat", request.latStart(), "lon", request.lonStart()),
                            "duration", 0,
                            "startTime", TimeUtil.removeSecondsSafe(request.time())
                    );
                }
                return Map.of(
                        "mode", "walk",
                        "to", Map.of("lat", node.getStop().getStopLat(), "lon", node.getStop().getStopLon()),
                        "duration", 0,
                        "startTime", TimeUtil.removeSecondsSafe(node.getArrivalTime())
                );
            } else if (Objects.equals(node.getMode(), "SAME_TRIP")) {
                return Map.of(
                        "mode", "ride",
                        "to", Map.of("lat", node.getStop().getStopLat(), "lon", node.getStop().getStopLon()),
                        "duration", 0,
                        "startTime", TimeUtil.removeSecondsSafe(node.getArrivalTime()),
                        "stop", node.getStop().getStopName(),
                        "route", Map.of(
                                "operator", node.getTrip().route().agency() != null ? node.getTrip().route().agency().agencyName() : "N/A",
                                "shortName", node.getTrip().route().routeShortName(),
                                "longName", node.getTrip().route().routeLongName(),
                                "headSign", node.getTrip().headSign() == null ? "N/A" : node.getTrip().headSign()
                        )
                );
            } else if (Objects.equals(node.getMode(), "TRANSFER")) {
                return Map.of(
                        "mode", "ride",
                        "to", Map.of("lat", node.getStop().getStopLat(), "lon", node.getStop().getStopLon()),
                        "duration", 0,
                        "startTime", TimeUtil.removeSecondsSafe(node.getArrivalTime()),
                        "stop", node.getStop().getStopName(),
                        "route", Map.of(
                                "operator", node.getTrip().route().agency() != null ? node.getTrip().route().agency().agencyName() : "N/A",
                                "shortName", node.getTrip().route().routeShortName(),
                                "longName", node.getTrip().route().routeLongName(),
                                "headSign", node.getTrip().headSign() == null ? "N/A" : node.getTrip().headSign()
                        )
                );
            }
            return null;
        }).filter(Objects::nonNull).toList();
    }
}
// {"routeFrom":{"lat":41.904,"lon":12.5004},"to":{"lat":41.8791,"lon":12.5221},"startingAt":"09:30"} - test case
//  Roma Termini - Vatican test case below
// {"routeFrom":{"lat":41.900496398,"lon":12.501164662},"to":{"lat":41.906487,"lon":12.453641},"startingAt":"09:30"}