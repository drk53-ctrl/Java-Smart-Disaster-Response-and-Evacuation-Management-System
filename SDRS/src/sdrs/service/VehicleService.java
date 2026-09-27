package sdrs.service;

import java.util.ArrayList;
import java.util.List;

import sdrs.model.Disaster;
import sdrs.model.Vehicle;
import sdrs.util.AppException;
import sdrs.util.Validators;

public class VehicleService {

    private final DataManager dataManager;

    public VehicleService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public Vehicle addVehicle(String type, String plateNumber, String driverName,
                              String locationId) throws AppException {
        Validators.requireSelection(type, "vehicle type");
        Validators.requireText(plateNumber, "Plate number");
        Validators.requireText(driverName, "Driver name");
        dataManager.requireLocation(locationId);
        List<Vehicle> vehicles = dataManager.data().getVehicles();
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getPlateNumber().equalsIgnoreCase(plateNumber.trim())) {
                throw new AppException("A vehicle with this plate number already exists.");
            }
        }
        Vehicle vehicle = new Vehicle(dataManager.nextId("VEH"), type, plateNumber.trim().toUpperCase(),
                driverName.trim(), locationId);
        dataManager.addVehicle(vehicle);
        dataManager.notifyChanged();
        return vehicle;
    }

    public void updateVehicle(Vehicle vehicle, String type, String plateNumber, String driverName,
                              String locationId) throws AppException {
        Validators.requireSelection(type, "vehicle type");
        Validators.requireText(plateNumber, "Plate number");
        Validators.requireText(driverName, "Driver name");
        dataManager.requireLocation(locationId);
        List<Vehicle> vehicles = dataManager.data().getVehicles();
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle other = vehicles.get(i);
            if (!other.getId().equals(vehicle.getId())
                    && other.getPlateNumber().equalsIgnoreCase(plateNumber.trim())) {
                throw new AppException("A vehicle with this plate number already exists.");
            }
        }
        vehicle.setType(type);
        vehicle.setPlateNumber(plateNumber.trim().toUpperCase());
        vehicle.setDriverNameOnly(driverName.trim());
        vehicle.setLocationId(locationId);
        dataManager.notifyChanged();
    }

    public void deleteVehicle(String vehicleId) throws AppException {
        dataManager.requireVehicle(vehicleId);
        Vehicle vehicle = (Vehicle) dataManager.vehicleById(vehicleId);
        if (vehicle.getAssignedDisasterId() != null) {
            throw new AppException("Vehicle is currently assigned to a disaster. Release it first.");
        }
        dataManager.pushDeleted(vehicle);
        dataManager.removeVehicle(vehicleId);
        dataManager.notifyChanged();
    }

    public void assignToDisaster(String vehicleId, String disasterId) throws AppException {
        dataManager.requireVehicle(vehicleId);
        dataManager.requireDisaster(disasterId);
        Vehicle vehicle = (Vehicle) dataManager.vehicleById(vehicleId);
        Disaster disaster = (Disaster) dataManager.disasterById(disasterId);
        if (!vehicle.isAvailable()) {
            throw new AppException(vehicle.getType() + " " + vehicle.getPlateNumber()
                    + " is not available (already on " + vehicle.getAssignedDisasterId() + ").");
        }
        if (!disaster.isActiveLike()) {
            throw new AppException("Vehicles can only be assigned to an active disaster.");
        }
        vehicle.setAvailable(false);
        vehicle.setAssignedDisasterId(disasterId);
        if (!disaster.getVehicleIds().contains(vehicleId)) {
            disaster.getVehicleIds().add(vehicleId);
        }
        dataManager.notifyChanged();
    }

    public void releaseFromDisaster(String vehicleId) throws AppException {
        dataManager.requireVehicle(vehicleId);
        Vehicle vehicle = (Vehicle) dataManager.vehicleById(vehicleId);
        if (vehicle.isAvailable()) {
            throw new AppException("Vehicle is not assigned to any disaster.");
        }
        Disaster disaster = (Disaster) dataManager.disasterById(vehicle.getAssignedDisasterId());
        if (disaster != null) {
            disaster.getVehicleIds().remove(vehicleId);
        }
        vehicle.setAvailable(true);
        vehicle.setAssignedDisasterId(null);
        dataManager.notifyChanged();
    }

    public List<Vehicle> allVehicles() {
        return new ArrayList<Vehicle>(dataManager.data().getVehicles());
    }

    public List<Vehicle> search(String keyword, String typeFilter, String availabilityFilter) {
        List<Vehicle> result = new ArrayList<Vehicle>();
        String needle = keyword == null ? "" : keyword.trim().toLowerCase();
        List<Vehicle> vehicles = dataManager.data().getVehicles();
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle v = vehicles.get(i);
            if (!needle.isEmpty() && !v.getPlateNumber().toLowerCase().contains(needle)
                    && !v.getId().toLowerCase().contains(needle)
                    && !v.getDriverName().toLowerCase().contains(needle)) {
                continue;
            }
            if (typeFilter != null && !typeFilter.equals("All") && !v.getType().equals(typeFilter)) {
                continue;
            }
            if ("Available".equals(availabilityFilter) && !v.isAvailable()) {
                continue;
            }
            if ("Assigned".equals(availabilityFilter) && v.isAvailable()) {
                continue;
            }
            result.add(v);
        }
        return result;
    }
}
