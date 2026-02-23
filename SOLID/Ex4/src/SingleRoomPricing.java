public class SingleRoomPricing implements RoomPricing {
    public double getMonthlyFee() { return 14000.0; }
    public double getDeposit() { return 5000.0; }
    public String getRoomType() { return "SINGLE"; }
}