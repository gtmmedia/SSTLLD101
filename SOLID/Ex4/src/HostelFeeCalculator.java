import java.util.*;

public class HostelFeeCalculator {
    private final FakeBookingRepo repo;

    public HostelFeeCalculator(FakeBookingRepo repo) { this.repo = repo; }

    public void process(BookingRequest req) {
        RoomPricing roomPricing = getRoomPricing(req.roomType);
        List<AddOnPricing> addOnPricings = new ArrayList<>();
        for (AddOn a : req.addOns) {
            addOnPricings.add(getAddOnPricing(a));
        }

        double monthlyTotal = roomPricing.getMonthlyFee();
        double depositTotal = roomPricing.getDeposit();
        for (AddOnPricing addon : addOnPricings) {
            monthlyTotal += addon.getMonthlyFee();
            depositTotal += addon.getDeposit();
        }

        Money monthly = new Money(monthlyTotal);
        Money deposit = new Money(depositTotal);

        ReceiptPrinter.print(req, monthly, deposit);

        String bookingId = "H-" + (7000 + new Random(1).nextInt(1000)); // deterministic-ish
        repo.save(bookingId, req, monthly, deposit);
    }

    private RoomPricing getRoomPricing(int roomType) {
        return switch (roomType) {
            case LegacyRoomTypes.SINGLE -> new SingleRoomPricing();
            case LegacyRoomTypes.DOUBLE -> new DoubleRoomPricing();
            case LegacyRoomTypes.TRIPLE -> new TripleRoomPricing();
            default -> new DefaultRoomPricing();
        };
    }

    private AddOnPricing getAddOnPricing(AddOn addOn) {
        return switch (addOn) {
            case MESS -> new MessAddOn();
            case LAUNDRY -> new LaundryAddOn();
            case GYM -> new GymAddOn();
        };
    }
}
