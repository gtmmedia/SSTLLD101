// Disciplinary flag rule
public class DisciplinaryFlagRule implements EligibilityRule {
    @Override
    public String evaluate(StudentProfile profile) {
        if (!"NONE".equals(profile.getFlag())) {
            return "disciplinary flag: " + profile.getFlag();
        }
        return null;
    }
}
