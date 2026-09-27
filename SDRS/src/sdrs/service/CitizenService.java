package sdrs.service;

import java.util.ArrayList;
import java.util.List;

import sdrs.model.Citizen;
import sdrs.model.EmergencyContact;
import sdrs.model.Evacuation;
import sdrs.util.AppException;
import sdrs.util.Validators;

public class CitizenService {

    private final DataManager dataManager;

    public CitizenService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public Citizen addCitizen(String name, String ageText, String phone, String address,
                              String locationId, String householdText, String emergencyPriority,
                              String medical, String contactName, String contactRelation,
                              String contactPhone) throws AppException {
        Validators.requireText(name, "Name");
        int age = Validators.requireInt(ageText, "Age", 0, 120);
        Validators.requirePhone(phone);
        Validators.requireText(address, "Address");
        Validators.requireInt(householdText, "Household size", 1, 30);
        Validators.requireSelection(emergencyPriority, "emergency priority");
        if (contactName != null && !contactName.trim().isEmpty()) {
            Validators.requirePhone(contactPhone);
        }
        if (phoneExists(phone, null)) {
            throw new AppException("Phone number is already registered to another citizen.");
        }
        Citizen citizen = new Citizen(dataManager.nextId("CIT"), name.trim(), age, phone.trim(),
                address.trim(), locationId, Integer.parseInt(householdText.trim()),
                emergencyPriority, medical == null ? "" : medical.trim());
        if (contactName != null && !contactName.trim().isEmpty()) {
            citizen.setEmergencyContact(new EmergencyContact(contactName.trim(), contactRelation,
                    contactPhone.trim()));
        }
        dataManager.addCitizen(citizen);
        dataManager.notifyChanged();
        return citizen;
    }

    public void updateCitizen(Citizen citizen, String name, String ageText, String phone, String address,
                              String locationId, String householdText, String emergencyPriority,
                              String medical, String contactName, String contactRelation,
                              String contactPhone) throws AppException {
        Validators.requireText(name, "Name");
        int age = Validators.requireInt(ageText, "Age", 0, 120);
        Validators.requirePhone(phone);
        Validators.requireText(address, "Address");
        Validators.requireInt(householdText, "Household size", 1, 30);
        Validators.requireSelection(emergencyPriority, "emergency priority");
        if (contactName != null && !contactName.trim().isEmpty()) {
            Validators.requirePhone(contactPhone);
        }
        if (phoneExists(phone, citizen.getId())) {
            throw new AppException("Phone number is already registered to another citizen.");
        }
        citizen.setName(name.trim());
        citizen.setAge(age);
        citizen.setPhone(phone.trim());
        citizen.setAddress(address.trim());
        citizen.setLocationId(locationId);
        citizen.setHouseholdSize(Integer.parseInt(householdText.trim()));
        citizen.setEmergencyPriority(emergencyPriority);
        citizen.setMedicalRequirement(medical == null ? "" : medical.trim());
        if (contactName != null && !contactName.trim().isEmpty()) {
            citizen.setEmergencyContact(new EmergencyContact(contactName.trim(), contactRelation,
                    contactPhone.trim()));
        }
        dataManager.notifyChanged();
    }

    public void deleteCitizen(String citizenId) throws AppException {
        dataManager.requireCitizen(citizenId);
        List<Evacuation> toRemove = new ArrayList<Evacuation>();
        List<Evacuation> evacuations = dataManager.data().getEvacuations();
        for (int i = 0; i < evacuations.size(); i++) {
            Evacuation ev = evacuations.get(i);
            if (ev.getCitizenId().equals(citizenId) && !Evacuation.STATUS_EVACUATED.equals(ev.getStatus())) {
                toRemove.add(ev);
            }
        }
        dataManager.data().getEvacuations().removeAll(toRemove);
        dataManager.removeCitizen(citizenId);
        dataManager.notifyChanged();
    }

    private boolean phoneExists(String phone, String excludeCitizenId) {
        List<Citizen> citizens = dataManager.data().getCitizens();
        for (int i = 0; i < citizens.size(); i++) {
            Citizen c = citizens.get(i);
            if (c.getPhone().equals(phone.trim()) && !c.getId().equals(excludeCitizenId)) {
                return true;
            }
        }
        return false;
    }

    public List<Citizen> allCitizens() {
        return new ArrayList<Citizen>(dataManager.data().getCitizens());
    }

    public List<Citizen> search(String idOrName, String priorityFilter, String statusFilter) {
        List<Citizen> result = new ArrayList<Citizen>();
        String needle = idOrName == null ? "" : idOrName.trim().toLowerCase();
        List<Citizen> citizens = dataManager.data().getCitizens();
        for (int i = 0; i < citizens.size(); i++) {
            Citizen c = citizens.get(i);
            if (!needle.isEmpty()
                    && !c.getName().toLowerCase().contains(needle)
                    && !c.getId().toLowerCase().contains(needle)) {
                continue;
            }
            if (priorityFilter != null && !priorityFilter.equals("All") && !c.getEmergencyPriority().equals(priorityFilter)) {
                continue;
            }
            if (statusFilter != null && !statusFilter.equals("All") && !c.getEvacuationStatus().equals(statusFilter)) {
                continue;
            }
            result.add(c);
        }
        return result;
    }
}
