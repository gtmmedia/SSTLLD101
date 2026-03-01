public class ClassroomController {
    private final DeviceRegistry reg;

    public ClassroomController(DeviceRegistry reg) { this.reg = reg; }

    public void startClass() {
        Powerable pjPower = reg.getFirstOfType(Powerable.class, "Projector");
        pjPower.powerOn();
        InputConnectable pjInput = reg.getFirstOfType(InputConnectable.class, "Projector");
        pjInput.connectInput("HDMI-1");

        BrightnessControllable lights = reg.getFirstOfType(BrightnessControllable.class, "LightsPanel");
        lights.setBrightness(60);

        TemperatureControllable ac = reg.getFirstOfType(TemperatureControllable.class, "AirConditioner");
        ac.setTemperatureC(24);

        AttendanceScannable scan = reg.getFirstOfType(AttendanceScannable.class, "AttendanceScanner");
        System.out.println("Attendance scanned: present=" + scan.scanAttendance());
    }

    public void endClass() {
        System.out.println("Shutdown sequence:");
        reg.getFirstOfType(Powerable.class, "Projector").powerOff();
        reg.getFirstOfType(Powerable.class, "LightsPanel").powerOff();
        reg.getFirstOfType(Powerable.class, "AirConditioner").powerOff();
    }
}
