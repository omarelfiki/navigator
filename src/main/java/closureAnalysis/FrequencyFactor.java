package closureAnalysis;

import models.Stop;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FrequencyFactor {
    private String stopId;
    private double fs;

    public FrequencyFactor(String stopId) {
        this.stopId = stopId;
        this.fs = 0.0;
    }

    public String getStopId() {
        return stopId;
    }

    public double getFs() {
        return fs;
    }

    public void setFs(double fs) {
        this.fs = fs;
    }

    public static List<FrequencyFactor> calculateFrequencyFactor(List<Stop> stops, Map<String, StopFrequencyData> stopFreqData) {
        // maxRoute keeps track of how frequent a stop is included in different routes
        // maxTrips keeps track of how frequent trips go through a stop
        int maxRoute = 0;
        int maxTrips = 0;

        for (StopFrequencyData data : stopFreqData.values()) {
            if (data.routeCount > maxRoute) maxRoute = data.routeCount;
            if (data.tripCount > maxTrips) maxTrips = data.tripCount;
        }

        List<FrequencyFactor> frequencyFactors = new ArrayList<>();

        for (Stop stop : stops) {
            String stopId = stop.getStopId();
            StopFrequencyData data = stopFreqData.getOrDefault(stopId, new StopFrequencyData(0, 0));

            double relativeRouteCount = (maxRoute == 0) ? 0.0 : (double) data.routeCount / maxRoute;
            double relativeTripsCount = (maxTrips == 0) ? 0.0 : (double) data.tripCount / maxTrips;

            double fs = 0.5 * relativeRouteCount + 0.5 * relativeTripsCount;

            FrequencyFactor ff = new FrequencyFactor(stopId);
            ff.setFs(fs);
            frequencyFactors.add(ff);
        }

        return frequencyFactors;
    }
}
