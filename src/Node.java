class Node {
    public Node(String stopId, double arrivalTime,Node parent,String mode,Trip tripInfo) {
        this.stopId = stopId;
        this.arrivalTime = arrivalTime;
        this.parent = parent;
        this.mode = mode;
        this.tripInfo = tripInfo;
    }
    String stopId;
    double arrivalTime;
    double g;
    double h;
    Node parent;
    String mode;
    Trip tripInfo;
}
