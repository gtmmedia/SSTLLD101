// Credits earned rule
public class CreditsRule implements EligibilityRule {
    private final int minCredits;

    public CreditsRule(int minCredits) {
        this.minCredits = minCredits;
    }

    @Override
    public String evaluate(StudentProfile profile) {
        if (profile.getCredits() < minCredits) {
            return "credits below " + minCredits;
        }
        return null;
    }
}
