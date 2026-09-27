package sdrs.model;

import java.io.Serializable;

public class EmergencyTeam implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_MEDICAL = "Medical";
    public static final String TYPE_FIRE_RESCUE = "Fire & Rescue";
    public static final String TYPE_POLICE = "Police";
    public static final String TYPE_SEARCH_RESCUE = "Search & Rescue";
    public static final String TYPE_DISASTER_RESPONSE = "Disaster Response";

    private String id;
    private String name;
    private String type;
    private int memberCount;
    private String leaderName;
    private String locationId;
    private boolean available;
    private String assignedDisasterId;

    public EmergencyTeam(String id, String name, String type, int memberCount, String leaderName,
                         String locationId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.memberCount = memberCount;
        this.leaderName = leaderName;
        this.locationId = locationId;
        this.available = true;
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

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public void setLocation(String locationId) {
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
        return id + " - " + name;
    }
}
