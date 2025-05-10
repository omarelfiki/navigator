package com.navigator14;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

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

                // > {"routeFrom":{"lat":1,"lon":1},"to":{"lat":2,"lon":2},"startingAt":"08:30:00"} -- input
//                List<Node> path = router.findFastestPath(latStart,lonStart,latEnd,lonEnd,statTime); == method call

                //< [{"mode":"walk","to":{"lat":3,"lon":3},"duration":1,"startTime":"08:30"},
                //{"mode":"ride","to":{"lat":2,"lon":2},"duration":6,"startTime":"08:35",
                //"stop":"Hoofdstraat","route":{"operator":"My Bus Company","shortName":"5",
                //"longName":"Bus number 5","headSign":"Naar Hoofdstraat"}}]

                if (request.containsKey("routeFrom") && request.containsKey("to") && request.containsKey("startingAt")) {
                    AStarRouterV router = new AStarRouterV();
                    Map<String, Object> routeFrom = (Map<String, Object>) request.get("routeFrom");
                    Map<String, Object> to = (Map<String, Object>) request.get("to");
                    String time = (String) request.get("startingAt");
                    double latStart = (double) routeFrom.get("lat");
                    double lonStart = (double) routeFrom.get("lon");
                    double latEnd = (double) to.get("lat");
                    double lonEnd = (double) to.get("lon");
                    List<Node> path = router.findFastestPath(latStart,lonStart,latEnd,lonEnd,time);


                    Node firstNode = path.get(0);
                    Node lastNode = path.get(path.size()-1);

                    if((TripEdge)firstNode.getMode().equals("walk")) {
                        String output = "[" +
                                "{\"mode\":\"walk \"," +
                                "\"to\":{\"lat\":" + firstNode.getStopLat() + ",\"lon\":" + firstNode.getStopLon() + "}," +
                                "\"duration\":" +  (TripEdge)firstNode.getWeight() + "," +
                                "\"startTime\":\"" +  + "\"}," +
                    } else {

                        // Add the methods between the pluses

//                            String output = "{\"mode\":\"" +  + "\"," +
//                            "\"to\":{\"lat\":" +  + ",\"lon\":" +  + "}," +
//                            "\"duration\":" +  + "," +
//                            "\"startTime\":\"" + ] + "\"," +
//                            "\"stop\":\"" +  + "\"," +
//                            "\"route\":{" +
//                            "\"operator\":\"" +  + "\"," +
//                            "\"shortName\":\"" +  + "\"," +
//                            "\"longName\":\"" +  + "\"," +
//                            "\"headSign\":\"" +  + "\"" + "}}]";
                    }

                    sendOk(output);
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
