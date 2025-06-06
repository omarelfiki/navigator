package closureAnalysis;

import models.Stop;
import java.util.List;
import java.util.Map;

public class FrequencyFactor {
    private String stopId;
    private double fs;
    public FrequencyFactor(String stopId) {
        this.stopId = stopId;
        this.fs = 0.0;
    }
    public String getStopId() {return stopId;}
    public double getFs() {return fs;}
    public void setFs(double fs) {this.fs = fs;}

    public static List<FrequencyFactor> calculateFrequencyFactor(List<Stop> stops, Map<String, Integer> stopRouteCounts) {
        // Find the maximum route count
        int maxRouteCount = 0;
        for (int count : stopRouteCounts.values()) {
            if (count > maxRouteCount) {
                maxRouteCount = count;
            }
        }
        List<FrequencyFactor> frequencyFactors = new java.util.ArrayList<>();
        // Set Fs for each stop
        for (Stop stop : stops) {
            FrequencyFactor ff = new FrequencyFactor(stop.getStopId());
            String stopId = stop.getStopId();
            int routeCount = stopRouteCounts.getOrDefault(stopId, 0);
            double fs = (maxRouteCount == 0) ? 0.0 : (double) routeCount / maxRouteCount;
            ff.setFs(fs);
            frequencyFactors.add(ff);
        }
        return frequencyFactors;
    }

}
