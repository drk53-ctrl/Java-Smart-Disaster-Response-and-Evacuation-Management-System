package sdrs.model;

import java.util.ArrayList;
import java.util.List;

public class Disaster implements Prioritizable, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TYPE_FLOOD = "Flood";
    public static final String TYPE_EARTHQUAKE = "Earthquake";
    public static final String TYPE_CYCLONE = "Cyclone";
    public static final String TYPE_FIRE = "Fire";
    public static final String TYPE_LANDSLIDE = "Landslide";
    public static final String TYPE_TSUNAMI = "Tsunami";
    public static final String TYPE_OTHER = "Other";

    public static final String SEVERITY_LOW = "Low";
    public static final String SEVERITY_MEDIUM = "Medium";
    public static final String SEVERITY_HIGH = "High";
    public static final String SEVERITY_CRITICAL = "Critical";

    public static final String STATUS_REPORTED = "Reported";
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_RESPONSE = "Under Response";
    public static final String STATUS_RESOLVED = "Resolved";

    private String id;
    private String type;
    private String locationName;
    private String locationId;
    private String severity;
    private long reportedAt;
    private String status;
    private int affectedPeople;
    private String description;
    private List<String> teamIds = new ArrayList<String>();
    private List<String> vehicleIds = new ArrayList<String>();
    private List<String> citizenIds = new ArrayList<String>();

    public Disaster(String id, String type, String locationName, String locationId, String severity,
                    long reportedAt, int affectedPeople, String description) {
        this.id = id;
        this.type = type;
        this.locationName = locationName;
        this.locationId = locationId;
        this.severity = severity;
        this.reportedAt = reportedAt;
        this.status = STATUS_REPORTED;
        this.affectedPeople = affectedPeople;
        this.description = description;
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public long getReportedAt() {
        return reportedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAffectedPeople() {
        return affectedPeople;
    }

    public void setAffectedPeople(int affectedPeople) {
        this.affectedPeople = affectedPeople;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTeamIds() {
        return teamIds;
    }

    public List<String> getVehicleIds() {
        return vehicleIds;
    }

    public List<String> getCitizenIds() {
        return citizenIds;
    }

    public boolean isActiveLike() {
        return STATUS_REPORTED.equals(status) || STATUS_ACTIVE.equals(status)
                || STATUS_RESPONSE.equals(status);
    }

    @Override
    public int getPriorityScore() {
        int score = 0;
        if (SEVERITY_CRITICAL.equals(severity)) {
            score += 60;
        } else if (SEVERITY_HIGH.equals(severity)) {
            score += 40;
        } else if (SEVERITY_MEDIUM.equals(severity)) {
            score += 20;
        }
        if (affectedPeople > 1000) {
            score += 25;
        } else if (affectedPeople > 500) {
            score += 20;
        } else if (affectedPeople > 100) {
            score += 15;
        } else if (affectedPeople > 0) {
            score += 5;
        }
        if (STATUS_REPORTED.equals(status)) {
            score += 15;
        }
        if (TYPE_TSUNAMI.equals(type) || TYPE_EARTHQUAKE.equals(type)) {
            score += 10;
        }
        return score;
    }

    @Override
    public String toString() {
        return id + " - " + type + " at " + locationName;
    }
}
