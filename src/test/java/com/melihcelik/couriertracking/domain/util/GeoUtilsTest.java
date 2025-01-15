package com.melihcelik.couriertracking.domain.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {

    private static final double DELTA = 0.01; // Acceptable difference for double comparisons

    @Test
    void calculateDistance_SamePoint_ShouldReturnZero() {
        double lat = 40.9923307;
        double lon = 29.1244229;
        
        double distance = GeoUtils.calculateDistance(lat, lon, lat, lon);
        
        assertEquals(0.0, distance, DELTA);
    }

    @ParameterizedTest
    @CsvSource({
        // Ataşehir MMM Migros to Novada MMM Migros
        "40.9923307, 29.1244229, 40.986106, 29.1161293, 0.98",
        // Ataşehir MMM Migros to Beylikdüzü 5M Migros
        "40.9923307, 29.1244229, 41.0066851, 28.6552262, 39.41",
        // Novada MMM Migros to Caddebostan MMM Migros
        "40.986106, 29.1161293, 40.9632463, 29.0630908, 5.13"
    })
    void calculateDistance_KnownLocations_ShouldReturnExpectedDistance(
            double lat1, double lon1, double lat2, double lon2, double expectedKm) {
        double distance = GeoUtils.calculateDistance(lat1, lon1, lat2, lon2);
        
        assertEquals(expectedKm, distance, 0.01);
    }

    @Test
    void isWithinRadius_SamePoint_ShouldReturnTrue() {
        double lat = 40.9923307;
        double lon = 29.1244229;
        double radius = 100; // meters
        
        boolean result = GeoUtils.isWithinRadius(lat, lon, lat, lon, radius);
        
        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource({
        // Points 50m apart
        "40.9923307, 29.1244229, 40.9923307, 29.1245229, 100, true",
        // Points 150m apart
        "40.9923307, 29.1244229, 40.9923307, 29.1259229, 100, false",
        // Points exactly 100m apart
        "40.9923307, 29.1244229, 40.9923307, 29.1255229, 100, true"
    })
    void isWithinRadius_VariousDistances_ShouldReturnExpectedResult(
            double lat1, double lon1, double lat2, double lon2, double radiusMeters, boolean expected) {
        boolean result = GeoUtils.isWithinRadius(lat1, lon1, lat2, lon2, radiusMeters);
        
        assertEquals(expected, result);
    }
} 