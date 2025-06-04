package closureAnalysis;

import models.Stop;
import java.util.List;
import java.util.Map;

public class FrequencyFactor {

    public static void calculateFrequencyFactor(List<Stop> stops, Map<String, Integer> stopRouteCounts) {
        // Find the maximum route count
        int maxRouteCount = 0;
        for (int count : stopRouteCounts.values()) {
            if (count > maxRouteCount) {
                maxRouteCount = count;
            }
        }

        // Set Fs for each stop
        for (Stop stop : stops) {
            String stopId = stop.getStopId();
            int routeCount = stopRouteCounts.getOrDefault(stopId, 0);
            double fs = (maxRouteCount == 0) ? 0.0 : (double) routeCount / maxRouteCount;
            stop.setFs(fs);
        }
    }

}
