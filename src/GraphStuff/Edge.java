abstract class Edge {
    protected final String toStopId;
    protected final String departureTime;
    protected final String arrivalTime;
    protected final String mode;

    public Edge(String toStopId, String departureTime, String arrivalTime, String mode) {
        this.toStopId = toStopId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.mode = mode;
    }

    public abstract Trip getTripInfo();

    public String getToStopId() {
        return toStopId;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getMode() {
        return mode;
    }

    public double getWeight() {
        return toMinutes(arrivalTime) - toMinutes(departureTime);
    }

    private double toMinutes(String time) {
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        return hours * 60 + minutes + seconds / 60.0;
    }

    public abstract Double getDistanceKm() ;
}
