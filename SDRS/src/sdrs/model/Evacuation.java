package sdrs.model;

import java.io.Serializable;

public class Evacuation implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_ASSIGNED = "Assigned";
    public static final String STATUS_EVACUATING = "Evacuating";
    public static final String STATUS_EVACUATED = "Evacuated";

    private String id;
    private String citizenId;
    private String disasterId;
    private String shelterId;
    private int peopleCount;
    private String status;
    private long createdAt;
    private String notes;

    public Evacuation(String id, String citizenId, String disasterId, int peopleCount, long createdAt) {
        this.id = id;
        this.citizenId = citizenId;
        this.disasterId = disasterId;
        this.peopleCount = peopleCount;
        this.status = STATUS_PENDING;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }

    public String getDisasterId() {
        return disasterId;
    }

    public void setDisasterId(String disasterId) {
        this.disasterId = disasterId;
    }

    public String getShelterId() {
        return shelterId;
    }

    public void setShelterId(String shelterId) {
        this.shelterId = shelterId;
    }

    public int getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(int peopleCount) {
        this.peopleCount = peopleCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
