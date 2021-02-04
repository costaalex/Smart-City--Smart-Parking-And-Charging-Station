package model;


public class GpsLocationDescriptor {

    private Double latitude;
    private Double longitude;

    public GpsLocationDescriptor() {
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public GpsLocationDescriptor(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GpsLocationDescriptor{");
        sb.append("latitude=").append(latitude);
        sb.append(", longitude=").append(longitude);
        sb.append('}');
        return sb.toString();
    }
}
