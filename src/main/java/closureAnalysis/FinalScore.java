package closureAnalysis;

import models.Stop;
import db.TDSImplement;
import java.io.IOException;
import java.util.*;

import static closureAnalysis.FrequencyFactor.calculateFrequencyFactor;
import static closureAnalysis.ProximityFactor.calculateProximityFactor;
import static closureAnalysis.StopEssentialityCalculator.compute;

public class FinalScore {
    private static final double weightFs = 0.25; // Weight for stop frequency
    private static final double weightPs = 0.20; // Weight for proximity to monument
    private static final double weightEs = 0.35; // Weight for stop essentiality
    private static final double weightDs = 0.20; // Weight for stop population density


    public static List<StopData> calculateFinalScore(List<Stop> allStops) throws IOException {
        List<TouristicLocations> monuments = Arrays.asList(
                new TouristicLocations("Colosseum", 41.890209, 12.492231),
                new TouristicLocations("Pantheon", 41.898610, 12.476872),
                new TouristicLocations("Roman Forum", 41.892465, 12.485324),
                new TouristicLocations("Piazza Navona", 41.8989, 12.4731),
                new TouristicLocations("Trevi Fountain", 41.900932, 12.483313),
                new TouristicLocations("Spanish Steps", 41.9057, 12.4823),
                new TouristicLocations("St. Peter's Basilica", 41.9022, 12.4539),
                new TouristicLocations("Castel Sant'Angelo", 41.9031, 12.4663),
                new TouristicLocations("Piazza Venezia & Altare della Patria", 41.8955, 12.4828),
                new TouristicLocations("Galleria Borghese & Villa Borghese", 41.9142, 12.4923),
                new TouristicLocations("Baths of Caracalla", 41.8796, 12.4930),
                new TouristicLocations("Palatine Hill", 41.8894, 12.4882),
                new TouristicLocations("Ostia Antica", 41.7550, 12.2930),
                new TouristicLocations("Termini Station", 41.9028, 12.5014),
                new TouristicLocations("Sapienza University of Rome", 41.9063, 12.5137),
                new TouristicLocations("Policlinico Umberto I (Main Hospital)", 41.9068, 12.5142),
                new TouristicLocations("Via del Corso (Main Shopping Street)", 41.9002, 12.4805),
                new TouristicLocations("Euroma2 Shopping Mall", 41.7914, 12.4661),
                new TouristicLocations("EUR Business District", 41.8325, 12.4714),
                new TouristicLocations("Vatican Museums & Sistine Chapel", 41.9065, 12.4536),
                new TouristicLocations("Trastevere District", 41.8880, 12.4663),
                new TouristicLocations("Campo de' Fiori", 41.8950, 12.4728),
                new TouristicLocations("Piazza del Popolo", 41.9112, 12.4783),
                new TouristicLocations("MAXXI – National Museum of 21st Century Arts", 41.9293, 12.4669),
                new TouristicLocations("Cinecittà Studios", 41.8482, 12.5730),
                new TouristicLocations("Stadio Olimpico", 41.9339, 12.4547),
                new TouristicLocations("Auditorium Parco della Musica", 41.9279, 12.4828),
                new TouristicLocations("Roma Tiburtina Station", 41.9108, 12.5302),
                new TouristicLocations("Porta di Roma Shopping Centre", 41.9670, 12.5466),
                new TouristicLocations("Appian Way Regional Park (Parco della Caffarella)", 41.8572, 12.5173)
        );
        //List of all the stops with their weights
        TDSImplement tds = new TDSImplement();
        List<StopData> allStopsData = new ArrayList<> ();

        //inititialize a list with StopData objects to find the lowest scores
        for (Stop stop : allStops) {
            StopData s = new StopData(stop.getStopId(), stop.getStopName(), stop.getStopLat(), stop.getStopLon());
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
        List<ProximityFactor> pf = calculateProximityFactor(allStops,monuments);
        for( ProximityFactor proximityFactor : pf) {
            for (StopData s : allStopsData) {
                if (s.getStopId().equals(proximityFactor.getStopId())) {
                    s.setPs(proximityFactor.getPs());
                    break;
                }
            }
        }

        // Fs - Frequency Factor (route count + trip count)
        Map<String, StopFrequencyData> stopFrequencyData = tds.getStopFrequencyData();
        List<FrequencyFactor> fs = calculateFrequencyFactor(allStops, stopFrequencyData);
        for (FrequencyFactor frequencyFactor : fs) {
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
