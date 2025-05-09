//abstract class Edge {
//    String toStopId;
//    String departureTime;
//    String arrivalTime;
//    String mode;
//    Double weight;
//
//    public Edge(String toStopId, String departureTime, String arrivalTime, String mode,Double weight) {
//        this.toStopId = toStopId;
//        this.departureTime = departureTime;
//        this.arrivalTime = arrivalTime;
//        this.mode = mode;
//        this.weight=weight;
//    }
//
//    public abstract Trip getTripInfo();
//
//    public String getToStopId() {
//        return toStopId;
//    }
//
//    public String getDepartureTime() {
//        return departureTime;
//    }
//
//    public String getArrivalTime() {
//        return arrivalTime;
//    }
//
//    public String getMode() {
//        return mode;
//    }
//
//    public double getWeight() {
//        return weight;
//    }
//    public void setWeight(Node startingNode){
//        this.weight = toSeconds(this.arrivalTime) -toSeconds(startingNode.arrivalTime);
//    }
//
//    private double toSeconds(String time) {
//        String[] parts = time.split(":");
//        int hours = Integer.parseInt(parts[0]);
//        int minutes = Integer.parseInt(parts[1]);
//        int seconds = Integer.parseInt(parts[2]);
//        return hours * 60 + minutes + seconds / 60.0;
//    }
//
//    public abstract Double getDistanceKm() ;
//}
