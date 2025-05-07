import java.sql.*;
import java.util.*;

public class GraphBuilder {
    private final DBaccess db;
    private final double maxWalkingDistanceKm;
    private final int maxTravelMinutes = 120;
    private final Map<String, Edge> earliestEdgeCache = new HashMap<>();

    public GraphBuilder(DBaccess db, double maxWalkingDistanceKm) {
        this.db = db;
        this.maxWalkingDistanceKm = maxWalkingDistanceKm;
    }

    public Graph buildGraph(LatLon startPoint, LatLon endPoint, String startTime) {
        Graph graph = new Graph();
        Node startNode = new Node("START", startTime, null);
        graph.nodes.put(startNode.stopId, startNode);

        double centerLat = (startPoint.lat + endPoint.lat) / 2.0;
        double centerLon = (startPoint.lon + endPoint.lon) / 2.0;
        double boundingRadiusKm = GeoUtil.distance(startPoint.lat, startPoint.lon, endPoint.lat, endPoint.lon) / 2.0 + maxWalkingDistanceKm;

        List<Stop> candidateStops = getStopsWithinRadiusOptimized(centerLat, centerLon, boundingRadiusKm);
        for (Stop stop : candidateStops) {
            double distance = GeoUtil.distance(startPoint.lat, startPoint.lon, stop.getStopLat(), stop.getStopLon());
            if (distance > maxWalkingDistanceKm) continue;
            double walkSeconds = distance / 5.0 * 3600.0;
            String arrivalTime = addSecondsToTime(startTime, (int) walkSeconds);
            double weight = walkSeconds;
            Edge edge = new WalkingEdge(stop.getStopId(), startTime, arrivalTime, distance) {
                @Override
                public double getWeight() {
                    return weight;
                }
            };
            graph.addEdge(startNode, edge);
        }

        PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingDouble(n -> toSeconds(n.arrivalTime)));
        frontier.addAll(graph.nodes.values());
        Set<String> visited = new HashSet<>();

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();
            if (visited.contains(current.stopId) || (toSeconds(current.arrivalTime) - toSeconds(startTime)) > maxTravelMinutes * 60) continue;
            visited.add(current.stopId);

            Edge earliestEdge = earliestEdgeCache.computeIfAbsent(current.stopId + "@" + current.arrivalTime, k -> getEarliestTransitEdge(current));
            if (earliestEdge != null) {
                double weight = toSeconds(earliestEdge.getArrivalTime()) - toSeconds(current.arrivalTime);
                if (weight >= 0 && !visited.contains(earliestEdge.getToStopId())) {
                    graph.addEdge(current, new TripEdge(earliestEdge.getToStopId(), earliestEdge.getDepartureTime(), earliestEdge.getArrivalTime(), earliestEdge.getTripInfo()) {
                        @Override
                        public double getWeight() {
                            return weight;
                        }
                    });
                    frontier.add(new Node(earliestEdge.getToStopId(), earliestEdge.getArrivalTime(), earliestEdge.getTripInfo()));
                }
            }

            List<Edge> transferEdges = getTransferEdges(current);
            for (Edge edge : transferEdges) {
                double weight = toSeconds(edge.getArrivalTime()) - toSeconds(current.arrivalTime);
                if (weight >= 0 && !visited.contains(edge.getToStopId())) {
                    graph.addEdge(current, new WalkingEdge(edge.getToStopId(), edge.getDepartureTime(), edge.getArrivalTime(), ((WalkingEdge) edge).getDistanceKm()) {
                        @Override
                        public double getWeight() {
                            return weight;
                        }
                    });
                    frontier.add(new Node(edge.getToStopId(), edge.getArrivalTime(), null));
                }
            }

