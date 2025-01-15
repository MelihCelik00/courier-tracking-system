package com.melihcelik.couriertracking.infrastructure.rest;

public final class ApiEndpoints {
    private ApiEndpoints() {
        // Private constructor to prevent instantiation
    }
    public static final String API_BASE = "/api/v1";
    public static final String COURIERS = API_BASE + "/couriers";


    public static final String REPORT_LOCATION = "/{courierId}/locations";
    public static final String GET_COURIER = "/{courierId}";
    public static final String GET_TOTAL_TRAVEL_DISTANCE = "/{courierId}/total-travel-distance";
} 