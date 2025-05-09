package util;

public class HaversineUtil {

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        System.out.println("Calculating distance between: " + lat1 + ", " + lon1 + " and " + lat2 + ", " + lon2);
        double R = 6371; // Earth radius in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        System.out.println("Distance: " + (R * c)*1000);
        return R * c;
    }
}
