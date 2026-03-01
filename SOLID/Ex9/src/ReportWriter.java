public class ReportWriter implements Writer {
    @Override public String write(Submission s, int plag, int code) {
        // writes to a pretend file name
        return "report-" + s.roll + ".txt";
    }
}
