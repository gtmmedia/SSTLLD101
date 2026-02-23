import java.util.*;

public class InvoiceFormatter {
    public String formatInvoice(String invoiceId, List<OrderLine> orderLines, double subtotal, double tax, double discount, double total, TaxRules taxRules, DiscountRules discountRules, double taxPct) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Cafeteria Billing ===\n");
        sb.append("Invoice# ").append(invoiceId).append("\n");
        for (OrderLine line : orderLines) {
            // For printing, you may need to access menu item name and price elsewhere if needed
            sb.append("- ").append(line.itemId).append(" x").append(line.qty).append(" = ")
              .append(String.format("%.2f", 0.0)).append("\n"); // Placeholder, update as needed
        }
        sb.append(String.format("Subtotal: %.2f\n", subtotal));
        sb.append(String.format("Tax(%.0f%%): %.2f\n", taxPct, tax));
        sb.append(String.format("Discount: -%.2f\n", discount));
        sb.append(String.format("TOTAL: %.2f\n", total));
        return sb.toString();
    }
}
