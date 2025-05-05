//
//import org.json.JSONObject;
//
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
import java.util.ArrayList;
public class NearbyStops {

    public static ArrayList<Stop> getNearbyStops(double lat, double lon, double radiusMeters) {
        ArrayList<Stop> stopsWithinRadius = new ArrayList<>();

        /* TO-DO: Loop through all stops and calculate the distance between
           the input point and the stop point and compare this distance to the radius.
           If distance <= radiusMeters, then add the stop object to ArrayList.
         */


        return stopsWithinRadius;
    }

    //sql implementation

//    public Stop getClosestStops(double lat, double lon) {
//        String useDbQuery = "USE " + dbName;
//        try (Statement stmt = conn.createStatement()) {
//            stmt.execute(useDbQuery);
//        } catch (SQLException e) {
//            System.out.println("SQL Error: " + e.getMessage());
//        }
//        String MSQLquery = "SELECT get_closest_stop(?, ?) AS closest_stop;";
//        try (PreparedStatement stmt = conn.prepareStatement(MSQLquery)) {
//            stmt.setDouble(1, lat);
//            stmt.setDouble(2, lon);
//            ResultSet rs = stmt.executeQuery();
//
//            if (rs.next()) {
//                String json = rs.getString("closest_stop");
//                JSONObject jsonObject = new JSONObject(json);
//
//                String stopID = jsonObject.optString("stop_id", "N/A");
//                String stopName = jsonObject.optString("stop_name", "N/A");
//                double stopLat = jsonObject.optDouble("stop_lat", 0.0);
//                double stopLon = jsonObject.optDouble("stop_lon", 0.0);
//
//
//                return new Stop(stopID, stopName, stopLat, stopLon);
//            }
//        } catch (SQLException e) {
//            System.out.println("SQL Error: " + e.getMessage());
//        }
//        return null;
//    }
}
