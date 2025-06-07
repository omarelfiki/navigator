package closureAnalysis;

import models.Stop;
import db.TDSImplement;
import java.io.IOException;
import java.util.*;

import static closureAnalysis.FrequencyFactor.calculateFrequencyFactor;
import static closureAnalysis.ProximityFactor.calculateProximityFactor;
import static closureAnalysis.StopEssentialityCalculator.compute;

public class FinalScore {
    private static double weightFs = 0.25; // Weight for stop frequency
    private static double weightPs = 0.20; // Weight for proximity to monument
    private static double weightEs = 0.35; // Weight for stop essentiality
    private static double weightDs = 0.20; // Weight for stop population density

    public static List<StopData> calculateFinalScore(List<Stop> allStops, List<TouristicLocations> monuments) throws IOException {
        //List of all the stops with their weights
        TDSImplement tds = new TDSImplement();
        List<StopData> allStopsData = new ArrayList<> ();

        //inititialize a list with StopData objects to find the lowest scores
        for (Stop stop : allStops) {
            StopData s = new StopData(stop.getStopId());
            allStopsData.add(s);
        }
        //population density factor Ds for each stop
        List<PopulationTile> withStops = GridReaderSimple.buildTilesWithStops();
        List<PopulationTile> ranked = GridReaderSimple.scoreAndRankTiles(withStops);
        for(StopData s:allStopsData){
            for (PopulationTile t : ranked){
                List<Stop> stopList=t.getStopsList();
                for(Stop stop:stopList){
                    if(stop.getStopId().equals(s.getStopId())){
                        s.setDs(t.popScore);
                        break;
                    }
                }

            }
        }
        //Adding essentiality factor Es for each stop
        List<StopEssentialityCalculator> essentialities = compute(allStops);
        for (StopEssentialityCalculator ess : essentialities) {
            for (StopData s : allStopsData) {
                if (s.getStopId().equals(ess.getStopId())) {
                    s.setEs(ess.getEs());
                    break;
                }
            }
        }
        //proximity factor to monuments Ps for each stop
        List<ProximityFactor> pf = calculateProximityFactor(allStops, monuments);
        for( ProximityFactor proximityFactor : pf) {
            for (StopData s : allStopsData) {
                if (s.getStopId().equals(proximityFactor.getStopId())) {
                    s.setPs(proximityFactor.getPs());
                    break;
                }
            }
        }

        //frequency of trips Fs for each stop
        Map<String, Integer> stopRouteCounts =tds.getStopRouteCounts();
        List<FrequencyFactor> fs = calculateFrequencyFactor(allStops,stopRouteCounts);
        for( FrequencyFactor frequencyFactor : fs) {
            for (StopData s : allStopsData) {
                if (s.getStopId().equals(frequencyFactor.getStopId())) {
                    s.setFs(frequencyFactor.getFs());
                    break;
                }
            }
        }
        for (StopData stop : allStopsData) {
            double score = weightFs * stop.getFs()+ weightPs * stop.getPs() + weightEs * stop.getEs() + weightDs * stop.getDs();
            stop.setScore(score);
        }
        return allStopsData;
    }

}
