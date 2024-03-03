package com.cosider.test.views;

import com.cosider.test.entities.Invoice;
import com.cosider.test.entities.Item;

import com.cosider.test.service.InvoiceService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;


public class SingleInvoice extends VerticalLayout {
    Grid<Item> grid = new Grid<>(Item.class, true);

    @Getter
    private final Invoice invoice;
    SingleInvoice (Invoice value) throws IOException {
        this.invoice=value;
        addClassName("list-view");
        configureGrid();
        setSizeFull();
        grid.setItems(value.InvoiceItems());
        add(grid);
    }
    private void configureGrid () {
        grid.addClassNames("invoice-grid");
        grid.setSizeFull();
        grid.addColumn(Item::ItemPrice).setHeader("Prix d’item");
        grid.addColumn(Item::ItemQuantity).setHeader("Quantité d’item");
        grid.addColumn(Item::ItemLibelle).setHeader(" Item Libelle");
        grid.addColumn(Item::ItemUnit).setHeader("Unité d’Item");
        grid.addColumn(Item::ItemTax).setHeader("Taxe d’item");
        grid.addColumn(item -> item.calculateTTC(item)).setHeader("Montant TTC");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

}
