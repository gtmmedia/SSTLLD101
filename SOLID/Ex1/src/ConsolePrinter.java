import java.util.List;

public class ConsolePrinter {
    public void successPrinter(String id, StudentRecord rec, int StudentCount) {
        System.out.println("OK: created student " + id);
        System.out.println("Saved. Total students: " + StudentCount);
        System.out.println("CONFIRMATION:");
        System.out.println(rec);
    }

    static void printErrors(List<String> errors) {
        System.out.println("ERROR: cannot register");
            for (String e : errors) System.out.println("- " + e);
    }
}
