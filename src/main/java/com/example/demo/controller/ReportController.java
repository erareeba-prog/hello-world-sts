package com.example.demo.controller;

import com.example.demo.service.PdfReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports",
     description = "PDF report generation endpoints")
public class ReportController {

    @Autowired
    private PdfReportService pdfReportService;

    // ✅ GET /api/reports/donation-receipt/{donationId}
    @GetMapping("/donation-receipt/{donationId}")
    @Operation(
        summary = "Generate donation receipt PDF",
        description = "Returns a formatted PDF receipt " +
            "for a donation including tax-deductible " +
            "note, NGO details and QR placeholder")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "PDF generated successfully"),
        @ApiResponse(responseCode = "404",
            description = "Donation not found")
    })
    public void getDonationReceipt(
            @Parameter(description = "Donation UUID")
            @PathVariable UUID donationId,
            HttpServletResponse response) {
        try {
            byte[] pdf =
                pdfReportService
                    .generateDonationReceipt(donationId);
            response.setContentType(
                "application/pdf");
            response.setHeader(
                "Content-Disposition",
                "inline; filename=\"receipt_" +
                donationId + ".pdf\"");
            response.setContentLength(pdf.length);
            response.getOutputStream().write(pdf);
            response.getOutputStream().flush();
        } catch (Exception e) {
            try {
                response.setStatus(
                    e.getMessage().contains("not found")
                        ? 404 : 500);
                response.getWriter().write(
                    "Error: " + e.getMessage());
            } catch (Exception ignored) {}
        }
    }

    // ✅ GET /api/reports/ngo-impact/{ngoId}
    @GetMapping("/ngo-impact/{ngoId}")
    @Operation(
        summary = "Generate NGO impact summary PDF",
        description = "Returns a PDF with fund summary, " +
            "needs breakdown and donation statistics " +
            "for a specific NGO")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "PDF generated successfully"),
        @ApiResponse(responseCode = "404",
            description = "NGO not found")
    })
    public void getNgoImpactReport(
            @Parameter(description = "NGO UUID")
            @PathVariable UUID ngoId,
            HttpServletResponse response) {
        try {
            byte[] pdf =
                pdfReportService
                    .generateNgoImpactReport(ngoId);
            response.setContentType(
                "application/pdf");
            response.setHeader(
                "Content-Disposition",
                "inline; filename=\"ngo_impact_" +
                ngoId + ".pdf\"");
            response.setContentLength(pdf.length);
            response.getOutputStream().write(pdf);
            response.getOutputStream().flush();
        } catch (Exception e) {
            try {
                response.setStatus(
                    e.getMessage().contains("not found")
                        ? 404 : 500);
                response.getWriter().write(
                    "Error: " + e.getMessage());
            } catch (Exception ignored) {}
        }
    }
}