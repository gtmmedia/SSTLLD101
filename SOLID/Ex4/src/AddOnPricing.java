public interface AddOnPricing {
    String getAddOnName();
    double getMonthlyFee();
    double getDeposit(); // If add-on has deposit, else return 0
}
