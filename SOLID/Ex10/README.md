# Ex10 — DIP: Campus Transport Booking

## 1. Context
A campus transport service books rides for students. It calculates distance, allocates a driver, and charges payment.

## 2. Current behavior
- `TransportBookingService.book` directly creates concrete `PaymentGateway`, `DriverAllocator`, `DistanceCalculator`
- Prints receipt

## 3. What’s wrong (at least 5 issues)
1. High-level booking logic depends on concrete services (hard-coded `new`).
2. Hard to test booking without real dependencies.
3. Hard to add a new payment method without editing booking logic.
4. Business rules (pricing) mixed with infrastructure calls.
5. No clear abstraction boundaries.

## 4. Your task
- Introduce abstractions and inject them into booking service.
- Preserve output.

## 5. Constraints
- Preserve receipt output format.
- Keep `TripRequest` fields unchanged.
- No external libs.

## 6. Acceptance criteria
- Booking service depends only on abstractions.
- Concrete implementations can be swapped without editing booking logic.

## 7. How to run
```bash
cd SOLID/Ex10/src
javac *.java
java Main
```

## 8. Sample output
```text
=== Transport Booking ===
DistanceKm=6.0
Driver=DRV-17
Payment=PAID txn=TXN-9001
RECEIPT: R-501 | fare=90.00
```

## 9. Hints (OOP-only)
- Make the booking service accept interfaces in constructor.
- Keep pricing rules separate from infrastructure calls.

## 10. Stretch goals
- Add a “mock” allocator and gateway for tests without touching booking logic.
