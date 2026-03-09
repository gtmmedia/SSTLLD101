package com.example.reports;

public class AccessControl {

    public boolean canAccess(User user, String classification) {
        String role = user.getRole();

        if ("PUBLIC".equals(classification)) return true;
        if ("FACULTY".equals(classification)) {
            return "FACULTY".equals(role) || "ADMIN".equals(role);
        }
        if ("ADMIN".equals(classification)) {
            return "ADMIN".equals(role);
        }
        return false;
    }
}
