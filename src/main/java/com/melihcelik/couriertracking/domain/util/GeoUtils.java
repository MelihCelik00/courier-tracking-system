package com.melihcelik.couriertracking.domain.util;

public class GeoUtils {
    private static final double EARTH_RADIUS = 6371.0; // Earth's radius in kilometers

    private GeoUtils() {
        // Utility class
    }

    /**
     * Calculate distance between two points using the Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }

    /**
     * Check if a point is within a specified radius of another point
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @param radius Radius in meters
     * @return true if point is within radius
     */
    public static boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radius) {
        return calculateDistance(lat1, lon1, lat2, lon2) * 1000 <= radius; // Convert km to meters for comparison
    }
} 