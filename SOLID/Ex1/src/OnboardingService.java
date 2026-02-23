import java.util.*;

public class OnboardingService {
    private final StudentRepository db;
    private final InputParser inputParser;
    private final StudentValidator studentValidator;
    private final ConsolePrinter consolePrinter;

    public OnboardingService(StudentRepository db) { 
        this.db = db; 
        this.inputParser = new InputParser();
        this.studentValidator = new StudentValidator(Set.of("CSE", "AI" , "SWE"));    
        this.consolePrinter = new ConsolePrinter();   
        }

    // Intentionally violates SRP: parses + validates + creates ID + saves + prints.
    public void registerFromRawInput(String raw) {
        System.out.println("INPUT: " + raw);

        Map<String, String> kv = inputParser.parse(raw);

        String name = kv.getOrDefault("name", "");
        String email = kv.getOrDefault("email", "");
        String phone = kv.getOrDefault("phone", "");
        String program = kv.getOrDefault("program", "");

        // validation inline, printing inline
        List<String> errors = studentValidator.validate(kv);

        if (!errors.isEmpty()) {
            ConsolePrinter.printErrors(errors);
            return;
        }
        
        String id = IdUtil.nextStudentId(db.count());
        StudentRecord rec = new StudentRecord(id, name, email, phone, program);

        db.save(rec);

        consolePrinter.successPrinter(id, rec, db.count());
    }
}
