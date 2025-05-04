package db;
import java.util.List;
import models.*;
public class TDSImplement implements TransitDataService.TDS
{
    private final TDBService dbService;

    public TDSImplement(TDBService dbService) {
        this.dbService = dbService;
    }

    @Override
    public Stop getStop2(String stopId) {
        return dbService.getStopById(stopId);
    }

    @Override
    public List<Stop> getAllStops() {
        return dbService.getAllStops();
    }

    @Override
    public List<StopTime> getStopTimesForTrip(String tripId) {
        return dbService.getStopTimesByTripId(tripId);
    }

    @Override
    public List<StopTime> getStopTimesForStop(String stopId) {
        return dbService.getStopTimesByStopId(stopId);
    }

    @Override
    public List<StopTime> getFutureDepartures(String stopId, int afterTime) {
        return dbService.getFutureDepartures(stopId, afterTime);
    }

    @Override
    public Trip getTrip(String tripId) {
        return dbService.getTripById(tripId);
    }

    @Override
    public Route getRoute2(String routeId) {
        return dbService.getRouteById(routeId);
    }
}

