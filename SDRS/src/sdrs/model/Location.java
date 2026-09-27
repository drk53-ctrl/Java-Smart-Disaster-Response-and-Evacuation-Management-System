package sdrs.model;

import java.io.Serializable;

public class Location implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_DISASTER_ZONE = "Disaster Zone";
    public static final String TYPE_SHELTER = "Shelter";
    public static final String TYPE_HOSPITAL = "Hospital";
    public static final String TYPE_FIRE_STATION = "Fire Station";
    public static final String TYPE_POLICE_STATION = "Police Station";
    public static final String TYPE_EMERGENCY_CENTER = "Emergency Center";
    public static final String TYPE_JUNCTION = "Junction";

    public static final double MAP_MIN = 0.0;
    public static final double MAP_MAX = 100.0;

    private String id;
    private String name;
    private String type;
    private double x = -1.0;
    private double y = -1.0;

    public Location(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public Location(String id, String name, String type, double x, double y) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.x = clamp(x);
        this.y = clamp(y);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = clamp(x);
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = clamp(y);
    }

    public boolean hasMapPosition() {
        return x >= 0 && y >= 0;
    }

    private double clamp(double value) {
        if (value < MAP_MIN) {
            return MAP_MIN;
        }
        if (value > MAP_MAX) {
            return MAP_MAX;
        }
        return value;
    }

    @Override
    public String toString() {
        return name + " [" + type + "]";
    }
}
