package com.cosider.test.views;

import com.cosider.test.entities.Invoice;
import com.cosider.test.service.InvoiceService;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;


@Route("") // map view to the root
public class ListInvoices extends VerticalLayout {
    Grid<Invoice> grid = new Grid<>(Invoice.class, true);
    Dialog dialog = new Dialog();
    Dialog printDialog = new Dialog();
    InvoiceService invoiceService=new InvoiceService(new RestTemplate());



    TextField filterText = new TextField("");
    List<Invoice> list;

    ListInvoices() {
        addClassName("list-view");
        setSizeFull(); // Ensure ListInvoices expands to fit its container

        configureGrid();

        // Update grid with initial data
        list = invoiceService.getInvoices();
        grid.setItems(list); // Load initial data into the grid

        add(getToolbar(), grid);

        // Add value change listener *after* loading initial data
        filterText.addValueChangeListener(event -> {
            String value = event.getValue();

            list = invoiceService.getInvoices().stream()
                    .filter(invoice -> invoice.InvoiceItems().stream()
                            .anyMatch(item -> item.ItemLibelle().toLowerCase().contains(value.toLowerCase())))
                    .toList();
            grid.setItems(list); // Apply filtered results to the grid
        });
    }
    private HorizontalLayout getToolbar() {
        filterText.setPlaceholder("Filter by item Libelle...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.setValue("");
        var toolbar = new HorizontalLayout(filterText);
        toolbar.addClassName("toolbar");
        return toolbar;
    }
    private void configureGrid () {
        grid.addClassNames("invoice-grid");
        grid.setSizeFull();
        grid.addColumn(Invoice::InvoiceID).setHeader("Facture ID");
        grid.addColumn(Invoice::InvoiceDate).setHeader(" Facture Date");
        grid.addColumn(Invoice::SupplierName).setHeader(" Fournisseur Nom");
        grid.addColumn(Invoice::ClientName).setHeader(" Client Nom");
        grid.addColumn(invoice -> invoice.calculateTTC(invoice.InvoiceItems())).setHeader("Montant TTC");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) { // Ensure a row is selected
                try {

                    addDialog(new SingleInvoice(event.getValue()));
                } catch (IOException e) {
                    makeAlertError(e.getMessage());
                }
                dialog.open();
            } else {
                makeAlertError("don't Click too much");
            }
        });


    }

    private void addDialog (SingleInvoice singleInvoice) {
        dialog.setHeaderTitle("Invoice Information");
        dialog.add(singleInvoice);
        dialog.setSizeFull();
        // Add the Cancel button only once
        dialog.getFooter().removeAll();
        Button cancelButton = new Button("close", e -> {
            dialog.removeAll();
            dialog.close();
        });
        dialog.getFooter().add(cancelButton);
        Button printButton = new Button("print", e -> {
            Invoice invoice = singleInvoice.getInvoice();
            try {

                StreamResource resource = invoiceService.createPdfInvoice(invoice, invoice.InvoiceID() + ".pdf");
                PdfViewer pdfViewer = new PdfViewer();
                pdfViewer.setAddPrintButton(true);
                pdfViewer.setSrc(resource);
                addPrintDialog(pdfViewer);
                dialog.close();
                dialog.removeAll();
                printDialog.open();
            } catch (IOException ex) {
                makeAlertError(ex.getMessage());
            }
        });
        printButton.getStyle().set("background-color", "#007bff");
        printButton.getStyle().set("color", "white");
        dialog.getFooter().add(printButton);
    }
    private void addPrintDialog (PdfViewer pdfViewer) {
        printDialog.setHeaderTitle("Print Invoice");
        printDialog.add(pdfViewer);
        printDialog.setSizeFull();
        // Add the Cancel button only once
        printDialog.getFooter().removeAll();
        Button cancelPrintButton = new Button("close", e -> {
            printDialog.removeAll();
            printDialog.close();
        });
        printDialog.getFooter().add(cancelPrintButton);
    }

    public void makeAlertError(String message){
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

        Div text = new Div(new Text(message));

        Button closeButton = new Button(new Icon("lumo", "cross"));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        closeButton.setAriaLabel("Close");
        closeButton.addClickListener(event1 -> notification.close());

        HorizontalLayout layout = new HorizontalLayout(text, closeButton);
        layout.setAlignItems(Alignment.CENTER);

        notification.add(layout);
        notification.open();
    }

}
