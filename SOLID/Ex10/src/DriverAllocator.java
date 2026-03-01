public class DriverAllocator implements AllocationService {
    @Override
    public String allocate(String studentId) {
        // fake deterministic driver
        return "DRV-17";
    }
}
