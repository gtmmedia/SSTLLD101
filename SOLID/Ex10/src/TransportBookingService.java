public class TransportBookingService {
    private final DistanceService distanceService;
    private final AllocationService allocationService;
    private final PaymentService paymentService;

    public TransportBookingService(DistanceService distanceService, AllocationService allocationService, PaymentService paymentService) {
        this.distanceService = distanceService;
        this.allocationService = allocationService;
        this.paymentService = paymentService;
    }

    public void book(TripRequest req) {
        double km = distanceService.km(req.from, req.to);
        System.out.println("DistanceKm=" + km);

        String driver = allocationService.allocate(req.studentId);
        System.out.println("Driver=" + driver);

        double fare = 50.0 + km * 6.6666666667; // messy pricing
        fare = Math.round(fare * 100.0) / 100.0;

        String txn = paymentService.charge(req.studentId, fare);
        System.out.println("Payment=PAID txn=" + txn);

        BookingReceipt r = new BookingReceipt("R-501", fare);
        System.out.println("RECEIPT: " + r.id + " | fare=" + String.format("%.2f", r.fare));
    }
}
