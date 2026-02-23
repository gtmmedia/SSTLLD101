public class Money {
    public final double amount;
    public Money(double amount) { this.amount = round2(amount); }

    public Money plus(Money other) { return new Money(this.amount + other.amount); }

    private static double round2(double x) { return Math.round(x * 100.0) / 100.0; }

    @Override public String toString() { return String.format("%.2f", amount); }
}
