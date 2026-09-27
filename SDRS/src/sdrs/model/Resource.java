package sdrs.model;

import java.io.Serializable;

public class Resource implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_FOOD = "Food";
    public static final String TYPE_WATER = "Water";
    public static final String TYPE_MEDICAL_KIT = "Medical Kit";
    public static final String TYPE_BLANKET = "Blanket";
    public static final String TYPE_RESCUE_EQUIPMENT = "Rescue Equipment";
    public static final String TYPE_FUEL = "Fuel";
    public static final String TYPE_OTHER = "Other";

    private String id;
    private String name;
    private String type;
    private int quantity;
    private String locationId;
    private String unit;

    public Resource(String id, String name, String type, int quantity, String locationId, String unit) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.locationId = locationId;
        this.unit = unit;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String statusLabel() {
        if (quantity <= 0) {
            return "Out of Stock";
        }
        if (quantity < 50) {
            return "Low Stock";
        }
        return "Available";
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
