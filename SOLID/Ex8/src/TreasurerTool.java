public class TreasurerTool implements ClubAdminTools {
    private final BudgetLedger ledger;
    public TreasurerTool(BudgetLedger ledger) { this.ledger = ledger; }

    @Override public void addIncome(double amt, String note) { ledger.add(amt, note); }
    @Override public void addExpense(double amt, String note) { ledger.add(-amt, note); }

    @Override public void addMinutes(String text) { /* irrelevant */ }
    @Override public void createEvent(String name, double budget) { /* irrelevant */ }
    @Override public int getEventsCount() { return 0; } // dummy
}
