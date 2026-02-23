# Ex8 — ISP: Student Club Management Admin Tools

## 1. Context
Clubs have different roles: treasurer, secretary, event lead. The admin tool interface currently combines everything.

## 2. Current behavior
- One interface `ClubAdminTools` includes finance, minutes, and event operations
- Each role tool implements methods it does not use (dummy / exceptions)
- `ClubConsole` calls only the relevant subset per role

## 3. What’s wrong (at least 5 issues)
1. Fat interface forces irrelevant methods.
2. Dummy implementations cause hidden failures later.
3. Clients depend on methods they don’t need.
4. New role tools become harder to implement safely.
5. Capabilities are unclear.

## 4. Your task
- Split interface into smaller role/capability interfaces.
- Ensure each tool depends only on the methods it uses.
- Preserve output.

## 5. Constraints
- Preserve output and command order.
- Keep class names unchanged.

## 6. Acceptance criteria
- No dummy/no-op implementations for irrelevant methods.
- `ClubConsole` depends on minimal interfaces.

## 7. How to run
```bash
cd SOLID/Ex8/src
javac *.java
java Main
```

## 8. Sample output
```text
=== Club Admin ===
Ledger: +5000 (sponsor)
Minutes added: "Meeting at 5pm"
Event created: HackNight (budget=2000)
Summary: ledgerBalance=5000, minutes=1, events=1
```

## 9. Hints (OOP-only)
- Identify client groups: finance client, minutes client, events client.
- Split by what callers actually need.

## 10. Stretch goals
- Add “publicity lead” without implementing finance methods.
