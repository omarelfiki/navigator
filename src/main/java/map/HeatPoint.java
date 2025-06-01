package map;

public record HeatPoint(double latitude, double longitude, double time) {

    @Override
    public String toString() {
        return "HeatPoint{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", time=" + time +
                '}';
    }
}
