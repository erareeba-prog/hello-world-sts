package com.example.demo.service;

import com.example.demo.model.Donation;
import com.example.demo.model.Need;
import com.example.demo.model.Ngo;
import com.example.demo.model.Transaction;
import com.example.demo.repository.DonationRepository;
import com.example.demo.repository.NeedRepository;
import com.example.demo.repository.NgoRepository;
import com.example.demo.repository.TransactionRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PdfReportService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private NgoRepository ngoRepository;

    @Autowired
    private NeedRepository needRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    // ✅ Donation Receipt PDF
    public byte[] generateDonationReceipt(
            UUID donationId) throws Exception {

        Optional<Donation> donOpt =
            donationRepository.findById(donationId);
        if (donOpt.isEmpty())
            throw new RuntimeException(
                "Donation not found: " + donationId);

        Donation donation = donOpt.get();

        Optional<Transaction> txnOpt =
            transactionRepository
                .findByDonationId(donationId);

        ByteArrayOutputStream out =
            new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();

        // ── Header ──────────────────────────────────
        Font titleFont = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 22,
            new Color(21, 101, 192));
        Font headFont = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 12,
            Color.WHITE);
        Font labelFont = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 11,
            Color.DARK_GRAY);
        Font valueFont = FontFactory.getFont(
            FontFactory.HELVETICA, 11,
            Color.BLACK);
        Font smallFont = FontFactory.getFont(
            FontFactory.HELVETICA, 9,
            Color.GRAY);

        // Platform name
        Paragraph title = new Paragraph(
            "🏛️ NGO Donation Platform", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph subtitle = new Paragraph(
            "Official Donation Receipt",
            FontFactory.getFont(
                FontFactory.HELVETICA, 14,
                Color.GRAY));
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        doc.add(subtitle);

        // Divider
        doc.add(new Paragraph(
            "─────────────────────────────" +
            "──────────────────────",
            FontFactory.getFont(
                FontFactory.HELVETICA, 10,
                new Color(21, 101, 192))));

        doc.add(Chunk.NEWLINE);

        // ── Receipt Info Table ───────────────────────
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1.5f, 2.5f});
        infoTable.setSpacingBefore(10);
        infoTable.setSpacingAfter(10);

        addTableRow(infoTable, "Receipt ID",
            donation.getReceiptId() != null
                ? donation.getReceiptId() : "N/A",
            labelFont, valueFont);

        addTableRow(infoTable, "Transaction Ref",
            donation.getTransactionRef() != null
                ? donation.getTransactionRef() : "N/A",
            labelFont, valueFont);

        addTableRow(infoTable, "Donation ID",
            donationId.toString(),
            labelFont, valueFont);

        addTableRow(infoTable, "Amount",
            donation.getCurrency() + " " +
            donation.getAmountOrQty(),
            labelFont, valueFont);

        addTableRow(infoTable, "Status",
            donation.getStatus().toUpperCase(),
            labelFont, valueFont);

        addTableRow(infoTable, "Type",
            donation.getType() != null
                ? donation.getType() : "N/A",
            labelFont, valueFont);

        addTableRow(infoTable, "Date",
            donation.getDonatedAt() != null
                ? donation.getDonatedAt().format(FMT)
                : "N/A",
            labelFont, valueFont);

        // Donor display
        String donorDisplay =
            Boolean.TRUE.equals(donation.getIsAnonymous())
                ? "Anonymous Donor"
                : "Donor ID: " + donation.getDonorId();
        addTableRow(infoTable, "Donor",
            donorDisplay, labelFont, valueFont);

        // On behalf of
        if (donation.getOnBehalfOf() != null &&
            !donation.getOnBehalfOf().isEmpty()) {
            addTableRow(infoTable, "On Behalf Of",
                donation.getOnBehalfOf(),
                labelFont, valueFont);
        }

        doc.add(infoTable);

        // ── NGO Info ─────────────────────────────────
        if (txnOpt.isPresent()) {
            Transaction txn = txnOpt.get();
            doc.add(new Paragraph(
                "NGO & Need Details",
                FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 13,
                    new Color(21, 101, 192))));
            doc.add(Chunk.NEWLINE);

            PdfPTable ngoTable = new PdfPTable(2);
            ngoTable.setWidthPercentage(100);
            ngoTable.setWidths(new float[]{1.5f, 2.5f});
            ngoTable.setSpacingAfter(10);

            addTableRow(ngoTable, "NGO Name",
                txn.getNgoName() != null
                    ? txn.getNgoName() : "N/A",
                labelFont, valueFont);
            addTableRow(ngoTable, "Need Title",
                txn.getNeedTitle() != null
                    ? txn.getNeedTitle() : "N/A",
                labelFont, valueFont);
            doc.add(ngoTable);
        }

        // ── Tax Deductible Note ──────────────────────
        if (Boolean.TRUE.equals(
                donation.getTaxDeductible())) {
            PdfPTable taxTable = new PdfPTable(1);
            taxTable.setWidthPercentage(100);
            taxTable.setSpacingBefore(10);
            PdfPCell taxCell = new PdfPCell(
                new Phrase(
                    "✅ This donation is TAX DEDUCTIBLE " +
                    "under applicable tax laws. " +
                    "Please retain this receipt for " +
                    "your tax records.",
                    FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        10, new Color(46, 125, 50))));
            taxCell.setBackgroundColor(
                new Color(232, 245, 233));
            taxCell.setPadding(10);
            taxCell.setBorderColor(
                new Color(165, 214, 167));
            taxTable.addCell(taxCell);
            doc.add(taxTable);
        }

        // ── QR Placeholder ───────────────────────────
        doc.add(Chunk.NEWLINE);
        PdfPTable qrTable = new PdfPTable(1);
        qrTable.setWidthPercentage(40);
        qrTable.setHorizontalAlignment(
            Element.ALIGN_CENTER);
        PdfPCell qrCell = new PdfPCell(
            new Phrase(
                "[QR Code Placeholder]\nVerify at:\n" +
                "localhost:8080/api/donations/" +
                donationId + "/receipt",
                FontFactory.getFont(
                    FontFactory.HELVETICA, 9,
                    Color.GRAY)));
        qrCell.setHorizontalAlignment(
            Element.ALIGN_CENTER);
        qrCell.setPadding(15);
        qrCell.setBorderColor(Color.LIGHT_GRAY);
        qrTable.addCell(qrCell);
        doc.add(qrTable);

        // ── Footer ───────────────────────────────────
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph(
            "This is a system-generated receipt. " +
            "For queries contact support@ngoplatform.com",
            smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return out.toByteArray();
    }

    // ✅ NGO Impact Summary PDF
    public byte[] generateNgoImpactReport(
            UUID ngoId) throws Exception {

        Optional<Ngo> ngoOpt =
            ngoRepository.findById(ngoId);
        if (ngoOpt.isEmpty())
            throw new RuntimeException(
                "NGO not found: " + ngoId);

        Ngo ngo = ngoOpt.get();
        List<Need> needs =
            needRepository
                .findByNgoIdAndIsDeletedFalse(ngoId);
        List<Donation> donations =
            donationRepository.findByNgoId(ngoId);

        ByteArrayOutputStream out =
            new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titleFont = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 22,
            new Color(21, 101, 192));
        Font sectionFont = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 14,
            new Color(21, 101, 192));
        Font labelFont = FontFactory.getFont(
            FontFactory.HELVETICA_BOLD, 11,
            Color.DARK_GRAY);
        Font valueFont = FontFactory.getFont(
            FontFactory.HELVETICA, 11,
            Color.BLACK);
        Font smallFont = FontFactory.getFont(
            FontFactory.HELVETICA, 9,
            Color.GRAY);

        // ── Header ──────────────────────────────────
        Paragraph title = new Paragraph(
            "NGO Impact Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        Paragraph ngoName = new Paragraph(
            ngo.getName(),
            FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 16,
                Color.DARK_GRAY));
        ngoName.setAlignment(Element.ALIGN_CENTER);
        ngoName.setSpacingAfter(5);
        doc.add(ngoName);

        Paragraph location = new Paragraph(
            ngo.getCity() + ", " + ngo.getState(),
            FontFactory.getFont(
                FontFactory.HELVETICA, 11,
                Color.GRAY));
        location.setAlignment(Element.ALIGN_CENTER);
        location.setSpacingAfter(20);
        doc.add(location);

        // ── NGO Summary ──────────────────────────────
        doc.add(new Paragraph(
            "NGO Summary", sectionFont));
        doc.add(Chunk.NEWLINE);

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{1.5f, 2.5f});
        summaryTable.setSpacingAfter(15);

        addTableRow(summaryTable, "Registration No",
            ngo.getRegistrationNo() != null
                ? ngo.getRegistrationNo() : "N/A",
            labelFont, valueFont);
        addTableRow(summaryTable, "Status",
            ngo.getVerificationStatus()
                .toUpperCase(),
            labelFont, valueFont);
        addTableRow(summaryTable, "Contact",
            ngo.getContactPhone() != null
                ? ngo.getContactPhone() : "N/A",
            labelFont, valueFont);
        addTableRow(summaryTable, "Total Needs",
            String.valueOf(needs.size()),
            labelFont, valueFont);
        addTableRow(summaryTable, "Total Donations",
            String.valueOf(donations.size()),
            labelFont, valueFont);

        // Total funds
        BigDecimal totalFunds = donations.stream()
            .filter(d -> "confirmed"
                .equals(d.getStatus()))
            .map(Donation::getAmountOrQty)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        addTableRow(summaryTable, "Total Funds (INR)",
            "₹ " + totalFunds,
            labelFont, valueFont);

        doc.add(summaryTable);

        // ── Needs Breakdown ──────────────────────────
        doc.add(new Paragraph(
            "Needs Breakdown", sectionFont));
        doc.add(Chunk.NEWLINE);

        if (needs.isEmpty()) {
            doc.add(new Paragraph(
                "No needs found for this NGO.",
                valueFont));
        } else {
            PdfPTable needsTable = new PdfPTable(5);
            needsTable.setWidthPercentage(100);
            needsTable.setWidths(new float[]{
                2f, 1f, 1f, 1f, 1f});
            needsTable.setSpacingAfter(15);

            // Header row
            String[] headers = {
                "Title", "Category",
                "Status", "Required", "Fulfilled"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(
                    new Phrase(h,
                        FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            10, Color.WHITE)));
                cell.setBackgroundColor(
                    new Color(21, 101, 192));
                cell.setPadding(8);
                cell.setHorizontalAlignment(
                    Element.ALIGN_CENTER);
                needsTable.addCell(cell);
            }

            // Data rows
            for (Need need : needs) {
                needsTable.addCell(
                    styledCell(need.getTitle(),
                        valueFont));
                needsTable.addCell(
                    styledCell(
                        need.getCategory() != null
                            ? need.getCategory()
                            : "N/A",
                        valueFont));
                needsTable.addCell(
                    styledCell(need.getStatus(),
                        valueFont));
                needsTable.addCell(
                    styledCell(
                        need.getQtyRequired()
                            .toString(),
                        valueFont));
                needsTable.addCell(
                    styledCell(
                        need.getQtyFulfilled() != null
                            ? need.getQtyFulfilled()
                                .toString()
                            : "0",
                        valueFont));
            }
            doc.add(needsTable);
        }

        // ── Footer ───────────────────────────────────
        Paragraph footer = new Paragraph(
            "Generated by NGO Platform | " +
            java.time.LocalDateTime.now().format(FMT),
            smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return out.toByteArray();
    }

    // ✅ Helper — add table row
    private void addTableRow(PdfPTable table,
                              String label,
                              String value,
                              Font labelFont,
                              Font valueFont) {
        PdfPCell labelCell =
            new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(
            new Color(240, 244, 255));
        labelCell.setPadding(8);
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(labelCell);

        PdfPCell valueCell =
            new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(8);
        valueCell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(valueCell);
    }

    // ✅ Helper — styled cell
    private PdfPCell styledCell(String text,
                                 Font font) {
        PdfPCell cell =
            new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setBorderColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(
            Element.ALIGN_CENTER);
        return cell;
    }
}