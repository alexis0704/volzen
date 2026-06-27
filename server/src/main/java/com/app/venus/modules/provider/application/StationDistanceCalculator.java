package com.app.venus.modules.provider.application;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class StationDistanceCalculator {
    private static final double EARTH_RADIUS_KM = 6371.0088;

    public double distanceKm(BigDecimal fromLat, BigDecimal fromLng, BigDecimal toLat, BigDecimal toLng) {
        double lat1 = Math.toRadians(fromLat.doubleValue());
        double lng1 = Math.toRadians(fromLng.doubleValue());
        double lat2 = Math.toRadians(toLat.doubleValue());
        double lng2 = Math.toRadians(toLng.doubleValue());

        double latDelta = lat2 - lat1;
        double lngDelta = lng2 - lng1;
        double haversine = Math.pow(Math.sin(latDelta / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(lngDelta / 2), 2);

        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(haversine));
    }
}
