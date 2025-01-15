package com.melihcelik.couriertracking.domain.util;

public class DistanceCalculator {
    private static final double EARTH_RADIUS = 6371.0; // Earth's radius in kilometers

    public static void main(String[] args) {
        // Test case 1: Ataşehir to Kadıköy
        calculateAndPrint(40.9923307, 29.1244229, 40.986106, 29.1161293, "Ataşehir to Kadıköy");

        // Test case 2: Eminönü to Sirkeci
        calculateAndPrint(41.0082376, 28.9783589, 41.0051129, 28.9856033, "Eminönü to Sirkeci");

        // Test case 3: Near poles
        calculateAndPrint(89.9, 0.0, 89.9, 180.0, "Near North Pole");
        calculateAndPrint(-89.9, 0.0, -89.9, 180.0, "Near South Pole");

        // Test case 4: Date line crossing
        calculateAndPrint(0.0, 179.9, 0.0, -179.9, "Date Line Crossing");

        // Test case 5: Just outside radius
        calculateAndPrint(40.9923307, 29.1244229, 40.9923507, 29.1244429, "Outside Radius");
    }

    private static void calculateAndPrint(double lat1, double lon1, double lat2, double lon2, String description) {
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        System.out.printf("%s: %.2f meters%n", description, distance);
    }

    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c * 1000; // Convert to meters
    }
} 