package si.um.feri.cestar.Utils;

public class LocationInfo {

    private String locationName;
    private double latitude;
    private String address;
    private double longitude;
    private int connectors; // Total connectors
    private int connectorsAvailable; // Available connectors

    public LocationInfo(String locationName, double latitude, double longitude, int connectors, int connectorsAvailable,String address) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.connectors = connectors;
        this.connectorsAvailable = connectorsAvailable;
        this.address = address;
    }

    // Getter for locationName
    public String getLocationName() {
        return locationName;
    }

    // Setter for locationName
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // Getter for latitude
    public double getLatitude() {
        return latitude;
    }

    // Setter for latitude
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    // Getter for longitude
    public double getLongitude() {
        return longitude;
    }

    // Setter for longitude
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // Getter for connectors
    public int getConnectors() {
        return connectors;
    }

    // Setter for connectors
    public void setConnectors(int connectors) {
        this.connectors = connectors;
    }

    // Getter for connectorsAvailable
    public int getConnectorsAvailable() {
        return connectorsAvailable;
    }

    // Setter for connectorsAvailable
    public void setConnectorsAvailable(int connectorsAvailable) {
        this.connectorsAvailable = connectorsAvailable;
    }

    @Override
    public String toString() {
        return "LocationInfo{" +
            "locationName='" + locationName + '\'' +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", connectors=" + connectors +
            ", connectorsAvailable=" + connectorsAvailable +
            '}';
    }

}
