public class TripleRoomPricing implements RoomPricing {
    public double getMonthlyFee() { return 12000.0; }
    public double getDeposit() { return 5000.0; }
    public String getRoomType() { return "TRIPLE"; }
}