package sdrs.algorithm;

public class RouteStep {

    private final String locationId;
    private final String locationName;
    private final double distanceKm;
    private final double travelMinutes;

    public RouteStep(String locationId, String locationName, double distanceKm, double travelMinutes) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.distanceKm = distanceKm;
        this.travelMinutes = travelMinutes;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getTravelMinutes() {
        return travelMinutes;
    }

    @Override
    public String toString() {
        return locationName + " (" + String.format("%.1f", distanceKm) + " km, "
                + String.format("%.0f", travelMinutes) + " min)";
    }
}
