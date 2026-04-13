@echo off

echo Compiling Movie Ticket Booking...
if not exist bin mkdir bin

javac -d bin src\com\example\movieticket\*.java

if %ERRORLEVEL% EQU 0 (
  echo Compilation successful.
  echo Run with: java -cp bin com.example.movieticket.Main
) else (
  echo Compilation failed.
)
