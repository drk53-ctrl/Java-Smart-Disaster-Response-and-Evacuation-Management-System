package sdrs.model;

import java.io.Serializable;

public class Shelter implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_OPEN = "Open";
    public static final String STATUS_FULL = "Full";
    public static final String STATUS_CLOSED = "Closed";

    private String id;
    private String name;
    private String locationId;
    private String address;
    private String managerName;
    private String phone;
    private int capacity;
    private int currentOccupancy;
    private String status;

    public Shelter(String id, String name, String locationId, String address, String managerName,
                   String phone, int capacity, int currentOccupancy) {
        this.id = id;
        this.name = name;
        this.locationId = locationId;
        this.address = address;
        this.managerName = managerName;
        this.phone = phone;
        this.capacity = capacity;
        this.currentOccupancy = currentOccupancy;
        refreshStatus();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
        refreshStatus();
    }

    public int getCurrentOccupancy() {
        return currentOccupancy;
    }

    public int getAvailableCapacity() {
        return capacity - currentOccupancy;
    }

    public int admit(int people) {
        int allowed = Math.min(people, Math.max(0, capacity - currentOccupancy));
        currentOccupancy += allowed;
        refreshStatus();
        return allowed;
    }

    public int release(int people) {
        int allowed = Math.min(people, currentOccupancy);
        currentOccupancy -= allowed;
        refreshStatus();
        return allowed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private void refreshStatus() {
        if (STATUS_CLOSED.equals(status)) {
            return;
        }
        status = (currentOccupancy >= capacity) ? STATUS_FULL : STATUS_OPEN;
    }

    public double occupancyPercent() {
        if (capacity <= 0) {
            return 100.0;
        }
        return (currentOccupancy * 100.0) / capacity;
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
