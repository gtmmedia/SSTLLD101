package com.example.reports;

/**
 * Starter demo.
 *
 * CURRENT BEHAVIOR:
 * - Everyone can open everything
 * - Disk load happens on every call
 *
 * AFTER REFACTOR:
 * - Client code should use ReportProxy
 * - Unauthorized access should be blocked
 * - Real report should load lazily and ideally once per proxy
 */
public class App {

    public static void main(String[] args) {
        User student = new User("Jasleen", "STUDENT");
        User faculty = new User("Prof. Noor", "FACULTY");
        User admin = new User("Kshitij", "ADMIN");

        ReportFile publicReport = new ReportFile("R-101", "Orientation Plan", "PUBLIC");
        ReportFile facultyReport = new ReportFile("R-202", "Midterm Review", "FACULTY");
        ReportFile adminReport = new ReportFile("R-303", "Budget Audit", "ADMIN");

        ReportViewer viewer = new ReportViewer();

        System.out.println("=== CampusVault Demo ===");

        viewer.open(publicReport, student);
        System.out.println();

        viewer.open(facultyReport, student);
        System.out.println();

        viewer.open(facultyReport, faculty);
        System.out.println();

        viewer.open(adminReport, admin);
        System.out.println();

        viewer.open(adminReport, admin);
    }
}
