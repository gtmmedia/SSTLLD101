import java.util.*;

public class CafeteriaSystem {
    private final Map<String, MenuItem> menu = new LinkedHashMap<>();
    private final InvoiceStore store;
    private final TaxRules taxRules;
    private final DiscountRules discountRules;
    private final InvoiceFormatter formatter;
    private int invoiceSeq = 1000;

    public CafeteriaSystem(InvoiceStore store, TaxRules taxRules, DiscountRules discountRules, InvoiceFormatter formatter) {
        this.store = store;
        this.taxRules = taxRules;
        this.discountRules = discountRules;
        this.formatter = formatter;
    }

    public void addToMenu(MenuItem i) { menu.put(i.id, i); }

    public void checkout(String customerType, List<OrderLine> lines) {
        String invId = "INV-" + (++invoiceSeq);
        List<OrderLine> detailedLines = new ArrayList<>();
        double subtotal = 0.0;
        for (OrderLine l : lines) {
            MenuItem item = menu.get(l.itemId);
            double lineTotal = item.price * l.qty;
            subtotal += lineTotal;
            detailedLines.add(new OrderLine(item.id, l.qty));
        }
        double taxPct = taxRules.taxPercent(customerType);
        double tax = subtotal * (taxPct / 100.0);
        double discount = discountRules.discountAmount(customerType, subtotal, lines.size());
        double total = subtotal + tax - discount;

        String printable = formatter.formatInvoice(invId, lines, subtotal, tax, discount, total, taxRules, discountRules, taxPct);
        System.out.print(printable);

        store.saveInvoice(invId, printable);
        if (store instanceof FileStore) {
            System.out.println("Saved invoice: " + invId + " (lines=" + ((FileStore)store).countLines(invId) + ")");
        }
    }
}
