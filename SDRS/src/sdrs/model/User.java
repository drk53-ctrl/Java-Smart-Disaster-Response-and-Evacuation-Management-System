package sdrs.model;

import sdrs.util.PasswordUtil;

public class User extends Person {

    private static final long serialVersionUID = 1L;

    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_DMO = "Disaster Management Officer";
    public static final String ROLE_RO = "Response Officer";

    private String username;
    private String passwordHash;
    private String role;

    public User(String id, String username, String passwordHash, String name, String phone, String role) {
        super(id, name, phone);
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPassword(String plainPassword) {
        this.passwordHash = PasswordUtil.hash(plainPassword);
    }

    public boolean checkPassword(String plainPassword) {
        return PasswordUtil.verify(plainPassword, passwordHash);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    public boolean isOfficer() {
        return ROLE_DMO.equals(role);
    }

    public boolean isResponseOfficer() {
        return ROLE_RO.equals(role);
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
