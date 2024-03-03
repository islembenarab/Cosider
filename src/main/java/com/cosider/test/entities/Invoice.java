package com.cosider.test.entities;



import java.math.BigDecimal;
import java.util.List;

public record Invoice ( String InvoiceID, String InvoiceDate, String ClientName, String ClientRC, String ClientAddress, String ClientPhone, String ClientBank, String SupplierName, String SupplierRC, String SupplierAddress, String SupplierPhone, String SupplierBank, List<Item> InvoiceItems){
    // Getters and setters (omitted for brevity)
    public BigDecimal calculateTTC(List<Item> invoiceItems) {
        BigDecimal sum = BigDecimal.ZERO;

        // Iterate through each item and accumulate the total price
        for (Item item : invoiceItems) {
            BigDecimal itemPrice = BigDecimal.valueOf(item.ItemPrice());  // Get item price as BigDecimal
            int itemQuantity = item.ItemQuantity();
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(itemQuantity));  // Calculate item total
            itemTotal = itemTotal.add(BigDecimal.valueOf(item.ItemTax()));
            sum = sum.add(itemTotal);                                      // Add item total to the running sum
        }

        return sum;
    }public BigDecimal calculateTotal(List<Item> invoiceItems) {
        BigDecimal sum = BigDecimal.ZERO;

        // Iterate through each item and accumulate the total price
        for (Item item : invoiceItems) {
            BigDecimal itemPrice = BigDecimal.valueOf(item.ItemPrice());  // Get item price as BigDecimal
            int itemQuantity = item.ItemQuantity();
            BigDecimal itemTotal = itemPrice.multiply(BigDecimal.valueOf(itemQuantity));  // Calculate item total
            sum = sum.add(itemTotal);                                      // Add item total to the running sum
        }

        return sum;
    }
    public BigDecimal calculateTVA(List<Item> invoiceItems) {
        BigDecimal sum = BigDecimal.ZERO;


        for (Item item : invoiceItems) {
            BigDecimal itemTax = BigDecimal.valueOf(item.ItemTax());

            sum = sum.add(itemTax);
        }

        return sum;
    }

}


