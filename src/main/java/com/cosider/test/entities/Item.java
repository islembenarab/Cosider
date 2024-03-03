package com.cosider.test.entities;

import com.vaadin.flow.component.grid.Grid;
import lombok.Data;

public record Item(
        String ItemID,
        String ItemLibelle,
        String ItemUnit,
        int ItemQuantity,
        double ItemPrice,
        double ItemTax) {
    public Double calculateTTC (Item item) {
        return item.ItemPrice()* item.ItemQuantity()+ item.ItemTax();
    }   public Double calculateHT (Item item) {
        return item.ItemTax();
    }

    // Getters and setters (omitted for brevity)
}