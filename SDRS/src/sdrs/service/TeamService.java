package sdrs.service;

import sdrs.model.Disaster;
import sdrs.model.EmergencyTeam;
import sdrs.util.AppException;
import sdrs.util.Validators;

public class TeamService {

    private final DataManager dataManager;

    public TeamService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public EmergencyTeam addTeam(String name, String type, String memberText, String leaderName,
                                 String locationId) throws AppException {
        Validators.requireText(name, "Team name");
        Validators.requireSelection(type, "team type");
        Validators.requireInt(memberText, "Member count", 1, 500);
        Validators.requireText(leaderName, "Leader name");
        dataManager.requireLocation(locationId);
        EmergencyTeam team = new EmergencyTeam(dataManager.nextId("TEAM"), name.trim(), type,
                Integer.parseInt(memberText.trim()), leaderName.trim(), locationId);
        dataManager.addTeam(team);
        dataManager.notifyChanged();
        return team;
    }

    public void updateTeam(EmergencyTeam team, String name, String type, String memberText,
                           String leaderName, String locationId) throws AppException {
        Validators.requireText(name, "Team name");
        Validators.requireSelection(type, "team type");
        Validators.requireInt(memberText, "Member count", 1, 500);
        Validators.requireText(leaderName, "Leader name");
        dataManager.requireLocation(locationId);
        team.setName(name.trim());
        team.setType(type);
        team.setMemberCount(Integer.parseInt(memberText.trim()));
        team.setLeaderName(leaderName.trim());
        team.setLocation(locationId);
        dataManager.notifyChanged();
    }

    public void deleteTeam(String teamId) throws AppException {
        dataManager.requireTeam(teamId);
        EmergencyTeam team = (EmergencyTeam) dataManager.teamById(teamId);
        if (team.getAssignedDisasterId() != null) {
            throw new AppException("Team is currently assigned to a disaster. Release it first.");
        }
        dataManager.pushDeleted(team);
        dataManager.removeTeam(teamId);
        dataManager.notifyChanged();
    }

    public void assignToDisaster(String teamId, String disasterId) throws AppException {
        dataManager.requireTeam(teamId);
        dataManager.requireDisaster(disasterId);
        EmergencyTeam team = (EmergencyTeam) dataManager.teamById(teamId);
        Disaster disaster = (Disaster) dataManager.disasterById(disasterId);
        if (!team.isAvailable()) {
            throw new AppException(team.getName() + " is not available (already on "
                    + team.getAssignedDisasterId() + ").");
        }
        if (!disaster.isActiveLike()) {
            throw new AppException("Teams can only be assigned to an active disaster.");
        }
        team.setAvailable(false);
        team.setAssignedDisasterId(disasterId);
        if (!disaster.getTeamIds().contains(teamId)) {
            disaster.getTeamIds().add(teamId);
        }
        if (Disaster.STATUS_REPORTED.equals(disaster.getStatus())) {
            disaster.setStatus(Disaster.STATUS_RESPONSE);
        }
        dataManager.notifyChanged();
    }

    public void releaseFromDisaster(String teamId) throws AppException {
        dataManager.requireTeam(teamId);
        EmergencyTeam team = (EmergencyTeam) dataManager.teamById(teamId);
        if (team.isAvailable()) {
            throw new AppException("Team is not assigned to any disaster.");
        }
        Disaster disaster = (Disaster) dataManager.disasterById(team.getAssignedDisasterId());
        if (disaster != null) {
            disaster.getTeamIds().remove(teamId);
        }
        team.setAvailable(true);
        team.setAssignedDisasterId(null);
        dataManager.notifyChanged();
    }

    public void setAvailable(String teamId, boolean available) throws AppException {
        dataManager.requireTeam(teamId);
        EmergencyTeam team = (EmergencyTeam) dataManager.teamById(teamId);
        if (!available && team.getAssignedDisasterId() != null) {
            throw new AppException("Team is on an assignment; release it before marking unavailable.");
        }
        team.setAvailable(available);
        dataManager.notifyChanged();
    }

    public java.util.List<EmergencyTeam> allTeams() {
        return new java.util.ArrayList<EmergencyTeam>(dataManager.data().getTeams());
    }

    public java.util.List<EmergencyTeam> search(String keyword, String typeFilter, String availabilityFilter) {
        java.util.List<EmergencyTeam> result = new java.util.ArrayList<EmergencyTeam>();
        String needle = keyword == null ? "" : keyword.trim().toLowerCase();
        java.util.List<EmergencyTeam> teams = dataManager.data().getTeams();
        for (int i = 0; i < teams.size(); i++) {
            EmergencyTeam t = teams.get(i);
            if (!needle.isEmpty() && !t.getName().toLowerCase().contains(needle)
                    && !t.getId().toLowerCase().contains(needle)
                    && !t.getLeaderName().toLowerCase().contains(needle)) {
                continue;
            }
            if (typeFilter != null && !typeFilter.equals("All") && !t.getType().equals(typeFilter)) {
                continue;
            }
            if ("Available".equals(availabilityFilter) && !t.isAvailable()) {
                continue;
            }
            if ("Assigned".equals(availabilityFilter) && t.isAvailable()) {
                continue;
            }
            result.add(t);
        }
        return result;
    }
}
