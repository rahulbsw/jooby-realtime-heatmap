package org.jooby.sse.heatmap.domain;

public class Location {

    private int id;

    private String name;

    private Double latitude;

    private Double longitude;

    public static Location generateRandomLocation(int id, String name) {
        double minLat = 34.00;
        double maxLat = 42.00;
        double latitude = minLat + Math.random() * ((maxLat - minLat) + 1);
        double minLon = -122.00;
        double maxLon = -80.00;
        double longitude = minLon + Math.random() * ((maxLon - minLon) + 1);
        Location location = new Location();
        location.setId(id);
        location.setName(name);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

}