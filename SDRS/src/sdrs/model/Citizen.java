package sdrs.model;

import java.util.ArrayList;
import java.util.List;

public class Citizen extends Person implements Prioritizable {

    private static final long serialVersionUID = 1L;

    public static final String PRIORITY_LOW = "Low";
    public static final String PRIORITY_MEDIUM = "Medium";
    public static final String PRIORITY_HIGH = "High";
    public static final String PRIORITY_CRITICAL = "Critical";

    public static final String EV_PENDING = "Pending";
    public static final String EV_ASSIGNED = "Assigned";
    public static final String EV_EVACUATING = "Evacuating";
    public static final String EV_EVACUATED = "Evacuated";

    private int age;
    private String address;
    private String locationId;
    private int householdSize;
    private String emergencyPriority;
    private String medicalRequirement;
    private String evacuationStatus;
    private List<String> disasterIds = new ArrayList<String>();
    private EmergencyContact emergencyContact;

    public Citizen(String id, String name, int age, String phone, String address, String locationId,
                   int householdSize, String emergencyPriority, String medicalRequirement) {
        super(id, name, phone);
        this.age = age;
        this.address = address;
        this.locationId = locationId;
        this.householdSize = householdSize;
        this.emergencyPriority = emergencyPriority;
        this.medicalRequirement = medicalRequirement;
        this.evacuationStatus = EV_PENDING;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public int getHouseholdSize() {
        return householdSize;
    }

    public void setHouseholdSize(int householdSize) {
        this.householdSize = householdSize;
    }

    public String getEmergencyPriority() {
        return emergencyPriority;
    }

    public void setEmergencyPriority(String emergencyPriority) {
        this.emergencyPriority = emergencyPriority;
    }

    public String getMedicalRequirement() {
        return medicalRequirement;
    }

    public void setMedicalRequirement(String medicalRequirement) {
        this.medicalRequirement = medicalRequirement;
    }

    public String getEvacuationStatus() {
        return evacuationStatus;
    }

    public void setEvacuationStatus(String evacuationStatus) {
        this.evacuationStatus = evacuationStatus;
    }

    public List<String> getDisasterIds() {
        return disasterIds;
    }

    public EmergencyContact getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(EmergencyContact emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    @Override
    public int getPriorityScore() {
        int score = 0;
        if (PRIORITY_CRITICAL.equals(emergencyPriority)) {
            score += 40;
        } else if (PRIORITY_HIGH.equals(emergencyPriority)) {
            score += 25;
        } else if (PRIORITY_MEDIUM.equals(emergencyPriority)) {
            score += 10;
        }
        if (medicalRequirement != null && !medicalRequirement.isEmpty()) {
            score += 20;
        }
        if (age <= 12 || age >= 65) {
            score += 15;
        }
        if (householdSize >= 5) {
            score += 5;
        }
        return score;
    }

    @Override
    public String toString() {
        return getId() + " - " + getName();
    }
}
