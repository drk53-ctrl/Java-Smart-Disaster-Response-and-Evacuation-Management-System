package sdrs.model;

import java.io.Serializable;

public class RoadSegment implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_OPEN = "Open";
    public static final String STATUS_BLOCKED = "Blocked";

    private String fromId;
    private String toId;
    private double distanceKm;
    private double travelMinutes;
    private String status;
    private String name;

    public RoadSegment(String fromId, String toId, double distanceKm, double travelMinutes, String name) {
        this.fromId = fromId;
        this.toId = toId;
        this.distanceKm = distanceKm;
        this.travelMinutes = travelMinutes;
        this.status = STATUS_OPEN;
        this.name = name;
    }

    public String getFromId() {
        return fromId;
    }

    public String getToId() {
        return toId;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getTravelMinutes() {
        return travelMinutes;
    }

    public void setTravelMinutes(double travelMinutes) {
        this.travelMinutes = travelMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double cost(boolean byTime) {
        return byTime ? travelMinutes : distanceKm;
    }

    public boolean isBlocked() {
        return STATUS_BLOCKED.equals(status);
    }

    public String otherEnd(String nodeId) {
        if (fromId.equals(nodeId)) {
            return toId;
        }
        if (toId.equals(nodeId)) {
            return fromId;
        }
        return null;
    }
}
