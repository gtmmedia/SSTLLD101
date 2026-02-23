public interface EligibilityRule {
    /**
     * Evaluates the student profile and returns a reason if not eligible, or null if eligible.
     */
    String evaluate(StudentProfile profile);
}