            double[] currentCoords = getStopCoordinates(current.stopId);
            double toGoal = GeoUtil.distance(currentCoords[0], currentCoords[1], endPoint.lat, endPoint.lon);
            if (toGoal <= maxWalkingDistanceKm) {
                double walkSeconds = toGoal / 5.0 * 3600.0;
                String arrivalTime = addSecondsToTime(current.arrivalTime, (int) walkSeconds);
                double weight = walkSeconds;
                if (weight >= 0) {
                    Edge edge = new WalkingEdge("END", current.arrivalTime, arrivalTime, toGoal) {
                        @Override
                        public double getWeight() {
                            return weight;
                        }
                    };
                    graph.addEdge(current, edge);
                    graph.nodes.put("END", new Node("END", arrivalTime, null));
                }
            }
        }

        return graph;
    }

    private Edge getEarliestTransitEdge(Node current) {
        String query = "SELECT t.trip_id, st.departure_time, st2.stop_id AS to_stop_id, st2.arrival_time " +
                "FROM stop_times st " +
                "JOIN stop_times st2 ON st.trip_id = st2.trip_id AND st2.stop_sequence = st.stop_sequence + 1 " +
                "JOIN trips t ON t.trip_id = st.trip_id " +
                "WHERE st.stop_id = ? AND TIME_TO_SEC(st.departure_time) > TIME_TO_SEC(?) " +
                "ORDER BY TIME_TO_SEC(st.departure_time) LIMIT 1";

        try (PreparedStatement ps = db.conn.prepareStatement(query)) {
            ps.setString(1, current.stopId);
            ps.setString(2, current.arrivalTime);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String toStopId = rs.getString("to_stop_id");
                String departure = rs.getString("departure_time");
                String arrival = rs.getString("arrival_time");
                String tripId = rs.getString("trip_id");
                if (tripId == null || tripId.trim().isEmpty()) {
                    System.err.println("Warning: Invalid trip_id from stop " + current.stopId + " at time " + current.arrivalTime);
                }
                Trip trip = new Trip(tripId, null, null, null, null, null, null, false);
                return new TripEdge(toStopId, departure, arrival, trip);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Edge> getTransferEdges(Node current) {
        List<Edge> edges = new ArrayList<>();
        double[] coords = getStopCoordinates(current.stopId);
        List<Stop> nearby = getStopsWithinRadiusOptimized(coords[0], coords[1], 0.2); // limit radius
        PriorityQueue<Stop> closest = new PriorityQueue<>(Comparator.comparingDouble(s -> GeoUtil.distance(coords[0], coords[1], s.getStopLat(), s.getStopLon())));
        closest.addAll(nearby);

        int added = 0;
        while (!closest.isEmpty() && added < 3) {
            Stop stop = closest.poll();
            if (stop.getStopId().equals(current.stopId)) continue;
            if (!hasUsefulTrip(stop.getStopId(), current.arrivalTime)) continue;
            double distance = GeoUtil.distance(coords[0], coords[1], stop.getStopLat(), stop.getStopLon());
            double walkSeconds = distance / 5.0 * 3600.0;
            String arrival = addSecondsToTime(current.arrivalTime, (int) walkSeconds);
            double weight = walkSeconds;
            edges.add(new WalkingEdge(stop.getStopId(), current.arrivalTime, arrival, distance) {
                @Override
                public double getWeight() {
                    return weight;
                }
            });
            added++;
        }

        return edges;
    }

    private boolean hasUsefulTrip(String stopId, String afterTime) {
        String query = "SELECT 1 FROM stop_times WHERE stop_id = ? AND TIME_TO_SEC(departure_time) > TIME_TO_SEC(?) LIMIT 1";
        try (PreparedStatement ps = db.conn.prepareStatement(query)) {
            ps.setString(1, stopId);
            ps.setString(2, afterTime);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private double[] getStopCoordinates(String stopId) {
        String query = "SELECT stop_lat, stop_lon FROM stops WHERE stop_id = ?";
        try (PreparedStatement ps = db.conn.prepareStatement(query)) {
            ps.setString(1, stopId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new double[] { rs.getDouble("stop_lat"), rs.getDouble("stop_lon") };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new double[] { 0.0, 0.0 };
    }

    private List<Stop> getStopsWithinRadiusOptimized(double lat, double lon, double radiusKm) {
        List<Stop> stops = new ArrayList<>();
        double latDelta = radiusKm / 111.0;
        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));
        String query = "SELECT stop_id, stop_name, stop_lat, stop_lon FROM stops WHERE stop_lat BETWEEN ? AND ? AND stop_lon BETWEEN ? AND ?";
        try (PreparedStatement ps = db.conn.prepareStatement(query)) {
            ps.setDouble(1, lat - latDelta);
            ps.setDouble(2, lat + latDelta);
            ps.setDouble(3, lon - lonDelta);
            ps.setDouble(4, lon + lonDelta);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double stopLat = rs.getDouble("stop_lat");
                double stopLon = rs.getDouble("stop_lon");
                double distance = GeoUtil.distance(lat, lon, stopLat, stopLon);
                if (distance <= radiusKm) {
                    stops.add(new Stop(rs.getString("stop_id"), rs.getString("stop_name"), stopLat, stopLon));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stops;
    }

    private String addSecondsToTime(String time, int secondsToAdd) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        int totalSeconds = hours * 3600 + minutes * 60 + seconds + secondsToAdd;
        return String.format("%02d:%02d:%02d", (totalSeconds / 3600), (totalSeconds / 60) % 60, totalSeconds % 60);
    }

    private double toSeconds(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        return hours * 3600 + minutes * 60 + seconds;
    }

    private String fixTimeString(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        return String.format("%02d:%s:%s", hours % 24, parts[1], parts[2]);
    }
}
