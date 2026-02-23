public interface SmartClassroomDevice {
    // Fat interface (ISP violation)
    void powerOn();
    void powerOff();
    void setBrightness(int pct);
    void setTemperatureC(int c);
    int scanAttendance();
    void connectInput(String port);
}
