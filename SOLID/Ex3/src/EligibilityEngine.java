import java.util.*;

public class EligibilityEngine {
    private final FakeEligibilityStore store;

    private final List<EligibilityRule> rules;
    public EligibilityEngine(FakeEligibilityStore store) {
        this.store = store;
        // Order matters: disciplinary, CGR, attendance, credits
        rules = Arrays.asList(
            new DisciplinaryFlagRule(),
            new CGRRule(8.0),
            new AttendanceRule(75),
            new CreditsRule(20)
        );
    }

    public void runAndPrint(StudentProfile s) {
        ReportPrinter p = new ReportPrinter();
        EligibilityEngineResult r = evaluate(s); // giant conditional inside
        p.print(s, r);
        store.save(s.rollNo, r.status);
    }

    public EligibilityEngineResult evaluate(StudentProfile s) {
        List<String> reasons = new ArrayList<>();
        for (EligibilityRule rule : rules) {
            String reason = rule.evaluate(s);
            if (reason != null) {
                reasons.add(reason);
            }
        }
        String status = reasons.isEmpty() ? "ELIGIBLE" : "NOT_ELIGIBLE";
        return new EligibilityEngineResult(status, reasons);
    }
}

class EligibilityEngineResult {
    public final String status;
    public final List<String> reasons;
    public EligibilityEngineResult(String status, List<String> reasons) {
        this.status = status;
        this.reasons = reasons;
    }
}
