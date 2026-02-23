import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StudentValidator {

    private final Set<String> allowedPrograms;

    public StudentValidator(Set<String> allowedPrograms) {
        this.allowedPrograms = allowedPrograms;
    }
    
    public List<String> validate(Map<String, String> parsedData) {
        List<String> errors = new ArrayList<>();
        if (parsedData.get("name").isBlank())
            errors.add("name is required");
        if (parsedData.get("email").isBlank() || !parsedData.get("email").contains("@"))
            errors.add("email is invalid");
        if (parsedData.get("phone").isBlank() || !parsedData.get("phone").chars().allMatch(Character::isDigit))
            errors.add("phone is invalid");
        if (!allowedPrograms.contains(parsedData.get("program")))
            errors.add("program is invalid");        return errors;
     }
}
