// Attendance percentage rule
public class AttendanceRule implements EligibilityRule {
    private final int minAttendance;

    public AttendanceRule(int minAttendance) {
        this.minAttendance = minAttendance;
    }

    @Override
    public String evaluate(StudentProfile profile) {
        if (profile.getAttendance() < minAttendance) {
            return "attendance below " + minAttendance;
        }
        return null;
    }
}
