@echo off
REM Compile script for Elevator System

echo Compiling Elevator System...

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin

REM Compile all Java files from src directory
javac -d bin src\com\example\elevator\*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Compilation successful!
    echo.
    echo To run the system, execute: java -cp bin com.example.elevator.ElevatorSystem
) else (
    echo.
    echo Compilation failed!
)
pause
