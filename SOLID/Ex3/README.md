# Ex3 — OCP: Placement Eligibility Rules Engine

## 1. Context
Placement eligibility depends on multiple rules: CGR threshold, attendance percentage, earned credits, and disciplinary flags. More rules will be added later.

## 2. Current behavior
- Evaluates a `StudentProfile`
- Returns ELIGIBLE or NOT_ELIGIBLE with reasons
- Prints a report

## 3. What’s wrong with the design (at least 5 issues)
1. `EligibilityEngine.evaluate` is a long if/else chain with mixed responsibilities.
2. Adding a new rule requires editing the same method (risk of regressions).
3. Rule configuration is hard-coded.
4. Reasons formatting is mixed with evaluation.
5. Engine does persistence-ish logging via `FakeEligibilityStore`.
6. Type/flag logic is scattered.

## 4. Your task
Checkpoint A: Run and capture output.
Checkpoint B: Move each rule to its own unit (class) behind a shared abstraction.
Checkpoint C: Make it possible to add a new rule without editing the main evaluation logic.
Checkpoint D: Keep report text identical.

## 5. Constraints
- Keep `StudentProfile` fields unchanged.
- Preserve order of reasons in output.
- No external libraries.

## 6. Acceptance criteria
- New eligibility rule can be added by creating a new class and wiring it with minimal edits.
- No giant conditional chains.

## 7. How to run
```bash
cd SOLID/Ex3/src
javac *.java
java Main
```

## 8. Sample output
```text
=== Placement Eligibility ===
Student: Ayaan (CGR=8.10, attendance=72, credits=18, flag=NONE)
RESULT: NOT_ELIGIBLE
- attendance below 75
Saved evaluation for roll=23BCS1001
```

## 9. Hints (OOP-only)
- Use a list of rule objects and iterate.
- Keep rules small and single-purpose.

## 10. Stretch goals
- Read rule thresholds from a config object without editing rule logic.
