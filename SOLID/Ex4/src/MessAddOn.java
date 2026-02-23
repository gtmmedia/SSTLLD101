public class MessAddOn implements AddOnPricing {
    public String getAddOnName() { return "MESS"; }
    public double getMonthlyFee() { return 1000.0; }
    public double getDeposit() { return 0.0; }
}