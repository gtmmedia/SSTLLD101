public class Projector implements SmartClassroomDevice {
    private boolean on;

    @Override public void powerOn() { on = true; }
    @Override public void powerOff() { on = false; System.out.println("Projector OFF"); }

    @Override public void setBrightness(int pct) { /* irrelevant */ }
    @Override public void setTemperatureC(int c) { /* irrelevant */ }
    @Override public int scanAttendance() { return 0; } // dummy
    @Override public void connectInput(String port) { if (on) System.out.println("Projector ON (" + port + ")"); }
}
