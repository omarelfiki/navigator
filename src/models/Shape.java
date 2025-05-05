
public class Shape {
    private String shapeId;
    private double shapePtLat;
    private double shapePtLon;
    private double shapePtSequence;
    private double shapeDistanceTraveled;

    public Shape(String shapeId, double shapePtLat, double shapePtLon, double shapePtSequence, double shapeDistanceTraveled) {
        this.shapeId = shapeId;
        this.shapePtLat = shapePtLat;
        this.shapePtLon = shapePtLon;
        this.shapePtSequence = shapePtSequence;
        this.shapeDistanceTraveled = shapeDistanceTraveled;
    }

    public String getShapeId() {
        return shapeId;
    }

    public double getShapePtLat() {
        return shapePtLat;
    }

    public double getShapePtLon() {
        return shapePtLon;
    }

    public double getShapePtSequence() {
        return shapePtSequence;
    }

    public double getShapeDistanceTraveled() {
        return shapeDistanceTraveled;
    }

    @Override
    public String toString() {
        return "models.Shape{" +
                "shapeId='" + shapeId + '\'' +
                ", shapePtLat=" + shapePtLat +
                ", shapePtLon=" + shapePtLon +
                ", shapePtSequence=" + shapePtSequence +
                ", shapeDistanceTraveled=" + shapeDistanceTraveled +
                '}';
    }
}

