package com.cosider.test.service;


import com.cosider.test.entities.Invoice;
import com.cosider.test.entities.Item;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.vaadin.flow.server.StreamResource;
import lombok.RequiredArgsConstructor;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;


@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final RestTemplate restTemplate;


    public List<Invoice> getInvoices() { // Use Map instead of LinkedHashMap
        final String url = "https://elhoussam.github.io/invoicesapi/db.json";

        try {
            return restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Invoice>>() {}).getBody();

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving invoices: " + e.getMessage(), e);
        }
    }
    public StreamResource createPdfInvoice(Invoice invoice, String outputFile) throws IOException {
        PdfWriter writer = new PdfWriter(outputFile);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        document.setMargins(20, 20, 20, 20);
        // Use a bold font for headings
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);


        Paragraph invoiceTitle = new Paragraph("Facture N= " + invoice.InvoiceID())
                .setFont(regular)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(invoiceTitle);
        // Add the date of the invoice
        Paragraph invoiceDate = new Paragraph("Date de facture : " + invoice.InvoiceDate())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(70) // Add some space after the date
                .setMarginLeft(50);// Add some space after the date
        document.add(invoiceDate);
        // Add supplier and client information
        // For simplicity, let's assume that the invoice object has methods to get these details
        Paragraph supplierInfo = new Paragraph("FOURNISSEUR\n" + invoice.SupplierName())
                .setFontSize(12).setUnderline();
        Paragraph clientInfo = new Paragraph("CLIENT\n" + invoice.ClientName())
                .setFontSize(12).setUnderline();
        // Add the supplier and client information side by side

        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth().setBorder(Border.NO_BORDER);
        infoTable.addCell(new Cell().add(supplierInfo));
        infoTable.addCell(new Cell().add(clientInfo));
        document.add(infoTable);

        // Add a table for item details
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 3, 1, 1, 1, 1}))
                .setAutoLayout().setMarginTop(60).setPadding(0);
        Style header = new Style()
                .setBackgroundColor(new DeviceRgb(255, 165, 0))
                .setFont(bold).setPadding(0).setMargin(0);
        // Add headers
        table.addHeaderCell(new Paragraph().addStyle(header).add("N°")).setPadding(0);
        table.addHeaderCell(new Paragraph().addStyle(header).add("LIBELLE")).setPadding(0);
        table.addHeaderCell(new Paragraph().addStyle(header).add("QUANTITÉ")).setPadding(0);
        table.addHeaderCell(new Paragraph().addStyle(header).add("PRIX")).setPadding(0);
        table.addHeaderCell(new Paragraph().addStyle(header).add("HT")).setPadding(0);
        table.addHeaderCell(new Paragraph().addStyle(header).add("TTC")).setPadding(0);

        // Add items
        for (Item item : invoice.InvoiceItems()) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(item.ItemID()))));
            table.addCell(new Cell().add(new Paragraph(item.ItemLibelle())));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(item.ItemQuantity()))));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(item.ItemPrice()))));
            // Assuming we have methods to get the HT and TTC values
            table.addCell(new Cell().add(new Paragraph(String.valueOf(item.calculateHT(item)))));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(item.calculateTTC(item)))));
        }
        document.add(table);

        // Add totals
        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setMarginTop(50).setMarginRight(25)
                .setHorizontalAlignment(HorizontalAlignment.RIGHT).setAutoLayout();

        totalTable.addCell(new Cell().add(new Paragraph("TOTAL")));
        totalTable.addCell(new Cell().add(new Paragraph(invoice.calculateTotal(invoice.InvoiceItems()).toString())));
        totalTable.addCell(new Cell().add(new Paragraph("TVA")));
        totalTable.addCell(new Cell().add(new Paragraph(invoice.calculateTVA(invoice.InvoiceItems()).toString())));
        totalTable.addCell(new Cell().add(new Paragraph("Total TTC")));
        totalTable.addCell(new Cell().add(new Paragraph(invoice.calculateTTC(invoice.InvoiceItems()).toString())));

        document.add(totalTable);

        // Add signature area
        Paragraph signature = new Paragraph("LA SIGNATURE")
                .setFontSize(15).setMarginTop(50).setMarginRight(100)
                .setTextAlignment(TextAlignment.RIGHT).setMarginTop(20);
        document.add(signature);
        // Close the document
        document.close();
        return new StreamResource(outputFile, () -> {
            try {
                // Open a new FileInputStream for the outputFile
                return new FileInputStream(outputFile);
            } catch (FileNotFoundException e) {
                // Handle the case where the outputFile could not be found
                e.printStackTrace();
                return null;
            }
        });

    }



}
