package si.um.feri.cestar.Utils;

public class Geolocation {
    private static final double EARTH_RADIUS = 6371e3;
    public double lat;
    public double lng;

    public Geolocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }
    public void setLatitude(double lat) {
        this.lat = lat;
    }

    public void setLongitude(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public static double distanceBetween(Geolocation start, Geolocation end) {
        double lat1Rad = Math.toRadians(start.lat);
        double lat2Rad = Math.toRadians(end.lat);
        double deltaLatRad = Math.toRadians(end.lat - start.lat);
        double deltaLngRad = Math.toRadians(end.lng - start.lng);

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
            Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // in meters
    }
}
