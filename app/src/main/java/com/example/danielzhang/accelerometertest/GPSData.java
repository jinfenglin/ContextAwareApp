package com.example.danielzhang.accelerometertest;

/**
 * Created by Daniel Zhang on 2016/10/21.
 */

public class GPSData {

    private long timestamp;
    private double longitude;
    private double latitude;


    public GPSData(long timestamp, double x, double y) {
        this.timestamp = timestamp;
        this.longitude = x;
        this.latitude = y;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double x) {
        this.longitude = x;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double y) {
        this.latitude = y;
    }


    public String toString()
    {
        return "t="+timestamp;
    }
}
