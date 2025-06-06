package closureAnalysis;

import db.TDSImplement;
import models.Stop;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class RunResult {

    public static void main(String[] args) throws IOException {
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
        List<Stop> allStops = new TDSImplement().getAllStops();

        // Calculate all scores
        List<StopData> allStopsData = FinalScore.calculateFinalScore(allStops, monuments);

        // Sort by score (lowest = best candidates for closure)
        allStopsData.sort(Comparator.comparingDouble(StopData::getScore));

        // Print top 3 stops with the lowest scores
        System.out.println("Top 3 stops to consider for closure:");
        for (int i = 0; i < 3; i++) {
            System.out.println("Stop ID: " + allStopsData.get(i).getStopId() +
                               " Score: " + allStopsData.get(i).getScore() +
                               " Frequency Factor (Fs): " + allStopsData.get(i).getFs() +
                               " Proximity Factor (Ps): " + allStopsData.get(i).getPs() +
                               " Essentiality Score (Es): " + allStopsData.get(i).getEs() +
                               " Population Density Score (Ds): " + allStopsData.get(i).getDs());
        }
    }
}
