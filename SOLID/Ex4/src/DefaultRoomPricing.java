public class DefaultRoomPricing implements RoomPricing {
    public double getMonthlyFee() { return 16000.0; }
    public double getDeposit() { return 5000.0; }
    public String getRoomType() { return "DEFAULT"; }
}