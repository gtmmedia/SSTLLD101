// CGR threshold rule
public class CGRRule implements EligibilityRule {
    private final double threshold;

    public CGRRule(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public String evaluate(StudentProfile profile) {
        if (profile.getCgr() < threshold) {
            return "CGR below " + threshold;
        }
        return null;
    }
}
