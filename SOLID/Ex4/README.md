# Ex4 — OCP: Hostel Fee Calculator

## 1. Context
Hostel fees depend on room type and add-ons. New room types and add-ons will be introduced.

## 2. Current behavior
- Uses `BookingRequest` with roomType and add-ons
- Calculates monthly fee + one-time deposit
- Prints a receipt and saves booking

## 3. What’s wrong (at least 5 issues)
1. `HostelFeeCalculator.calculate` is a switch-case on room types.
2. Add-ons are handled with repeated branching logic.
3. Adding a room type requires editing the big switch (OCP violation).
4. Money arithmetic is scattered and formatted inconsistently.
5. Calculator also prints and persists booking data.

## 4. Your task
Checkpoint A: Run and capture output.
Checkpoint B: Encapsulate room pricing and add-on pricing behind abstractions.
Checkpoint C: Remove switch-case from main calculation logic.
Checkpoint D: Preserve output.

## 5. Constraints
- Keep receipt formatting identical.
- Keep `BookingRequest` fields unchanged.
- No external libs.

## 6. Acceptance criteria
- New room type can be added without editing a switch in calculator.
- Add-ons can be added without editing the core fee algorithm.

## 7. How to run
```bash
cd SOLID/Ex4/src
javac *.java
java Main
```

## 8. Sample output
```text
=== Hostel Fee Calculator ===
Room: DOUBLE | AddOns: [LAUNDRY, MESS]
Monthly: 16000.00
Deposit: 5000.00
TOTAL DUE NOW: 21000.00
Saved booking: H-7781
```

## 9. Hints (OOP-only)
- Prefer a list of pricing components (room + add-ons) that contribute to totals.
- Keep printing separate from calculation.

## 10. Stretch goals
- Add “late fee” rule without editing the main calculation loop.
