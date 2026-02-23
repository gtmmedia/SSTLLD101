# SST28-LLD101 — Refactoring Practice (SOLID)

This repository contains **10 refactoring exercises** for Scaler School of Technology students who have already covered OOP basics and are now practicing **SOLID** via behavior-preserving refactors.

Each exercise starts with:
- **Working code** (compiles + runs)
- **Intentionally messy design** that violates one SOLID principle heavily
- A clear set of refactoring tasks, constraints, and acceptance criteria

Important: **Design patterns are intentionally not used yet** in the instructions. You should solve these problems using only **OOP mechanisms** (classes, interfaces, composition, polymorphism, encapsulation) and good modular design.

## Suggested workflow
1) Read the exercise `README.md`
2) Run the program and note the output
3) Identify concrete design smells
4) Refactor in **small, safe steps** (compile often)
5) Re-run and compare output to ensure behavior is preserved

## SOLID cheat-sheet (short)
- **S — SRP:** A class should have one primary reason to change.
- **O — OCP:** Add new behavior by extension, not by editing large conditionals.
- **L — LSP:** Subtypes must be usable wherever the base type is expected, without surprises.
- **I — ISP:** Prefer small, client-specific interfaces over “fat” ones.
- **D — DIP:** High-level code should depend on abstractions, not concrete implementations.

## How exercises are mapped
- Ex1–Ex2: SRP
- Ex3–Ex4: OCP
- Ex5–Ex6: LSP
- Ex7–Ex8: ISP
- Ex9–Ex10: DIP

## Running any exercise
From the exercise folder (example: `SOLID/Ex1/src`):

```bash
javac *.java
java Main
```

No Maven/Gradle. Java 17. Default package (no `package` lines).
