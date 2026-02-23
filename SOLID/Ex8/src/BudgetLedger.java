public class BudgetLedger {
    private double balance = 0.0;
    public void add(double amt, String note) {
        balance += amt;
        if (amt >= 0) System.out.println("Ledger: +" + (int)amt + " (" + note + ")");
        else System.out.println("Ledger: " + (int)amt + " (" + note + ")");
    }
    public int balanceInt() { return (int)balance; }
}
