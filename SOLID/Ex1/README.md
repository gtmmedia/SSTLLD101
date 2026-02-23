# Ex1 — SRP: Student Onboarding Registration

## 1. Context
You are building a simple onboarding flow for new students. The system accepts a raw input string, validates fields, generates a student ID, saves to a store, and prints a confirmation.

## 2. Current behavior (what it does today)
- Parses a raw line like: `name=Riya;email=riya@sst.edu;phone=9876543210;program=CSE`
- Validates basic rules (non-empty, email contains `@`, phone digits, program allowed)
- Generates an ID like `SST-2026-0001`
- Saves the student record to an in-memory “DB”
- Prints a confirmation block and a small table dump

## 3. What’s wrong with the design (at least 5 issues)
1. `OnboardingService` mixes parsing, validation, ID generation, persistence, and printing.
2. Hard-coded program rules inside the same method as IO/printing.
3. Validation errors are printed directly instead of being represented cleanly.
4. Persistence is coupled to a specific `FakeDb` and direct calls.
5. Output formatting is mixed with business logic, making changes risky.
6. Utility logic is scattered (some in `IdUtil`, some inline).
7. Hard to unit test because everything runs inside one “do-it-all” method.

## 4. Your task (step-by-step refactor plan with checkpoints)
Checkpoint A:
- Run the program and capture output.
- Identify responsibilities currently inside `OnboardingService.registerFromRawInput`.

Checkpoint B:
- Extract parsing to a dedicated class (or method group) with a clear input/output.
- Keep behavior identical.

Checkpoint C:
- Extract validation rules into a separate component.
- Ensure error messages and order remain unchanged.

Checkpoint D:
- Decouple persistence from the onboarding flow (introduce an interface for saving).
- Keep `FakeDb` as an implementation.

Checkpoint E:
- Extract printing/formatting responsibilities away from the onboarding workflow.
- Preserve exact console output.

## 5. Constraints
- Keep `Main` output exactly the same.
- Keep `StudentRecord` fields and `toString()` unchanged.
- No external libraries.
- Default package only.

## 6. Acceptance criteria
- Program output is unchanged.
- `OnboardingService` no longer directly formats output and no longer directly knows `FakeDb`.
- Validation rules are testable without console IO.
- Adding a new field (e.g., `city`) should not require editing a “god method”.

## 7. How to run
```bash
cd SOLID/Ex1/src
javac *.java
java Main
```

## 8. Sample output (must match)
```text
=== Student Onboarding ===
INPUT: name=Riya;email=riya@sst.edu;phone=9876543210;program=CSE
OK: created student SST-2026-0001
Saved. Total students: 1
CONFIRMATION:
StudentRecord{id='SST-2026-0001', name='Riya', email='riya@sst.edu', phone='9876543210', program='CSE'}

-- DB DUMP --
| ID             | NAME | PROGRAM |
| SST-2026-0001   | Riya | CSE     |
```

## 9. Hints (OOP-only)
- Prefer passing structured data between steps rather than re-parsing strings.
- Prefer composition: onboarding workflow can *use* a parser/validator/saver/printer.
- Keep public APIs stable; move details behind small interfaces.

## 10. Stretch goals
- Add a second input example that fails validation, without duplicating logic.
- Make program list configurable without touching onboarding workflow code.
