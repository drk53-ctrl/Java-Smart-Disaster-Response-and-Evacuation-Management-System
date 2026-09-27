package sdrs.model;

import java.io.Serializable;

public class Vehicle implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_AMBULANCE = "Ambulance";
    public static final String TYPE_FIRE_TRUCK = "Fire Truck";
    public static final String TYPE_RESCUE = "Rescue Vehicle";
    public static final String TYPE_SUPPLY = "Supply Vehicle";

    private String id;
    private String type;
    private String plateNumber;
    private String driverName;
    private String locationId;
    private boolean available;
    private String assignedDisasterId;

    public Vehicle(String id, String type, String plateNumber, String driverName, String locationId) {
        this.id = id;
        this.type = type;
        this.plateNumber = plateNumber;
        this.driverName = driverName;
        this.locationId = locationId;
        this.available = true;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setDriverNameOnly(String driverName) {
        this.driverName = driverName;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getAssignedDisasterId() {
        return assignedDisasterId;
    }

    public void setAssignedDisasterId(String assignedDisasterId) {
        this.assignedDisasterId = assignedDisasterId;
    }

    @Override
    public String toString() {
        return id + " - " + type;
    }
}
