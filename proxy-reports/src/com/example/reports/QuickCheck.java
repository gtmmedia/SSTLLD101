package com.example.reports;

public class QuickCheck {

    public static void main(String[] args) {
        User student = new User("Riya", "STUDENT");
        User admin = new User("Kshitij", "ADMIN");

        Report adminReport = new ReportProxy("R-303", "Budget Audit", "ADMIN");
        Report facultyReport = new ReportProxy("R-202", "Midterm Review", "FACULTY");

        System.out.println("=== QuickCheck ===");
        facultyReport.display(student);
        System.out.println();
        adminReport.display(admin);
        System.out.println();
        adminReport.display(admin);
    }
}
