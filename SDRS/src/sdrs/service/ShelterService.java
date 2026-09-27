package sdrs.service;

import java.util.ArrayList;
import java.util.List;

import sdrs.model.Citizen;
import sdrs.model.Evacuation;
import sdrs.model.Shelter;
import sdrs.util.AppException;
import sdrs.util.Validators;

public class ShelterService {

    private final DataManager dataManager;

    public ShelterService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public Shelter addShelter(String name, String locationId, String address, String managerName,
                              String phone, String capacityText) throws AppException {
        Validators.requireText(name, "Shelter name");
        dataManager.requireLocation(locationId);
        Validators.requireText(address, "Address");
        Validators.requireText(managerName, "Manager name");
        Validators.requirePhone(phone);
        int capacity = Validators.requireInt(capacityText, "Capacity", 1, 100000);
        Shelter shelter = new Shelter(dataManager.nextId("SHL"), name.trim(), locationId, address.trim(),
                managerName.trim(), phone.trim(), capacity, 0);
        dataManager.addShelter(shelter);
        dataManager.notifyChanged();
        return shelter;
    }

    public void updateShelter(Shelter shelter, String name, String locationId, String address,
                              String managerName, String phone, String capacityText,
                              String statusText) throws AppException {
        Validators.requireText(name, "Shelter name");
        dataManager.requireLocation(locationId);
        Validators.requireText(address, "Address");
        Validators.requireText(managerName, "Manager name");
        Validators.requirePhone(phone);
        int capacity = Validators.requireInt(capacityText, "Capacity", 1, 100000);
        if (capacity < shelter.getCurrentOccupancy()) {
            throw new AppException("Capacity (" + capacity + ") cannot be less than current occupancy ("
                    + shelter.getCurrentOccupancy() + ").");
        }
        shelter.setName(name.trim());
        shelter.setLocationId(locationId);
        shelter.setAddress(address.trim());
        shelter.setManagerName(managerName.trim());
        shelter.setPhone(phone.trim());
        shelter.setCapacity(capacity);
        if (Shelter.STATUS_CLOSED.equals(statusText) && !Shelter.STATUS_CLOSED.equals(shelter.getStatus())) {
            if (shelter.getCurrentOccupancy() > 0) {
                throw new AppException("Cannot close a shelter that still houses " + shelter.getCurrentOccupancy()
                        + " people.");
            }
            shelter.setStatus(Shelter.STATUS_CLOSED);
        } else if (!Shelter.STATUS_CLOSED.equals(statusText) && Shelter.STATUS_CLOSED.equals(shelter.getStatus())) {
            shelter.setStatus(Shelter.STATUS_OPEN);
        }
        dataManager.notifyChanged();
    }

    public void deleteShelter(String shelterId) throws AppException {
        dataManager.requireShelter(shelterId);
        Shelter shelter = (Shelter) dataManager.shelterById(shelterId);
        if (shelter.getCurrentOccupancy() > 0) {
            throw new AppException("Cannot delete a shelter that currently houses " + shelter.getCurrentOccupancy()
                    + " people. Move them first.");
        }
        dataManager.pushDeleted(shelter);
        dataManager.removeShelter(shelterId);
        dataManager.notifyChanged();
    }

    public void adjustOccupancy(String shelterId, String deltaText) throws AppException {
        dataManager.requireShelter(shelterId);
        Shelter shelter = (Shelter) dataManager.shelterById(shelterId);
        int delta = Validators.requireIntAny(deltaText, "Occupancy change");
        int newOccupancy = shelter.getCurrentOccupancy() + delta;
        if (newOccupancy < 0) {
            throw new AppException("Occupancy cannot go below zero.");
        }
        if (newOccupancy > shelter.getCapacity()) {
            throw new AppException("That would exceed capacity of " + shelter.getCapacity() + ".");
        }
        shelter.admit(delta);
        dataManager.notifyChanged();
    }

    public List<Shelter> allShelters() {
        return new ArrayList<Shelter>(dataManager.data().getShelters());
    }

    public List<Shelter> search(String keyword, String statusFilter, boolean onlyAvailable) {
        List<Shelter> result = new ArrayList<Shelter>();
        String needle = keyword == null ? "" : keyword.trim().toLowerCase();
        List<Shelter> shelters = dataManager.data().getShelters();
        for (int i = 0; i < shelters.size(); i++) {
            Shelter s = shelters.get(i);
            if (!needle.isEmpty() && !s.getName().toLowerCase().contains(needle)
                    && !s.getId().toLowerCase().contains(needle)) {
                continue;
            }
            if (statusFilter != null && !statusFilter.equals("All") && !s.getStatus().equals(statusFilter)) {
                continue;
            }
            if (onlyAvailable && (s.getAvailableCapacity() <= 0 || Shelter.STATUS_CLOSED.equals(s.getStatus()))) {
                continue;
            }
            result.add(s);
        }
        return result;
    }

    public void registerCitizenAtShelter(String shelterId, String citizenId) throws AppException {
        dataManager.requireShelter(shelterId);
        dataManager.requireCitizen(citizenId);
        Shelter shelter = (Shelter) dataManager.shelterById(shelterId);
        Citizen citizen = (Citizen) dataManager.citizenById(citizenId);
        if (Shelter.STATUS_CLOSED.equals(shelter.getStatus())) {
            throw new AppException("This shelter is closed.");
        }
        if (shelter.getAvailableCapacity() < citizen.getHouseholdSize()) {
            throw new AppException("Only " + shelter.getAvailableCapacity() + " place(s) free; household needs "
                    + citizen.getHouseholdSize() + ".");
        }
        shelter.admit(citizen.getHouseholdSize());
        dataManager.notifyChanged();
    }

    public List<Citizen> citizensAtShelter(String shelterId) {
        List<Citizen> result = new ArrayList<Citizen>();
        List<Evacuation> evacuations = dataManager.data().getEvacuations();
        for (int i = 0; i < evacuations.size(); i++) {
            Evacuation ev = evacuations.get(i);
            if (shelterId.equals(ev.getShelterId())) {
                Citizen citizen = (Citizen) dataManager.citizenById(ev.getCitizenId());
                if (citizen != null) {
                    result.add(citizen);
                }
            }
        }
        return result;
    }
}
