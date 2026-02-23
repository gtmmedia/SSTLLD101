public class LightsPanel implements SmartClassroomDevice {
    @Override public void powerOn() { /* always on */ }
    @Override public void powerOff() { System.out.println("Lights OFF"); }

    @Override public void setBrightness(int pct) { System.out.println("Lights set to " + pct + "%"); }
    @Override public void setTemperatureC(int c) { /* irrelevant */ }
    @Override public int scanAttendance() { return 0; } // dummy
    @Override public void connectInput(String port) { /* irrelevant */ }
}
