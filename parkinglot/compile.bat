@echo off

echo Compiling Parking Lot...
if not exist bin mkdir bin

javac -d bin src\com\example\parkinglot\*.java

if %ERRORLEVEL% EQU 0 (
  echo Compilation successful.
  echo Run with: java -cp bin com.example.parkinglot.Main
) else (
  echo Compilation failed.
)
